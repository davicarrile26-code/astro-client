package eu.ezclient.forge;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Forge 1.8.9 event facade for the ezclient legacy port.
 *
 * This deliberately exposes Minecraft/Forge events behind small listener
 * interfaces so feature code can be migrated without importing Fabric API.
 */
public final class ForgeClientEvents {
    public interface TickListener { void onClientTick(Minecraft minecraft); }
    public interface HudListener { void onHudRender(Minecraft minecraft, float partialTicks); }
    public interface KeyListener { void onKey(int keyCode, boolean pressed); }
    public interface MouseListener { void onMouse(int button, boolean pressed); }
    public interface WorldListener { void onWorldJoin(EntityJoinWorldEvent event); }

    private static final List<TickListener> TICKS = new CopyOnWriteArrayList<TickListener>();
    private static final List<HudListener> HUD = new CopyOnWriteArrayList<HudListener>();
    private static final List<KeyListener> KEYS = new CopyOnWriteArrayList<KeyListener>();
    private static final List<MouseListener> MOUSE = new CopyOnWriteArrayList<MouseListener>();
    private static final List<WorldListener> WORLDS = new CopyOnWriteArrayList<WorldListener>();

    public static void addTickListener(TickListener listener) { if (listener != null) TICKS.add(listener); }
    public static void addHudListener(HudListener listener) { if (listener != null) HUD.add(listener); }
    public static void addKeyListener(KeyListener listener) { if (listener != null) KEYS.add(listener); }
    public static void addMouseListener(MouseListener listener) { if (listener != null) MOUSE.add(listener); }
    public static void addWorldListener(WorldListener listener) { if (listener != null) WORLDS.add(listener); }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        for (TickListener listener : TICKS) {
            try { listener.onClientTick(minecraft); }
            catch (Throwable error) { error.printStackTrace(); }
        }
    }

    @SubscribeEvent
    public void onHudRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        for (HudListener listener : HUD) {
            try { listener.onHudRender(minecraft, event.partialTicks); }
            catch (Throwable error) { error.printStackTrace(); }
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_NONE) return;
        boolean pressed = Keyboard.getEventKeyState();
        for (KeyListener listener : KEYS) {
            try { listener.onKey(key, pressed); }
            catch (Throwable error) { error.printStackTrace(); }
        }
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        int button = Mouse.getEventButton();
        if (button < 0) return;
        boolean pressed = Mouse.getEventButtonState();
        for (MouseListener listener : MOUSE) {
            try { listener.onMouse(button, pressed); }
            catch (Throwable error) { error.printStackTrace(); }
        }
    }

    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent event) {
        if (event.entity != Minecraft.getMinecraft().thePlayer) return;
        for (WorldListener listener : WORLDS) {
            try { listener.onWorldJoin(event); }
            catch (Throwable error) { error.printStackTrace(); }
        }
    }
}
