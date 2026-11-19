package dev.astro.ui;

import dev.astro.AstroClient;
import dev.astro.module.Category;
import dev.astro.module.Module;
import dev.astro.module.setting.BooleanSetting;
import dev.astro.module.setting.ColorSetting;
import dev.astro.module.setting.ModeSetting;
import dev.astro.module.setting.NumberSetting;
import dev.astro.module.setting.Setting;
import dev.astro.util.RenderUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** FIRED Client module interface. Independent implementation with a compact slide-out layout. */
public final class ClickGUI extends GuiScreen {
    private static final int HEADER = 38;
    private static final int SIDEBAR = 116;
    private static final int ROW = 29;
    private static final int ACCENT = 0xFFFF5425;
    private static final int ACCENT_SOFT = 0xFFFF8750;
    private static final int TEXT = 0xFFF4F4F6;
    private static final int MUTED = 0xFF9696A2;
    private static final int[] COLORS = {0xFFFFFFFF, 0xFFFF5425, 0xFFFF8528, 0xFFFF3030, 0xFF00D084, 0xFF45A5FF, 0xFFFFD43B};

    private Category selectedCategory = Category.COMBAT;
    private final Map<Category, Integer> scroll = new HashMap<Category, Integer>();
    private String search = "";
    private boolean searchFocused;
    private Module settingsModule;
    private NumberSetting draggingSlider;

    private int[] bounds() {
        ScaledResolution sr = new ScaledResolution(mc);
        int sw = sr.getScaledWidth(), sh = sr.getScaledHeight();
        int w = Math.max(280, Math.min(650, sw - 24));
        int h = Math.max(210, Math.min(390, sh - 24));
        w = Math.min(w, sw - 12);
        h = Math.min(h, sh - 12);
        return new int[]{(sw-w)/2, (sh-h)/2, w, h};
    }

    private List<Module> modules() {
        List<Module> source = AstroClient.INSTANCE.getModuleManager().getByCategory(selectedCategory);
        if (search.isEmpty()) return source;
        List<Module> result = new ArrayList<Module>();
        String q = search.toLowerCase();
        for (Module m : source) {
            if (m.getName().toLowerCase().contains(q) || m.getDescription().toLowerCase().contains(q)) result.add(m);
        }
        return result;
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        int[] b = bounds();
        int x=b[0], y=b[1], w=b[2], h=b[3];
        drawDefaultBackground();
        RenderUtil.drawRoundedRect(x-3,y-3,x+w+3,y+h+3,7,0x70000000);
        RenderUtil.drawRoundedRect(x,y,x+w,y+h,6,0xF0121218);
        drawHeader(x,y,w);
        drawSidebar(x,y,h,mx,my);
        drawContent(x,y,w,h,mx,my);
        if (settingsModule != null) drawSettings(x,y,w,h);
        super.drawScreen(mx,my,partialTicks);
    }

    private void drawHeader(int x,int y,int w) {
        RenderUtil.drawRoundedRect(x,y,x+w,y+HEADER,6,0xFF1B1B23);
        RenderUtil.drawRect(x,y+HEADER-1,x+w,y+HEADER,0xFF303039);
        mc.getTextureManager().bindTexture(new ResourceLocation("firedclient","textures/gui/fired.png"));
        Gui.drawModalRectWithCustomSizedTexture(x+10,y+8,0,0,22,22,22,22);
        FontRenderer fr=mc.fontRendererObj;
        fr.drawStringWithShadow("FIRED CLIENT",x+40,y+9,TEXT);
        fr.drawString("MODS",x+40,y+22,ACCENT_SOFT);
        int sx=x+w-178;
        RenderUtil.drawRoundedRect(sx,y+8,x+w-10,y+30,4,0xFF292932);
        RenderUtil.drawOutline(sx,y+8,x+w-10,y+30,searchFocused?ACCENT:0xFF3A3A45);
        fr.drawString("⌕",sx+8,y+15,MUTED);
        fr.drawString(search.isEmpty()?(searchFocused?"Type to search...":"Search mods"):search,sx+25,y+15,TEXT);
    }

    private void drawSidebar(int x,int y,int h,int mx,int my) {
        RenderUtil.drawRect(x,y+HEADER,x+SIDEBAR,y+h,0xFF17171E);
        int cy=y+HEADER+12;
        for(Category c:Category.values()) {
            boolean active=c==selectedCategory;
            boolean hover=mx>=x+8&&mx<x+SIDEBAR-8&&my>=cy&&my<cy+29;
            if(active) {
                RenderUtil.drawRoundedRect(x+8,cy,x+SIDEBAR-8,cy+29,4,0xFF35221F);
                RenderUtil.drawRoundedRect(x+8,cy+5,x+11,cy+24,1,ACCENT);
            } else if(hover) RenderUtil.drawRoundedRect(x+8,cy,x+SIDEBAR-8,cy+29,4,0xFF25252E);
            mc.fontRendererObj.drawString(c.getDisplayName(),x+20,cy+11,active?ACCENT_SOFT:(hover?TEXT:MUTED));
            cy+=34;
        }
        RenderUtil.drawRect(x+SIDEBAR-1,y+HEADER,x+SIDEBAR,y+h,0xFF303039);
        mc.fontRendererObj.drawString("FIRED 1.0",x+14,y+h-17,0xFF666671);
    }

    private void drawContent(int x,int y,int w,int h,int mx,int my) {
        int left=x+SIDEBAR+14, right=x+w-14, top=y+HEADER+12;
        mc.fontRendererObj.drawString(selectedCategory.getDisplayName().toUpperCase(),left,top, TEXT);
        List<Module> list=modules();
        mc.fontRendererObj.drawString(list.size()+" MODS",right-mc.fontRendererObj.getStringWidth(list.size()+" MODS"),top,MUTED);
        int start=top+20, bottom=y+h-12;
        int max=Math.max(0,list.size()-Math.max(1,(bottom-start)/ROW));
        int s=scroll.containsKey(selectedCategory)?scroll.get(selectedCategory):0;
        s=Math.max(0,Math.min(s,max)); scroll.put(selectedCategory,s);
        int visible=Math.max(1,(bottom-start)/ROW);
        for(int i=0;i<visible&&i+s<list.size();i++) {
            Module m=list.get(i+s); int ry=start+i*ROW;
            boolean hover=mx>=left&&mx<right&&my>=ry&&my<ry+ROW-2;
            RenderUtil.drawRoundedRect(left,ry,right,ry+ROW-3,4,hover?0xFF292932:0xFF202028);
            mc.fontRendererObj.drawStringWithShadow(m.getName(),left+10,ry+6,m.isEnabled()?ACCENT_SOFT:TEXT);
            String desc=m.getDescription();
            if(desc!=null&&!desc.isEmpty()) mc.fontRendererObj.drawString(desc,left+10,ry+17,0xFF858590);
            if(m.hasSettings()) mc.fontRendererObj.drawString("⚙",right-54,ry+8,MUTED);
            int tx=right-31, ty=ry+9;
            RenderUtil.drawRoundedRect(tx,ty,tx+22,ty+11,6,m.isEnabled()?ACCENT:0xFF4A4A55);
            RenderUtil.drawRoundedRect(m.isEnabled()?tx+12:tx+1,ty+1,m.isEnabled()?tx+21:tx+10,ty+10,5,0xFFF5F5F5);
        }
        if(max>0) {
            int trackX=right-2, trackY=start, trackH=bottom-start;
            int thumbH=Math.max(16,trackH*visible/list.size());
            int thumbY=trackY+(trackH-thumbH)*s/max;
            RenderUtil.drawRoundedRect(trackX,trackY,trackX+2,bottom,1,0x44555555);
            RenderUtil.drawRoundedRect(trackX,thumbY,trackX+2,thumbY+thumbH,1,ACCENT);
        }
    }

    private void drawSettings(int x,int y,int w,int h) {
        int pw=Math.min(330,w-24), ph=Math.min(h-24,54+settingsModule.getSettings().size()*27);
        int px=x+(w-pw)/2, py=y+(h-ph)/2;
        RenderUtil.drawRoundedRect(px-4,py-4,px+pw+4,py+ph+4,7,0xB8000000);
        RenderUtil.drawRoundedRect(px,py,px+pw,py+ph,5,0xFF191920);
        RenderUtil.drawRoundedRect(px,py,px+pw,py+30,4,0xFF29232A);
        mc.fontRendererObj.drawString(settingsModule.getName()+" SETTINGS",px+10,py+11,ACCENT_SOFT);
        mc.fontRendererObj.drawString("X",px+pw-17,py+11,TEXT);
        int ry=py+37;
        for(Setting<?> setting:settingsModule.getSettings()) {
            if(ry+22>py+ph-4) break;
            RenderUtil.drawRoundedRect(px+7,ry,px+pw-7,ry+22,3,0xFF292932);
            mc.fontRendererObj.drawString(setting.getName(),px+13,ry+8,TEXT);
            if(setting instanceof BooleanSetting) {
                boolean on=((BooleanSetting)setting).getValue();
                RenderUtil.drawRoundedRect(px+pw-40,ry+5,px+pw-14,ry+17,5,on?ACCENT:0xFF555560);
                RenderUtil.drawRoundedRect(on?px+pw-26:px+pw-38,ry+6,on?px+pw-15:px+pw-27,ry+16,5,0xFFFFFFFF);
            } else if(setting instanceof NumberSetting) {
                NumberSetting n=(NumberSetting)setting;
                String v=n.getStep()>=1?String.valueOf(n.getIntValue()):String.format("%.1f",n.getValue());
                mc.fontRendererObj.drawString(v,px+pw-48,ry+8,ACCENT_SOFT);
                RenderUtil.drawRect(px+120,ry+17,px+pw-60,ry+19,0xFF4A3A36);
                int knob=px+120+(int)((pw-180)*n.getPercent());
                RenderUtil.drawRoundedRect(knob-3,ry+14,knob+3,ry+22,3,ACCENT);
            } else if(setting instanceof ModeSetting) {
                mc.fontRendererObj.drawString(((ModeSetting)setting).getValue(),px+pw-105,ry+8,ACCENT_SOFT);
            } else if(setting instanceof ColorSetting) {
                int cx=px+pw-88;
                for(int color:COLORS) { RenderUtil.drawRect(cx,ry+6,cx+9,ry+16,color); cx+=11; }
            }
            ry+=27;
        }
    }

    @Override
    protected void mouseClicked(int mx,int my,int button) throws IOException {
        super.mouseClicked(mx,my,button);
        int[] b=bounds(); int x=b[0],y=b[1],w=b[2],h=b[3];
        int sx=x+w-178;
        if(button==0&&mx>=sx&&mx<=x+w-10&&my>=y+8&&my<=y+30) { searchFocused=true; return; }
        if(button==0&&searchFocused) searchFocused=false;
        if(settingsModule!=null) {
            int pw=Math.min(330,w-24),ph=Math.min(h-24,54+settingsModule.getSettings().size()*27);
            int px=x+(w-pw)/2,py=y+(h-ph)/2;
            if(button==0&&mx>=px+pw-25&&mx<=px+pw&&my>=py&&my<=py+30) { settingsModule=null; return; }
            int ry=py+37;
            for(Setting<?> setting:settingsModule.getSettings()) {
                if(my>=ry&&my<ry+22&&mx>=px+7&&mx<=px+pw-7) {
                    if(setting instanceof BooleanSetting&&button==0) ((BooleanSetting)setting).toggle();
                    else if(setting instanceof ModeSetting) { if(button==1)((ModeSetting)setting).cycleBack();else ((ModeSetting)setting).cycle(); }
                    else if(setting instanceof ColorSetting&&button==0) {
                        int cx=px+pw-88; for(int color:COLORS){if(mx>=cx&&mx<cx+9){((ColorSetting)setting).setValue(color);break;}cx+=11;}
                    } else if(setting instanceof NumberSetting&&button==0) { draggingSlider=(NumberSetting)setting; updateSlider(mx,px,pw); }
                    saveConfig(); return;
                }
                ry+=27;
            }
            if(mx>=px&&mx<=px+pw&&my>=py&&my<=py+ph)return;
            settingsModule=null;
        }
        if(button!=0&&button!=1)return;
        if(mx>=x+8&&mx<x+SIDEBAR-8&&my>=y+HEADER+12) {
            int index=(my-(y+HEADER+12))/34;
            if(index>=0&&index<Category.values().length) { selectedCategory=Category.values()[index]; return; }
        }
        int left=x+SIDEBAR+14,right=x+w-14,top=y+HEADER+32;
        if(mx<left||mx>right||my<top||my>y+h-12)return;
        int row=(my-top)/ROW, s=scroll.containsKey(selectedCategory)?scroll.get(selectedCategory):0;
        List<Module> list=modules();
        if(row<0||row+s>=list.size())return;
        Module m=list.get(row+s);
        if(button==1||(mx>=right-60&&m.hasSettings())) { if(m.hasSettings())settingsModule=m; return; }
        m.toggle(); saveConfig();
    }

    private void updateSlider(int mx,int px,int pw) {
        if(draggingSlider==null)return;
        float pct=(float)(mx-(px+120))/(float)Math.max(1,pw-180);
        draggingSlider.setPercent(Math.max(0f,Math.min(1f,pct)));
    }

    @Override
    protected void mouseClickMove(int mx,int my,int button,long elapsed) {
        if(draggingSlider!=null&&button==0) {
            int[] b=bounds(); int pw=Math.min(330,b[2]-24),px=b[0]+(b[2]-pw)/2;
            updateSlider(mx,px,pw);
        }
        super.mouseClickMove(mx,my,button,elapsed);
    }

    @Override
    protected void mouseReleased(int mx,int my,int state) {
        if(draggingSlider!=null){draggingSlider=null;saveConfig();}
        super.mouseReleased(mx,my,state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel=Mouse.getEventDWheel(); if(wheel==0)return;
        int mx=Mouse.getX()*width/mc.displayWidth;
        int my=height-Mouse.getY()*height/mc.displayHeight-1;
        int[] b=bounds(); int left=b[0]+SIDEBAR+14,right=b[0]+b[2]-14,top=b[1]+HEADER+32,bottom=b[1]+b[3]-12;
        if(mx>=left&&mx<=right&&my>=top&&my<=bottom) {
            int s=scroll.containsKey(selectedCategory)?scroll.get(selectedCategory):0;
            int max=Math.max(0,modules().size()-Math.max(1,(bottom-top)/ROW));
            scroll.put(selectedCategory,Math.max(0,Math.min(max,s+(wheel>0?-2:2))));
        }
    }

    @Override
    protected void keyTyped(char c,int keyCode) throws IOException {
        if(keyCode==1) {
            if(settingsModule!=null)settingsModule=null;
            else if(!search.isEmpty())search="";
            else mc.displayGuiScreen(null);
            return;
        }
        if(!searchFocused)return;
        if(keyCode==14){if(!search.isEmpty())search=search.substring(0,search.length()-1);return;}
        if(c>=32&&c<127)search+=c;
    }

    private void saveConfig() {
        if(AstroClient.INSTANCE.getConfigManager()!=null)AstroClient.INSTANCE.getConfigManager().save();
    }

    @Override public boolean doesGuiPauseGame(){return false;}
}
