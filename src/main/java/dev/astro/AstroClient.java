package dev.astro;

import dev.astro.command.ApiKeyCommand;
import dev.astro.engine.FiredEngine;
import dev.astro.util.AstroUsers;
import dev.astro.util.HypixelAPI;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

/**
 * FIRED Client bootstrap adapter for the current Forge 1.8.9 runtime.
 * FIRED-owned services live in FiredEngine; Forge is currently only the
 * Minecraft launch/event integration and will remain until a replacement exists.
 */
@Mod(modid = AstroClient.MOD_ID, name = AstroClient.NAME, version = AstroClient.VERSION)
public class AstroClient {

    public static final String MOD_ID  = "firedclient";
    public static final String NAME    = "FIRED Client";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static AstroClient INSTANCE;

    private FiredEngine engine;
    private HypixelAPI hypixelAPI;
    private AstroUsers astroUsers;

    public static KeyBinding keyOpenGui;
    public static KeyBinding keyHudEditor;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("[" + NAME + "] Initialising v" + VERSION + "...");

        // Start FIRED's own runtime before attaching the Minecraft adapter.
        engine = new FiredEngine();
        engine.start();

        hypixelAPI = new HypixelAPI();
        hypixelAPI.init();

        astroUsers = new AstroUsers();
        astroUsers.init();

        ClientCommandHandler.instance.registerCommand(new ApiKeyCommand());

        keyOpenGui = new KeyBinding("Open FIRED Client GUI", Keyboard.KEY_RSHIFT, "FIRED Client");
        keyHudEditor = new KeyBinding("FIRED HUD Editor", Keyboard.KEY_H, "FIRED Client");
        ClientRegistry.registerKeyBinding(keyOpenGui);
        ClientRegistry.registerKeyBinding(keyHudEditor);

        // Temporary Forge adapter: replace only after FIRED has its own
        // LaunchWrapper/coremod bootstrap and complete game-hook coverage.
        MinecraftForge.EVENT_BUS.register(new dev.astro.event.ForgeEventBridge());

        System.out.println("[" + NAME + "] FIRED Engine is "
                + engine.getState() + ".");
    }

    public FiredEngine getEngine() { return engine; }
    public dev.astro.event.EventBus getEventBus() {
        return engine == null ? null : engine.getEventBus();
    }
    public dev.astro.module.ModuleManager getModuleManager() {
        return engine == null ? null : engine.getModuleManager();
    }
    public dev.astro.util.ConfigManager getConfigManager() {
        return engine == null ? null : engine.getConfigManager();
    }
    public HypixelAPI getHypixelAPI() { return hypixelAPI; }
    public AstroUsers getAstroUsers() { return astroUsers; }
    public dev.astro.ui.HUD getHud() {
        return engine == null ? null : engine.getHud();
    }
}
