package eu.ezclient.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

/**
 * Entry point for the Forge-backed compatibility layer.
 *
 * The upstream ezclient feature classes are not copied into this repository
 * yet. Once ported, EzClient's initialization will register its callbacks
 * through this adapter rather than Fabric's ClientModInitializer/API.
 */
public final class ForgeClientAdapter {
    private static boolean initialized;

    private ForgeClientAdapter() {}

    public static synchronized void initialize() {
        if (initialized) return;
        if (Minecraft.getMinecraft() == null) {
            throw new IllegalStateException("Minecraft client is not available yet");
        }
        MinecraftForge.EVENT_BUS.register(new ForgeClientEvents());
        initialized = true;
    }

    public static void registerKeyBinding(KeyBinding keyBinding) {
        if (keyBinding == null) throw new IllegalArgumentException("keyBinding");
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
