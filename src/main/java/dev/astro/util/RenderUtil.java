package dev.astro.util;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Static drawing helpers.  Every method cleans up GL state.
 */
public final class RenderUtil {

    private RenderUtil() {}

    /** Solid filled rect with alpha blending. */
    public static void drawRect(float x1, float y1, float x2, float y2, int colour) {
        if (x1 > x2) { float t = x1; x1 = x2; x2 = t; }
        if (y1 > y2) { float t = y1; y1 = y2; y2 = t; }

        float a = (colour >> 24 & 0xFF) / 255f;
        float r = (colour >> 16 & 0xFF) / 255f;
        float g = (colour >>  8 & 0xFF) / 255f;
        float b = (colour       & 0xFF) / 255f;

        Tessellator   tes = Tessellator.getInstance();
        WorldRenderer wr  = tes.getWorldRenderer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(x1, y2, 0).endVertex();
        wr.pos(x2, y2, 0).endVertex();
        wr.pos(x2, y1, 0).endVertex();
        wr.pos(x1, y1, 0).endVertex();
        tes.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }

    /** 1-px outline rectangle. */
    public static void drawOutline(float x1, float y1, float x2, float y2, int colour) {
        drawRect(x1, y1, x2, y1 + 1, colour);
        drawRect(x1, y2 - 1, x2, y2, colour);
        drawRect(x1, y1, x1 + 1, y2, colour);
        drawRect(x2 - 1, y1, x2, y2, colour);
    }

    /** Smooth rounded rectangle rendered with segmented OpenGL arcs. */
    public static void drawRoundedRect(float x1, float y1, float x2, float y2,
                                       float radius, int colour) {
        if (x2 <= x1 || y2 <= y1) return;
        float r = Math.max(0f, Math.min(radius, Math.min(x2-x1, y2-y1)/2f));
        if (r <= 0f) { drawRect(x1, y1, x2, y2, colour); return; }
        float alpha=(colour >>> 24 & 255)/255f;
        float red=(colour >>> 16 & 255)/255f;
        float green=(colour >>> 8 & 255)/255f;
        float blue=(colour & 255)/255f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f((x1+x2)/2f, (y1+y2)/2f);
        float[] cx={x2-r,x2-r,x1+r,x1+r};
        float[] cy={y1+r,y2-r,y2-r,y1+r};
        float[] start={-90f,0f,90f,180f};
        for(int corner=0;corner<4;corner++) {
            for(int i=0;i<=8;i++) {
                double angle=Math.toRadians(start[corner]+i*90f/8f);
                GL11.glVertex2f(cx[corner]+(float)Math.cos(angle)*r,
                                cy[corner]+(float)Math.sin(angle)*r);
            }
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f,1f,1f,1f);
    }

    /** Horizontal gradient rect (left colour → right colour). */
    public static void drawGradientH(float x1, float y1, float x2, float y2,
                                     int colourLeft, int colourRight) {
        float aL = (colourLeft >> 24 & 0xFF) / 255f;
        float rL = (colourLeft >> 16 & 0xFF) / 255f;
        float gL = (colourLeft >>  8 & 0xFF) / 255f;
        float bL = (colourLeft       & 0xFF) / 255f;

        float aR = (colourRight >> 24 & 0xFF) / 255f;
        float rR = (colourRight >> 16 & 0xFF) / 255f;
        float gR = (colourRight >>  8 & 0xFF) / 255f;
        float bR = (colourRight       & 0xFF) / 255f;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator   tes = Tessellator.getInstance();
        WorldRenderer wr  = tes.getWorldRenderer();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x1, y2, 0).color(rL, gL, bL, aL).endVertex();
        wr.pos(x2, y2, 0).color(rR, gR, bR, aR).endVertex();
        wr.pos(x2, y1, 0).color(rR, gR, bR, aR).endVertex();
        wr.pos(x1, y1, 0).color(rL, gL, bL, aL).endVertex();
        tes.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /** Scale the alpha channel of a colour. */
    private static int blendAlpha(int colour, float factor) {
        int a = (int) (((colour >> 24) & 0xFF) * factor);
        return (a << 24) | (colour & 0x00FFFFFF);
    }
}
