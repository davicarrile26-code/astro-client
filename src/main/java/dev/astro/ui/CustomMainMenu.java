package dev.astro.ui;

import dev.astro.AstroClient;
import dev.astro.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import java.io.IOException;

/**
 * FIRED home screen matching the Lunar reference composition:
 * centered logo, two stacked primary buttons, bottom utility tiles,
 * and a large client tile in the lower-right corner.
 */
public final class CustomMainMenu extends GuiMainMenu {
    private static final int ORANGE = 0xFFFF4A16;
    private static final int WHITE = 0xFFF4F4F6;
    private static final int MUTED = 0xFFB5B5BC;
    private static final int BTN = 0xB52B3036;
    private static final int HOVER = 0xE5454B52;
    private static final int SINGLE=1, MULTI=2, OPTIONS=4, QUIT=5, MODS=6, LANGUAGE=7;
    private static final ResourceLocation FIRED_ICON = new ResourceLocation("firedclient", "textures/gui/fired.png");

    @Override public void initGui() {
        super.initGui();
        buttonList.clear();

        // The reference uses one centered vertical stack, not a button grid.
        int primaryW = Math.min(444, Math.max(260, width - 40));
        int primaryH = 38;
        int primaryX = (width - primaryW) / 2;
        int primaryY = Math.max(210, height * 41 / 100);
        buttonList.add(new FlatButton(SINGLE, primaryX, primaryY, primaryW, primaryH, "S I N G L E P L A Y E R"));
        buttonList.add(new FlatButton(MULTI, primaryX, primaryY + 50, primaryW, primaryH, "M U L T I P L A Y E R"));

        // Four compact shortcut tiles along the bottom, following Lunar's menu rhythm.
        int tile = 42, gap = 10;
        int utilityX = width / 2 - (tile * 2 + gap * 3);
        int utilityY = height - 58;
        buttonList.add(new FlatButton(OPTIONS, utilityX, utilityY, tile, tile, "⚙"));
        buttonList.add(new FlatButton(LANGUAGE, utilityX + tile + gap, utilityY, tile, tile, "文"));
        buttonList.add(new FlatButton(MODS, utilityX + (tile + gap) * 2, utilityY, tile, tile, "F"));
        buttonList.add(new FlatButton(QUIT, utilityX + (tile + gap) * 3, utilityY, tile, tile, "×"));
    }

    @Override public void drawScreen(int mx,int my,float partialTicks) {
        super.drawScreen(mx,my,partialTicks);
        // Softly darken the panorama without changing its composition.
        drawRect(0,0,width,height,0x24000000);

        // FIRED icon and wordmark replace Lunar branding; keep the Minecraft panorama.
        int logoSize = Math.min(58, Math.max(40, width / 18));
        int logoX = width / 2 - logoSize / 2;
        int logoY = Math.max(28, (int)(height * 0.075F));
        mc.getTextureManager().bindTexture(FIRED_ICON);
        drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoSize, logoSize, 256, 256);
        String brand="FIRED CLIENT";
        int brandY=logoY+logoSize+7;
        fontRendererObj.drawStringWithShadow(brand, width/2-fontRendererObj.getStringWidth(brand)/2, brandY, WHITE);
        String subtitle="MINECRAFT 1.8.9";
        fontRendererObj.drawStringWithShadow(subtitle, width/2-fontRendererObj.getStringWidth(subtitle)/2, brandY+15, MUTED);
        RenderUtil.drawGradientH(width/2-72,brandY+29,width/2+72,brandY+31,0x00FF4A16,ORANGE);

        // Lower-right FIRED client tile.
        int tileW=Math.min(230,Math.max(150,width/4));
        int tileH=76, tx=width-tileW-18, ty=height-tileH-16;
        boolean over=mx>=tx&&mx<tx+tileW&&my>=ty&&my<ty+tileH;
        RenderUtil.drawRoundedRect(tx,ty,tx+tileW,ty+tileH,7,over?0xE5452420:0xD51A1720);
        RenderUtil.drawOutline(tx,ty,tx+tileW,ty+tileH,over?0xB5FF4A16:0x55777777);
        mc.getTextureManager().bindTexture(FIRED_ICON);
        drawModalRectWithCustomSizedTexture(tx+12,ty+14,0,0,46,46,256,256);
        fontRendererObj.drawStringWithShadow("FIRED CLIENT",tx+68,ty+23,WHITE);
        fontRendererObj.drawStringWithShadow("1.8.9  -  v"+AstroClient.VERSION,tx+68,ty+39,MUTED);

        // Cover vanilla/Forge version and branding lines drawn by GuiMainMenu
        // in the lower-left corner, then show a single FIRED status label.
        RenderUtil.drawRoundedRect(8, height - 27, 190, height - 5, 5, 0xE5101117);
        RenderUtil.drawOutline(8, height - 27, 190, height - 5, 0x55FF4A16);
        fontRendererObj.drawStringWithShadow("FIRED CLIENT  /  v"+AstroClient.VERSION,
                17,height-19,0xFFF0F0F2);
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException {
        switch(b.id){
            case SINGLE: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
            case MULTI: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
            case MODS: mc.displayGuiScreen(new ClickGUI()); break;
            case OPTIONS: mc.displayGuiScreen(new GuiOptions(this,mc.gameSettings)); break;
            case LANGUAGE: mc.displayGuiScreen(new GuiLanguage(this,mc.gameSettings,mc.getLanguageManager())); break;
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
            int color=hovered?WHITE:0xFFE0E0E0;
            mc.fontRendererObj.drawStringWithShadow(displayString,
                    xPosition+(width-mc.fontRendererObj.getStringWidth(displayString))/2,
                    yPosition+(height-8)/2,color);
        }
    }
}
