package dev.astro.ui;

import dev.astro.AstroClient;
import dev.astro.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import java.io.IOException;

/** FIRED home screen: classic Minecraft/Lunar-style centered navigation. */
public final class CustomMainMenu extends GuiMainMenu {
    private static final int ORANGE = 0xFFFF4A16;
    private static final int WHITE = 0xFFF4F4F6;
    private static final int MUTED = 0xFFB5B5BC;
    private static final int BTN = 0xB018181D;
    private static final int HOVER = 0xE52B2422;
    private static final int BTN_W = 154, BTN_H = 28, GAP = 7;
    private static final int SINGLE=1, MULTI=2, MODS=3, OPTIONS=4, QUIT=5;

    @Override public void initGui() {
        super.initGui();
        buttonList.clear();
        int cx=width/2;
        int y=height/2+4;
        int left=cx-BTN_W-GAP/2;
        int right=cx+GAP/2;
        buttonList.add(new FlatButton(SINGLE,left,y,BTN_W,BTN_H,"Singleplayer"));
        buttonList.add(new FlatButton(MULTI,right,y,BTN_W,BTN_H,"Multiplayer"));
        buttonList.add(new FlatButton(MODS,left,y+BTN_H+GAP,BTN_W,BTN_H,"FIRED Mods"));
        buttonList.add(new FlatButton(OPTIONS,right,y+BTN_H+GAP,BTN_W,BTN_H,"Options"));
        buttonList.add(new FlatButton(QUIT,cx-74,y+2*(BTN_H+GAP),148,24,"Quit Game"));
    }

    @Override public void drawScreen(int mx,int my,float partialTicks) {
        super.drawScreen(mx,my,partialTicks);
        drawRect(0,0,width,height,0x10000000);
        String brand="FIRED";
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.7F,2.7F,1.0F);
        fontRendererObj.drawStringWithShadow(brand,
                width/5.4F-fontRendererObj.getStringWidth(brand)/2.0F,
                height/5.4F-12.0F,WHITE);
        GlStateManager.popMatrix();
        int cx=width/2;
        RenderUtil.drawGradientH(cx-58,height/2-24,cx+58,height/2-22,0x00FF4A16,ORANGE);
        String subtitle="MINECRAFT 1.8.9";
        fontRendererObj.drawStringWithShadow(subtitle,
                cx-fontRendererObj.getStringWidth(subtitle)/2,height/2-15,MUTED);
        fontRendererObj.drawStringWithShadow("FIRED CLIENT  /  v"+AstroClient.VERSION,
                8,height-13,0xFFB5B5BC);
        fontRendererObj.drawStringWithShadow("FIRED",width-43,height-13,ORANGE);
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException {
        switch(b.id){
            case SINGLE: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
            case MULTI: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
            case MODS: mc.displayGuiScreen(new ClickGUI()); break;
            case OPTIONS: mc.displayGuiScreen(new GuiOptions(this,mc.gameSettings)); break;
            case QUIT: mc.shutdown(); break;
        }
    }

    private static class FlatButton extends GuiButton {
        FlatButton(int id,int x,int y,int w,int h,String text){super(id,x,y,w,h,text);}
        @Override public void drawButton(Minecraft mc,int mx,int my){
            if(!visible)return;
            hovered=mx>=xPosition&&my>=yPosition&&mx<xPosition+width&&my<yPosition+height;
            int bg=hovered?HOVER:BTN;
            RenderUtil.drawRect(xPosition,yPosition,xPosition+width,yPosition+height,bg);
            if(hovered)RenderUtil.drawRect(xPosition,yPosition,xPosition+2,yPosition+height,ORANGE);
            RenderUtil.drawOutline(xPosition,yPosition,xPosition+width,yPosition+height,0x30FFFFFF);
            int color=hovered?WHITE:0xFFE0E0E0;
            mc.fontRendererObj.drawStringWithShadow(displayString,
                    xPosition+(width-mc.fontRendererObj.getStringWidth(displayString))/2,
                    yPosition+(height-8)/2,color);
        }
    }
}
