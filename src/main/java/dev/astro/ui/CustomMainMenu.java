package dev.astro.ui;

import dev.astro.AstroClient;
import dev.astro.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
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
    private static final int SINGLE=1, MULTI=2, OPTIONS=4, QUIT=5, MODS=6;

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

        // Small utility row along the bottom, like the four Lunar shortcuts.
        int tile = 42, gap = 10;
        int utilityX = width / 2 - 115;
        int utilityY = height - 53;
        buttonList.add(new FlatButton(OPTIONS, utilityX, utilityY, tile, tile, "⚙"));
        buttonList.add(new FlatButton(MODS, utilityX + tile + gap, utilityY, tile, tile, "F"));
        buttonList.add(new FlatButton(QUIT, utilityX + (tile + gap) * 2, utilityY, tile, tile, "×"));

        int clientW=Math.min(230,Math.max(150,width/4));
        buttonList.add(new FlatButton(MODS,width-clientW-18,height-116,clientW,100,""));
    }

    @Override public void drawScreen(int mx,int my,float partialTicks) {
        super.drawScreen(mx,my,partialTicks);
        // Softly darken the panorama without changing its composition.
        drawRect(0,0,width,height,0x24000000);

        String brand="FIRED";
        GlStateManager.pushMatrix();
        float scale = Math.min(3.2F, Math.max(2.3F, width / 350.0F));
        GlStateManager.scale(scale,scale,1.0F);
        int logoX = (int)(width/(2.0F*scale)-fontRendererObj.getStringWidth(brand)/2.0F);
        int logoY = (int)(height*0.105F/scale);
        fontRendererObj.drawStringWithShadow(brand,logoX,logoY,WHITE);
        GlStateManager.popMatrix();

        String subtitle="MINECRAFT 1.8.9";
        fontRendererObj.drawStringWithShadow(subtitle,
                width/2-fontRendererObj.getStringWidth(subtitle)/2,
                (int)(height*0.105F)+48,MUTED);
        RenderUtil.drawGradientH(width/2-72,(int)(height*0.105F)+40,
                width/2+72,(int)(height*0.105F)+42,0x00FF4A16,ORANGE);

        // Large lower-right client tile occupies the same visual anchor as Lunar's.
        int tileW=Math.min(230,Math.max(150,width/4));
        int tileH=100, tx=width-tileW-18, ty=height-tileH-16;
        boolean over=mx>=tx&&mx<tx+tileW&&my>=ty&&my<ty+tileH;
        RenderUtil.drawRoundedRect(tx,ty,tx+tileW,ty+tileH,7,over?0xE5452420:0xD51A1720);
        RenderUtil.drawOutline(tx,ty,tx+tileW,ty+tileH,over?0xB5FF4A16:0x55777777);
        String mark="F";
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.2F,2.2F,1F);
        fontRendererObj.drawStringWithShadow(mark,
                (tx+tileW/2-fontRendererObj.getStringWidth(mark)*1.1F)/2F,
                (ty+22)/2F,ORANGE);
        GlStateManager.popMatrix();
        fontRendererObj.drawStringWithShadow("FIRED CLIENT",
                tx+(tileW-fontRendererObj.getStringWidth("FIRED CLIENT"))/2,
                ty+64,WHITE);
        fontRendererObj.drawStringWithShadow("1.8.9  •  v"+AstroClient.VERSION,
                tx+(tileW-fontRendererObj.getStringWidth("1.8.9  •  v"+AstroClient.VERSION))/2,
                ty+78,MUTED);

        fontRendererObj.drawStringWithShadow("FIRED CLIENT  /  v"+AstroClient.VERSION,
                10,height-12,0xFFB5B5BC);
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException {
        switch(b.id){
            case SINGLE: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
            case MULTI: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
            case MODS: mc.displayGuiScreen(new ClickGUI()); break;
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
            int color=hovered?WHITE:0xFFE0E0E0;
            mc.fontRendererObj.drawStringWithShadow(displayString,
                    xPosition+(width-mc.fontRendererObj.getStringWidth(displayString))/2,
                    yPosition+(height-8)/2,color);
        }
    }
}
