package dev.astro.util;

import com.google.gson.*;
import dev.astro.AstroClient;
import dev.astro.module.HUDModule;
import dev.astro.module.Module;
import dev.astro.module.setting.Setting;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.List;

/** Persists module state in .minecraft/firedclient/config.json. */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private File configDir;
    private File configFile;
    private File legacyConfigFile;
    private boolean loading;

    public void init() {
        File mcDir = Minecraft.getMinecraft().mcDataDir;
        configDir = new File(mcDir, "firedclient");
        configFile = new File(configDir, "config.json");
        legacyConfigFile = new File(new File(mcDir, "astroclient"), "config.json");
        if (!configDir.exists()) configDir.mkdirs();
    }

    public boolean isLoading() { return loading; }

    public void load() {
        File source = configFile.exists() ? configFile :
                (legacyConfigFile.exists() ? legacyConfigFile : configFile);
        if (!source.exists()) return;

        loading = true;
        boolean needsSave = !source.equals(configFile);
        try (Reader reader = new FileReader(source)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();

            for (Module mod : AstroClient.INSTANCE.getModuleManager().getModules()) {
                if (!root.has(mod.getName())) continue;
                JsonObject obj = root.getAsJsonObject(mod.getName());

                if (obj.has("keyBind")) {
                    int keyBind = obj.get("keyBind").getAsInt();
                    mod.setKeyBind(keyBind);
                    if (mod.getKeyBind() != keyBind) needsSave = true;
                }
                if (obj.has("visible")) mod.setVisible(obj.get("visible").getAsBoolean());

                if (mod instanceof HUDModule) {
                    HUDModule hud = (HUDModule) mod;
                    if (obj.has("renderX")) hud.setRenderX(obj.get("renderX").getAsInt());
                    if (obj.has("renderY")) hud.setRenderY(obj.get("renderY").getAsInt());
                }

                if (obj.has("settings")) {
                    JsonObject settingsObj = obj.getAsJsonObject("settings");
                    for (Setting<?> setting : mod.getSettings()) {
                        if (settingsObj.has(setting.getName())) {
                            setting.fromJson(settingsObj.get(setting.getName()));
                        }
                    }
                }

                if (mod instanceof Configurable) ((Configurable) mod).loadConfig(obj);

                if (mod.shouldPersistEnabledState() && obj.has("enabled")
                        && obj.get("enabled").getAsBoolean()) {
                    mod.setEnabled(true);
                }
            }

            System.out.println("[FIRED Client] Config loaded.");
        } catch (Exception e) {
            System.err.println("[FIRED Client] Failed to load config: " + e.getMessage());
        } finally {
            loading = false;
        }

        if (needsSave) save();
    }

    public void save() {
        JsonObject root = new JsonObject();

        for (Module mod : AstroClient.INSTANCE.getModuleManager().getModules()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", mod.shouldPersistEnabledState() && mod.isEnabled());
            obj.addProperty("keyBind", mod.getKeyBind());
            obj.addProperty("visible", mod.isVisible());

            if (mod instanceof HUDModule) {
                HUDModule hud = (HUDModule) mod;
                obj.addProperty("renderX", hud.getRenderX());
                obj.addProperty("renderY", hud.getRenderY());
            }

            if (mod.hasSettings()) {
                JsonObject settingsObj = new JsonObject();
                for (Setting<?> setting : mod.getSettings()) {
                    settingsObj.add(setting.getName(), setting.toJson());
                }
                obj.add("settings", settingsObj);
            }

            if (mod instanceof Configurable) ((Configurable) mod).saveConfig(obj);
            root.add(mod.getName(), obj);
        }

        try (Writer writer = new FileWriter(configFile)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            System.err.println("[FIRED Client] Failed to save config: " + e.getMessage());
        }
    }

    public interface Configurable {
        void saveConfig(JsonObject obj);
        void loadConfig(JsonObject obj);
    }
}
