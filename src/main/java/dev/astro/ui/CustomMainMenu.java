package dev.astro.ui;

import dev.astro.AstroClient;
import dev.astro.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import java.io.IOException;

/**
 * Forge 1.8.9 port of the Lunar main-menu composition.
 *
 * The original Lunar screen classes depend on Lunar's Lcui/Bridge runtime,
 * which is not present in this Forge-only client. This screen recreates the
 * same home-screen layout and native Minecraft actions without those APIs.
 */
public final class CustomMainMenu extends GuiMainMenu {
    private static final int ORANGE = 0xFFFF4A16;
    private static final int WHITE = 0xFFF4F4F6;
    private static final int MUTED = 0xFFB5B5BC;
    private static final int BTN = 0xB52B3036;
    private static final int HOVER = 0xE5454B52;
    private static final int SINGLE=1, MULTI=2, OPTIONS=4, QUIT=5, MODS=6, COSMETICS=8;
    private static final ResourceLocation FIRED_ICON = new ResourceLocation("firedclient", "textures/gui/fired.png");

    @Override public void initGui() {
        super.initGui();
        buttonList.clear();

        // Main Lunar-style composition: two wide centered buttons.
        int primaryW = Math.min(396, Math.max(250, width - 48));
        int primaryH = 34;
        int primaryX = (width - primaryW) / 2;
        int primaryY = Math.max(150, Math.min(height - 150, height * 465 / 1000));
        buttonList.add(new FlatButton(SINGLE, primaryX, primaryY, primaryW, primaryH,
                "S I N G L E P L A Y E R"));
        buttonList.add(new FlatButton(MULTI, primaryX, primaryY + 47, primaryW, primaryH,
                "M U L T I P L A Y E R"));

        // Four small utility icons, centered along the bottom edge.
        int tile = 36, gap = 7;
        int total = tile * 4 + gap * 3;
        int utilityX = (width - total) / 2;
        int utilityY = height - 49;
        buttonList.add(new FlatButton(OPTIONS, utilityX, utilityY, tile, tile, ""));
        buttonList.add(new FlatButton(COSMETICS, utilityX + tile + gap, utilityY, tile, tile, ""));
        buttonList.add(new FlatButton(MODS, utilityX + (tile + gap) * 2, utilityY, tile, tile, ""));
        buttonList.add(new FlatButton(QUIT, utilityX + (tile + gap) * 3, utilityY, tile, tile, ""));
    }

    @Override public void drawScreen(int mx,int my,float partialTicks) {
        super.drawScreen(mx,my,partialTicks);
        drawRect(0,0,width,height,0x24000000);

        // Replace only Lunar's mark and wordmark with FIRED branding.
        int logoSize = Math.min(116, Math.max(72, width / 7));
        int logoX = width / 2 - logoSize / 2;
        int logoY = Math.max(22, height / 11);
        mc.getTextureManager().bindTexture(FIRED_ICON);
        drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoSize, logoSize, 256, 256);
        String brand = "FIRED CLIENT";
        fontRendererObj.drawStringWithShadow(brand,
                width / 2 - fontRendererObj.getStringWidth(brand) / 2,
                logoY + logoSize + 17, WHITE);

        // Bottom-right client tile mirrors the reference's launcher tile.
        int tileW = Math.min(204, Math.max(160, width / 4));
        int tileH = 88;
        int tx = width - tileW - 14;
        int ty = height - tileH - 14;
        boolean over = mx >= tx && mx < tx + tileW && my >= ty && my < ty + tileH;
        RenderUtil.drawRoundedRect(tx,ty,tx+tileW,ty+tileH,6,
                over ? 0xE52C1830 : 0xD51A1720);
        RenderUtil.drawOutline(tx,ty,tx+tileW,ty+tileH,
                over ? 0xB5FF4A16 : 0x55777777);
        mc.getTextureManager().bindTexture(FIRED_ICON);
        drawModalRectWithCustomSizedTexture(tx + 13, ty + 17, 0, 0, 48, 48, 256, 256);
        fontRendererObj.drawStringWithShadow("FIRED CLIENT",
                tx + 67, ty + 30, WHITE);
        fontRendererObj.drawStringWithShadow("1.8.9  -  v" + AstroClient.VERSION,
                tx + 67, ty + 47, MUTED);

        // Version and copyright captions at the two lower corners.
        fontRendererObj.drawStringWithShadow("FIRED CLIENT 1.8.9",
                12, height - 13, MUTED);
        String copyright = "Copyright Mojang AB. Do not distribute!";
        fontRendererObj.drawStringWithShadow(copyright,
                width - fontRendererObj.getStringWidth(copyright) - 12,
                height - 13, MUTED);
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException {
        switch(b.id){
            case SINGLE: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
            case MULTI: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
            case MODS: mc.displayGuiScreen(new ClickGUI()); break;
            case COSMETICS: mc.displayGuiScreen(new CosmeticsGUI(this)); break;
            case OPTIONS: mc.displayGuiScreen(new GuiOptions(this,mc.gameSettings)); break;
            case QUIT: mc.shutdown(); break;
            default: super.actionPerformed(b);
        }
    }

    private static class FlatButton extends GuiButton {
        FlatButton(int id,int x,int y,int w,int h,String text){super(id,x,y,w,h,text);}
        @Override public void drawButton(Minecraft mc,int mx,int my){
            if(!visible)return;
            hovered=mx>=xPosition&&my>=yPosition&&mx<xPosition+width&&my<yPosition+height;
            int bg=hovered?HOVER:BTN;
            RenderUtil.drawRoundedRect(xPosition,yPosition,xPosition+width,yPosition+height,5,bg);
            RenderUtil.drawOutline(xPosition,yPosition,xPosition+width,yPosition+height,
                    hovered?0x90FF4A16:0x40777777);
            int color=hovered?0xFFFF6A32:0xFFF0F0F2;
            if (id == SINGLE || id == MULTI) {
                String label = displayString;
                int textWidth = mc.fontRendererObj.getStringWidth(label);
                mc.fontRendererObj.drawString(label,
                        xPosition + (width - textWidth) / 2,
                        yPosition + (height - 8) / 2,
                        color, false);
                return;
            }
            drawShortcutIcon(mc, xPosition + width / 2, yPosition + height / 2, color);
        }

        private void drawShortcutIcon(Minecraft mc, int cx, int cy, int color) {
            if (id == MODS) {
                mc.getTextureManager().bindTexture(FIRED_ICON);
                Gui.drawModalRectWithCustomSizedTexture(cx-12, cy-12, 0, 0, 24, 24, 256, 256);
                return;
            }
            org.lwjgl.opengl.GL11.glPushMatrix();
            org.lwjgl.opengl.GL11.glLineWidth(2.0F);
            org.lwjgl.opengl.GL11.glColor4f(
                    ((color >> 16) & 255) / 255.0F,
                    ((color >> 8) & 255) / 255.0F,
                    (color & 255) / 255.0F, 1.0F);
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_LINE_SMOOTH);
            if (id == OPTIONS) {
                // Clean settings gear: circular hub with eight short teeth.
                org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_LINES);
                for (int i=0;i<8;i++) {
                    double a=Math.PI*2*i/8.0;
                    float x1=cx+(float)Math.cos(a)*8.0F;
                    float y1=cy+(float)Math.sin(a)*8.0F;
                    float x2=cx+(float)Math.cos(a)*11.0F;
                    float y2=cy+(float)Math.sin(a)*11.0F;
                    org.lwjgl.opengl.GL11.glVertex2f(x1,y1);
                    org.lwjgl.opengl.GL11.glVertex2f(x2,y2);
                }
                org.lwjgl.opengl.GL11.glEnd();
                circle(cx,cy,7,org.lwjgl.opengl.GL11.GL_LINE_LOOP);
                circle(cx,cy,2.5F,org.lwjgl.opengl.GL11.GL_LINE_LOOP);
            } else if (id == LANGUAGE) {
                // Globe with latitude and longitude lines.
                circle(cx,cy,10,org.lwjgl.opengl.GL11.GL_LINE_LOOP);
                org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_LINES);
                org.lwjgl.opengl.GL11.glVertex2f(cx-9,cy);
                org.lwjgl.opengl.GL11.glVertex2f(cx+9,cy);
                org.lwjgl.opengl.GL11.glVertex2f(cx,cy-9);
                org.lwjgl.opengl.GL11.glVertex2f(cx,cy+9);
                org.lwjgl.opengl.GL11.glEnd();
                org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_LINE_LOOP);
                for(int i=0;i<32;i++){
                    double a=Math.PI*2*i/32.0;
                    org.lwjgl.opengl.GL11.glVertex2f(cx+(float)Math.cos(a)*4.5F,
                            cy+(float)Math.sin(a)*9.0F);
                }
                org.lwjgl.opengl.GL11.glEnd();
            } else if (id == COSMETICS) {
                // Shirt/clothing symbol used for the cosmetics shortcut, in the familiar client style.
                org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_LINE_LOOP);
                org.lwjgl.opengl.GL11.glVertex2f(cx-5,cy-9);
                org.lwjgl.opengl.GL11.glVertex2f(cx-10,cy-5);
                org.lwjgl.opengl.GL11.glVertex2f(cx-7,cy+1);
                org.lwjgl.opengl.GL11.glVertex2f(cx-4,cy-1);
                org.lwjgl.opengl.GL11.glVertex2f(cx-4,cy+9);
                org.lwjgl.opengl.GL11.glVertex2f(cx+4,cy+9);
                org.lwjgl.opengl.GL11.glVertex2f(cx+4,cy-1);
                org.lwjgl.opengl.GL11.glVertex2f(cx+7,cy+1);
                org.lwjgl.opengl.GL11.glVertex2f(cx+10,cy-5);
                org.lwjgl.opengl.GL11.glVertex2f(cx+5,cy-9);
                org.lwjgl.opengl.GL11.glVertex2f(cx+2,cy-6);
                org.lwjgl.opengl.GL11.glVertex2f(cx-2,cy-6);
                org.lwjgl.opengl.GL11.glEnd();
                org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_LINES);
                org.lwjgl.opengl.GL11.glVertex2f(cx-2,cy-6);
                org.lwjgl.opengl.GL11.glVertex2f(cx,cy-2);
                org.lwjgl.opengl.GL11.glVertex2f(cx+2,cy-6);
                org.lwjgl.opengl.GL11.glVertex2f(cx,cy-2);
                org.lwjgl.opengl.GL11.glEnd();
            } else if (id == QUIT) {
                org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_LINES);
                org.lwjgl.opengl.GL11.glVertex2f(cx-7,cy-7);
                org.lwjgl.opengl.GL11.glVertex2f(cx+7,cy+7);
                org.lwjgl.opengl.GL11.glVertex2f(cx+7,cy-7);
                org.lwjgl.opengl.GL11.glVertex2f(cx-7,cy+7);
                org.lwjgl.opengl.GL11.glEnd();
            }
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_LINE_SMOOTH);
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
            org.lwjgl.opengl.GL11.glColor4f(1,1,1,1);
            org.lwjgl.opengl.GL11.glPopMatrix();
        }

        private void circle(float cx,float cy,float radius,int mode) {
            org.lwjgl.opengl.GL11.glBegin(mode);
            for(int i=0;i<48;i++){
                double a=Math.PI*2*i/48.0;
                org.lwjgl.opengl.GL11.glVertex2f(cx+(float)Math.cos(a)*radius,
                        cy+(float)Math.sin(a)*radius);
            }
            org.lwjgl.opengl.GL11.glEnd();
        }
    }
}
