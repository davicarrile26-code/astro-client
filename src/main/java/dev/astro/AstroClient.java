package dev.astro;

import dev.astro.command.ApiKeyCommand;
import dev.astro.event.EventBus;
import dev.astro.module.ModuleManager;
import dev.astro.ui.HUD;
import dev.astro.util.AstroUsers;
import dev.astro.util.ConfigManager;
import dev.astro.util.HypixelAPI;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileWriter;

/**
 * FIRED Client — a Forge 1.8.9 client with a modular HUD and ClickGUI.
 * The legacy Java package and mod id are retained for compatibility.
 */
@Mod(modid = AstroClient.MOD_ID, name = AstroClient.NAME, version = AstroClient.VERSION)
public class AstroClient {

    public static final String MOD_ID  = "firedclient";
    public static final String NAME    = "FIRED Client";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static AstroClient INSTANCE;

    private EventBus eventBus;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private HypixelAPI hypixelAPI;
    private AstroUsers astroUsers;
    private HUD hud;

    public static KeyBinding keyOpenGui;
    public static KeyBinding keyHudEditor;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        writeSplashProperties(event.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("[" + NAME + "] Initialising v" + VERSION + "...");

        eventBus = new EventBus();
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        hypixelAPI = new HypixelAPI();
        hud = new HUD();

        eventBus.register(hud);
        moduleManager.init();

        configManager.init();
        configManager.load();

        hypixelAPI.init();

        astroUsers = new AstroUsers();
        astroUsers.init();

        ClientCommandHandler.instance.registerCommand(new ApiKeyCommand());

        keyOpenGui = new KeyBinding("Open FIRED Client GUI", Keyboard.KEY_RSHIFT, "FIRED Client");
        keyHudEditor = new KeyBinding("FIRED HUD Editor", Keyboard.KEY_H, "FIRED Client");
        ClientRegistry.registerKeyBinding(keyOpenGui);
        ClientRegistry.registerKeyBinding(keyHudEditor);

        MinecraftForge.EVENT_BUS.register(new dev.astro.event.ForgeEventBridge());

        System.out.println("[" + NAME + "] Loaded " +
                moduleManager.getModules().size() + " modules.");
    }

    public EventBus getEventBus() { return eventBus; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public HypixelAPI getHypixelAPI() { return hypixelAPI; }
    public AstroUsers getAstroUsers() { return astroUsers; }
    public HUD getHud() { return hud; }

    private void writeSplashProperties(File configDir) {
        File splash = new File(configDir, "splash.properties");
        if (splash.exists()) return;

        try {
            if (!configDir.exists()) configDir.mkdirs();
            try (FileWriter fw = new FileWriter(splash)) {
                fw.write("# FIRED Client loading screen\n");
                fw.write("background=0x080604\n");
                fw.write("font=0xFFFFFF\n");
                fw.write("barBackground=0x24110D\n");
                fw.write("barBorder=0x7A2B12\n");
                fw.write("barColor=0xFF4A00\n");
                fw.write("logoOffset=0\n");
                fw.write("rotate=true\n");
            }
            System.out.println("[" + NAME + "] Wrote splash.properties (takes effect next launch)");
        } catch (Exception ignored) {
            // Non-critical.
        }
    }
}
