package dev.astro.module.misc;

import com.google.gson.JsonObject;
import dev.astro.event.EventTarget;
import dev.astro.event.events.KeyEvent;
import dev.astro.module.Category;
import dev.astro.module.Module;
import dev.astro.util.ConfigManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/**
 * Sends a configurable quick-chat line when the configured hotkey is pressed.
 * Enable the module in the Mods screen; default hotkey is G.
 */
public final class AutoText extends Module implements ConfigManager.Configurable {
    private String message = "gg";
    private int triggerKey = Keyboard.KEY_G;

    public AutoText() {
        super("AutoText", "Sends a saved chat message with a hotkey.", Category.MISC);
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        if (event.getKeyCode() != triggerKey) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) return;
        if (message != null && !message.trim().isEmpty()) {
            mc.thePlayer.sendChatMessage(message);
        }
    }

    public String getMessage() { return message; }
    public void setMessage(String message) {
        if (message == null) return;
        String clean = message.trim();
        if (clean.length() > 100) clean = clean.substring(0, 100);
        this.message = clean;
        save();
    }

    public int getTriggerKey() { return triggerKey; }
    public void setTriggerKey(int key) {
        if (key > Keyboard.KEY_NONE && key < Keyboard.KEYBOARD_SIZE) {
            triggerKey = key;
            save();
        }
    }

    private void save() {
        ConfigManager cfg = dev.astro.AstroClient.INSTANCE.getConfigManager();
        if (cfg != null && !cfg.isLoading()) cfg.save();
    }

    @Override
    public void saveConfig(JsonObject obj) {
        obj.addProperty("message", message);
        obj.addProperty("triggerKey", triggerKey);
    }

    @Override
    public void loadConfig(JsonObject obj) {
        if (obj.has("message")) message = obj.get("message").getAsString();
        if (obj.has("triggerKey")) triggerKey = obj.get("triggerKey").getAsInt();
    }
}
