package dev.astro.ui;

import dev.astro.util.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/**
 * Visual placeholder for the FIRED cosmetics area.
 * Cosmetic assets and equip actions will be connected in a later milestone.
 */
public final class CosmeticsGUI extends GuiScreen {
    private final GuiScreen parent;
    private static final int ACCENT = 0xFFFF4A16;
    private static final int TEXT = 0xFFF2F2F4;
    private static final int MUTED = 0xFF9999A2;

    private static final String[] NAMES = {"CAPES", "WINGS", "HAND ITEMS", "AURAS", "TRAILS"};
    private static final String[] DETAILS = {
            "Capas animadas e estilos FIRED",
            "Asas e efeitos nas costas",
            "Itens exibidos na mao do jogador",
            "Efeitos visuais ao redor do personagem",
            "Rastros de particulas ao se movimentar"
    };

    public CosmeticsGUI(GuiScreen parent) { this.parent = parent; }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int w = Math.min(650, width - 28);
        int h = Math.min(390, height - 28);
        int x = (width - w) / 2, y = (height - h) / 2;
        RenderUtil.drawRoundedRect(x-4,y-4,x+w+4,y+h+4,9,0x65000000);
        RenderUtil.drawRoundedRect(x,y,x+w,y+h,7,0xF0121218);
        RenderUtil.drawRect(x,y,x+w,y+58,0xFF0D0D12);
        RenderUtil.drawRect(x,y+57,x+w,y+59,0xB5FF4A16);

        RenderUtil.drawRoundedRect(x+16,y+13,x+48,y+45,7,0xFF552719);
        drawShirt(x+32,y+29,0xFFFF7136);
        fontRendererObj.drawStringWithShadow("FIRED",x+60,y+15,TEXT);
        fontRendererObj.drawStringWithShadow("COSMETICS",x+60,y+29,ACCENT);
        fontRendererObj.drawStringWithShadow("PERSONALIZE YOUR LOOK",x+60,y+42,MUTED);
        fontRendererObj.drawStringWithShadow("ESC  VOLTAR",x+w-76,y+25,MUTED);

        int cols = w >= 520 ? 3 : 2;
        int gap = 12, cardW = (w - 36 - gap*(cols-1))/cols;
        int cardH = 112, startX=x+18, startY=y+78;
        for(int i=0;i<NAMES.length;i++){
            int col=i%cols,row=i/cols;
            int cx=startX+col*(cardW+gap), cy=startY+row*(cardH+gap);
            boolean hover=mouseX>=cx&&mouseX<cx+cardW&&mouseY>=cy&&mouseY<cy+cardH;
            RenderUtil.drawRoundedRect(cx,cy,cx+cardW,cy+cardH,5,hover?0xFF29252A:0xFF1D1D24);
            RenderUtil.drawOutline(cx,cy,cx+cardW,cy+cardH,hover?0xA0FF4A16:0x405F4538);
            drawCategoryIcon(cx+27,cy+31,i);
            fontRendererObj.drawStringWithShadow(NAMES[i],cx+52,cy+19,TEXT);
            String detail=DETAILS[i];
            int max=Math.max(10,(cardW-64)/4);
            if(detail.length()>max)detail=detail.substring(0,max-3)+"...";
            fontRendererObj.drawStringWithShadow(detail,cx+52,cy+33,MUTED);
            RenderUtil.drawRoundedRect(cx+12,cy+78,cx+cardW-12,cy+99,4,0xFF38231D);
            String soon="EM BREVE";
            fontRendererObj.drawStringWithShadow(soon,cx+cardW/2-fontRendererObj.getStringWidth(soon)/2,cy+85,ACCENT);
        }
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    private void drawCategoryIcon(int x,int y,int type) {
        int c=0xFFFF7136;
        if(type==0) {
            RenderUtil.drawRect(x-8,y-11,x+8,y+11,0x553F2822);
            RenderUtil.drawOutline(x-8,y-11,x+8,y+11,c);
            RenderUtil.drawRect(x-7,y-2,x+7,y-1,c);
        } else if(type==1) {
            RenderUtil.drawRect(x-10,y-2,x-2,y+8,c);
            RenderUtil.drawRect(x+2,y-2,x+10,y+8,c);
            RenderUtil.drawRect(x-6,y-9,x-2,y+1,c);
            RenderUtil.drawRect(x+2,y-9,x+6,y+1,c);
        } else if(type==2) {
            RenderUtil.drawRoundedRect(x-5,y-11,x+5,y+11,3,0xFF34343D);
            RenderUtil.drawOutline(x-5,y-11,x+5,y+11, c);
        } else if(type==3) {
            org.lwjgl.opengl.GL11.glPushMatrix();
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
            org.lwjgl.opengl.GL11.glColor4f(1f,.44f,.21f,1f);
            org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_LINE_LOOP);
            for(int i=0;i<32;i++){double a=Math.PI*2*i/32.0;org.lwjgl.opengl.GL11.glVertex2f(x+(float)Math.cos(a)*10,y+(float)Math.sin(a)*10);}
            org.lwjgl.opengl.GL11.glEnd();
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
            org.lwjgl.opengl.GL11.glColor4f(1,1,1,1);
            org.lwjgl.opengl.GL11.glPopMatrix();
        } else {
            RenderUtil.drawRect(x-10,y-2,x-4,y+2,c);
            RenderUtil.drawRect(x-1,y-7,x+5,y-3,c);
            RenderUtil.drawRect(x+5,y+3,x+10,y+7,c);
        }
    }

    private void drawShirt(int x,int y,int color) {
        RenderUtil.drawRect(x-5,y-6,x+5,y+8,color);
        RenderUtil.drawRect(x-10,y-5,x-5,y+1,color);
        RenderUtil.drawRect(x+5,y-5,x+10,y+1,color);
        RenderUtil.drawRect(x-2,y-9,x+2,y-6,color);
    }

    @Override
    protected void mouseClicked(int mouseX,int mouseY,int button)throws IOException {
        if(button==0 && mouseX>=width/2+Math.min(650,width-28)/2-90 && mouseY<height/2-Math.min(390,height-28)/2+58){
            mc.displayGuiScreen(parent);
            return;
        }
        super.mouseClicked(mouseX,mouseY,button);
    }

    @Override protected void keyTyped(char typedChar,int keyCode)throws IOException {
        if(keyCode==1){mc.displayGuiScreen(parent);return;}
        super.keyTyped(typedChar,keyCode);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
