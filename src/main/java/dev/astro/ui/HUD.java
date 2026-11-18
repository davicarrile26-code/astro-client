package dev.astro.ui;

import dev.astro.event.EventTarget;
import dev.astro.event.events.Render2DEvent;

/**
 * FIRED HUD event root.
 * Individual HUD modules render their own elements. The old permanent
 * "FIRED Client" watermark has intentionally been removed.
 */
public final class HUD {
    @EventTarget
    public void onRender(Render2DEvent event) {
        // HUD elements are owned by enabled render modules.
    }
}
