package dev.astro.engine;

import dev.astro.event.EventBus;
import dev.astro.module.ModuleManager;
import dev.astro.ui.HUD;
import dev.astro.util.ConfigManager;

/**
 * FIRED's internal client runtime.
 *
 * This is the first extraction step toward a standalone FIRED platform:
 * it owns FIRED services and their startup order, while the current
 * Minecraft/Forge integration remains in the bootstrap adapter.
 *
 * Do not remove Forge until a replacement Minecraft bootstrap and game
 * hooks exist; this runtime alone is not a Forge replacement yet.
 */
public final class FiredEngine {

    public enum State {
        NEW, STARTING, RUNNING, FAILED
    }

    private State state = State.NEW;
    private EventBus eventBus;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private HUD hud;

    public synchronized void start() {
        if (state == State.RUNNING) return;
        if (state != State.NEW) {
            throw new IllegalStateException("FIRED Engine cannot start from state " + state);
        }

        state = State.STARTING;
        try {
            eventBus = new EventBus();
            moduleManager = new ModuleManager();
            configManager = new ConfigManager();
            hud = new HUD();

            eventBus.register(hud);
            moduleManager.init();

            configManager.init();
            configManager.load();

            state = State.RUNNING;
            System.out.println("[FIRED Engine] Runtime started with "
                    + moduleManager.getModules().size() + " modules.");
        } catch (RuntimeException | Error failure) {
            state = State.FAILED;
            throw failure;
        }
    }

    public State getState() { return state; }
    public EventBus getEventBus() { return eventBus; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public HUD getHud() { return hud; }
}
