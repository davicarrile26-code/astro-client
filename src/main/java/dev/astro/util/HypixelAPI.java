package dev.astro.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles Hypixel API calls with a strict 150 requests/minute rate limiter.
 * All HTTP requests run on a background thread pool to keep the render thread fast.
 * Results are cached in a ConcurrentHashMap for thread-safe access.
 */
public final class HypixelAPI {

    private static final int MAX_REQUESTS_PER_MINUTE = 299;
    private static final long WINDOW_MS = 60_000L;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    private String apiKey;
    private final Map<UUID, Integer> bedwarsLevelCache = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, Integer> hypixelLevelCache = new ConcurrentHashMap<UUID, Integer>();
    /** UUIDs currently being fetched — prevents duplicate requests */
    private final Map<UUID, Boolean> pending = new ConcurrentHashMap<UUID, Boolean>();
    private final Queue<Long> requestTimestamps = new ConcurrentLinkedQueue<Long>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private File keyFile;

    /**
     * Initialise — reads the FIRED API key from firedclient/apikey.txt (with legacy migration)
     */
    public void init() {
        File mcDir = Minecraft.getMinecraft().mcDataDir;
        File dir = new File(mcDir, "firedclient");
        if (!dir.exists()) dir.mkdirs();
        keyFile = new File(dir, "apikey.txt");
        File legacyKeyFile = new File(new File(mcDir, "astroclient"), "apikey.txt");
        if (!keyFile.exists() && legacyKeyFile.exists()) {
            try (InputStream in = new FileInputStream(legacyKeyFile); OutputStream out = new FileOutputStream(keyFile)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            } catch (Exception e) {
                System.err.println("[FIRED Client] Could not migrate the old API key: " + e.getMessage());
            }
        }

        if (keyFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(keyFile));
                String line = reader.readLine();
                reader.close();
                if (line != null && !line.trim().isEmpty()) {
                    apiKey = line.trim();
                }
            } catch (Exception e) {
                System.err.println("[FIRED Client] Failed to read API key: " + e.getMessage());
            }
        }

        if (apiKey != null) {
            System.out.println("[FIRED Client] Hypixel API key loaded (" + apiKey.substring(0, 8) + "...)");
        } else {
            System.out.println("[FIRED Client] No Hypixel API key set. Use /firedkey <key> in-game.");
        }
    }

    /**
     * Set API key and persist to disk.
     */
    public void setApiKey(String key) {
        this.apiKey = key;
        bedwarsLevelCache.clear();
        hypixelLevelCache.clear();
        pending.clear();
        try {
            FileWriter fw = new FileWriter(keyFile);
            fw.write(key);
            fw.close();
        } catch (Exception e) {
            System.err.println("[FIRED Client] Failed to save API key: " + e.getMessage());
        }
    }

    public String getApiKey() { return apiKey; }
    public boolean hasKey() { return apiKey != null && !apiKey.isEmpty(); }

    public int getLevel(UUID uuid) {
        return getBedwarsLevel(uuid);
    }

    public int peekBedwarsLevel(UUID uuid) {
        Integer cached = bedwarsLevelCache.get(uuid);
        return cached != null ? cached : -1;
    }

    public int peekHypixelLevel(UUID uuid) {
        Integer cached = hypixelLevelCache.get(uuid);
        return cached != null ? cached : -1;
    }

    /**
     * Returns the cached Bedwars level for a player, or -1 if not yet fetched.
     * Automatically kicks off an async fetch if not cached.
     */
    public int getBedwarsLevel(UUID uuid) {
        int cached = peekBedwarsLevel(uuid);
        if (cached >= 0) return cached;

        if (!pending.containsKey(uuid)) {
            fetchProfileAsync(uuid);
        }
        return -1;
    }

    /**
     * Returns the cached Hypixel network level for a player, or -1 if not yet fetched.
     * Automatically kicks off an async fetch if not cached.
     */
    public int getHypixelLevel(UUID uuid) {
        int cached = peekHypixelLevel(uuid);
        if (cached >= 0) return cached;

        if (!pending.containsKey(uuid)) {
            fetchProfileAsync(uuid);
        }
        return -1;
    }

    /**
     * Fetch a player's Bedwars level from the API on a background thread.
     */
    private void fetchProfileAsync(UUID uuid) {
        if (!hasKey()) return;
        if (!canMakeRequest()) return;
        pending.put(uuid, Boolean.TRUE);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    recordRequest();
                    String uuidStr = uuid.toString().replace("-", "");
                    URL url = new URL("https://api.hypixel.net/player?key=" + apiKey + "&uuid=" + uuidStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(CONNECT_TIMEOUT);
                    conn.setReadTimeout(READ_TIMEOUT);

                    int code = conn.getResponseCode();
                    if (code == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                        reader.close();

                        JsonObject json = new JsonParser().parse(sb.toString()).getAsJsonObject();
                        if (json.has("success") && json.get("success").getAsBoolean()
                                && json.has("player") && !json.get("player").isJsonNull()) {
                            JsonObject player = json.getAsJsonObject("player");
                            Integer hypixelLevel = parseHypixelLevel(player);
                            if (hypixelLevel != null) {
                                hypixelLevelCache.put(uuid, hypixelLevel);
                            }
                            if (player.has("achievements")) {
                                JsonObject achievements = player.getAsJsonObject("achievements");
                                if (achievements.has("bedwars_level")) {
                                    bedwarsLevelCache.put(uuid, achievements.get("bedwars_level").getAsInt());
                                }
                            }
                        }
                    } else if (code == 429) {
                        // Rate limited — back off
                        System.out.println("[FIRED Client] Hypixel API rate limited, backing off");
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    // Silently fail — LevelHead falls back to tab parsing
                } finally {
                    pending.remove(uuid);
                }
            }
        });
    }

    private boolean canMakeRequest() {
        pruneOldRequests();
        return requestTimestamps.size() < MAX_REQUESTS_PER_MINUTE;
    }

    private void recordRequest() {
        requestTimestamps.add(System.currentTimeMillis());
    }

    private void pruneOldRequests() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek() < cutoff) {
            requestTimestamps.poll();
        }
    }

    public void clearCache() {
        bedwarsLevelCache.clear();
        hypixelLevelCache.clear();
        pending.clear();
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private static Integer parseHypixelLevel(JsonObject player) {
        if (player == null) return null;

        if (player.has("networkLevel") && !player.get("networkLevel").isJsonNull()) {
            return (int) Math.floor(player.get("networkLevel").getAsDouble());
        }

        if (!player.has("networkExp") || player.get("networkExp").isJsonNull()) {
            return null;
        }

        double exp = player.get("networkExp").getAsDouble();
        double level = (Math.sqrt((2.0D * exp) + 30625.0D) - 175.0D) / 50.0D;
        return Math.max(0, (int) Math.floor(level));
    }
}
