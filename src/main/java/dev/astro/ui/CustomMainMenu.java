package dev.astro.ui;

import dev.astro.AstroClient;
import dev.astro.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import java.awt.Color;
import java.io.IOException;

/** FIRED main menu: a clean Lunar-inspired home screen with direct Mods access. */
public final class CustomMainMenu extends GuiScreen {
    private static final int ORANGE = 0xFFFF4A16;
    private static final int ORANGE2 = 0xFFFF8A28;
    private static final int PANEL = new Color(13, 14, 20, 226).getRGB();
    private static final int BTN = new Color(28, 29, 38, 238).getRGB();
    private static final int BTN_HOVER = new Color(48, 35, 31, 245).getRGB();
    private static final int WHITE = 0xFFF4F4F6;
    private static final int MUTED = 0xFF9A9BA6;
    private static final int BTN_W = 190, BTN_H = 25, GAP = 7;
    private static final int ID_SINGLE=1, ID_MULTI=2, ID_OPTIONS=3, ID_QUIT=4, ID_MODS=5;

    @Override public void initGui() {
        buttonList.clear();
        int x = width / 2 - BTN_W / 2;
        int y = height / 2 + 42;
        buttonList.add(new FlatButton(ID_SINGLE,x,y,BTN_W,BTN_H,"Singleplayer"));
        buttonList.add(new FlatButton(ID_MULTI,x,y+(BTN_H+GAP),BTN_W,BTN_H,"Multiplayer"));
        buttonList.add(new FlatButton(ID_MODS,x,y+2*(BTN_H+GAP),BTN_W,BTN_H,"FIRED Mods  •  Click GUI"));
        buttonList.add(new FlatButton(ID_OPTIONS,x,y+3*(BTN_H+GAP),BTN_W,BTN_H,"Settings"));
        buttonList.add(new FlatButton(ID_QUIT,x,y+4*(BTN_H+GAP),BTN_W,BTN_H,"Quit Game"));
    }

    @Override public void drawScreen(int mx,int my,float partialTicks) {
        drawGradientRect(0,0,width,height,0xFF10121A,0xFF090A10);
        // Warm FIRED glow behind the central feature panel.
        RenderUtil.drawGradientH(width/2-260,0,width/2+260,height,0x18FF4A16,0x00101010);
        int panelW=Math.min(720,width-36), panelH=Math.min(440,height-30);
        int px=(width-panelW)/2, py=(height-panelH)/2;
        RenderUtil.drawRoundedRect(px,py,px+panelW,py+panelH,9,PANEL);
        RenderUtil.drawOutline(px,py,px+panelW,py+panelH,0x24FFFFFF);

        // Left navigation rail, similar layout rhythm to a modern client home screen.
        RenderUtil.drawRoundedRect(px+14,py+14,px+154,py+panelH-14,7,0xD90B0C12);
        drawString(fontRendererObj,"FIRED",px+30,py+32,ORANGE);
        drawString(fontRendererObj,"CLIENT",px+30,py+45,WHITE);
        RenderUtil.drawRect(px+28,py+64,px+138,py+65,0x35FFFFFF);
        drawString(fontRendererObj,"HOME",px+30,py+82,ORANGE2);
        drawString(fontRendererObj,"MODS",px+30,py+108,WHITE);
        drawString(fontRendererObj,"1.8.9",px+30,py+panelH-42,MUTED);

        int cx=width/2;
        RenderUtil.drawRoundedRect(cx-24,py+28,cx+24,py+76,10,ORANGE);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F,2.0F,1.0F);
        fontRendererObj.drawStringWithShadow("F",(cx-11)/2F,(py+39)/2F,0xFFFFF4EE);
        GlStateManager.popMatrix();
        String title="FIRED";
        GlStateManager.pushMatrix();
        float scale=3.4F;
        GlStateManager.scale(scale,scale,1F);
        fontRendererObj.drawStringWithShadow(title,cx/scale-fontRendererObj.getStringWidth(title)/2F,
                (py+90)/scale,WHITE);
        GlStateManager.popMatrix();
        RenderUtil.drawGradientH(cx-72,py+132,cx+72,py+135,ORANGE,ORANGE2);
        String subtitle="YOUR MINECRAFT EXPERIENCE";
        fontRendererObj.drawStringWithShadow(subtitle,cx-fontRendererObj.getStringWidth(subtitle)/2F,
                py+144,MUTED);

        super.drawScreen(mx,my,partialTicks);
        String footer="Minecraft 1.8.9  •  FIRED Client v"+AstroClient.VERSION;
        fontRendererObj.drawStringWithShadow(footer,px+176,py+panelH-27,MUTED);
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException {
        switch(b.id){
            case ID_SINGLE: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
            case ID_MULTI: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
            case ID_MODS: mc.displayGuiScreen(new ClickGUI()); break;
            case ID_OPTIONS: mc.displayGuiScreen(new GuiOptions(this,mc.gameSettings)); break;
            case ID_QUIT: mc.shutdown(); break;
        }
    }

    private static class FlatButton extends GuiButton {
        FlatButton(int id,int x,int y,int w,int h,String text){super(id,x,y,w,h,text);}
        @Override public void drawButton(Minecraft mc,int mx,int my){
            if(!visible)return;
            hovered=mx>=xPosition&&my>=yPosition&&mx<xPosition+width&&my<yPosition+height;
            int bg=hovered?BTN_HOVER:BTN;
            RenderUtil.drawRoundedRect(xPosition,yPosition,xPosition+width,yPosition+height,4,bg);
            RenderUtil.drawOutline(xPosition,yPosition,xPosition+width,yPosition+height,0x25FFFFFF);
            if(hovered)RenderUtil.drawRoundedRect(xPosition+1,yPosition+4,xPosition+4,yPosition+height-4,1,ORANGE);
            int col=hovered?WHITE:0xFFD0D0D5;
            fontRendererObj(mc).drawStringWithShadow(displayString,
                    xPosition+(width-fontRendererObj(mc).getStringWidth(displayString))/2F,
                    yPosition+(height-8)/2F+1,col);
        }
        private FontRenderer fontRendererObj(Minecraft mc){return mc.fontRendererObj;}
    }
}
