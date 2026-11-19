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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FIRED Click GUI — Badlion-inspired tiled module browser.
 * Uses only Minecraft 1.8.9 / LWJGL 2 rendering APIs.
 */
public final class ClickGUI extends GuiScreen {
    private static final int HEADER_H = 58;
    private static final int SIDEBAR_W = 205;
    private static final int CARD_H = 205;
    private static final int GAP = 9;
    private static final int SETTING_ROW_H = 20;
    private static final int TOGGLE_W = 27;
    private static final int TOGGLE_H = 13;
    private static final int SCROLLBAR_W = 3;

    private static final int PANEL_BG = 0x70121217;
    private static final int HEADER_BG = 0x8A0D0D11;
    private static final int SIDEBAR_BG = 0x700F0F14;
    private static final int CARD_BG = 0x50303034;
    private static final int CARD_HOVER = 0x8040444C;
    private static final int ACCENT = 0xFFFF4A16;
    private static final int ACCENT_2 = 0xFFFF8528;
    private static final int TEXT = 0xFFF0F0F2;
    private static final int MUTED = 0xFF9999A2;
    private static final int OFF = 0xFF45454C;
    private static final int LINE = 0x24FFFFFF;
    private static final int[] COLORS = {0xFFFFFFFF, 0xFFFF4A16, 0xFFFF8528,
            0xFFFF3030, 0xFF00D084, 0xFF45A5FF, 0xFFFFD43B};

    private Category selectedCategory;
    private String searchQuery = "";
    private boolean searchFocused = true;
    private Module settingsModule;
    private NumberSetting draggingSlider;
    private final Map<String, Integer> scrollOffsets = new HashMap<String, Integer>();
    private final Map<Module, Float> toggleAnim = new HashMap<Module, Float>();

    private int[] bounds() {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = Math.min(Math.max(300, (int)(sr.getScaledWidth() * .96f)), sr.getScaledWidth() - 16);
        int h = Math.min(Math.max(230, (int)(sr.getScaledHeight() * .94f)), sr.getScaledHeight() - 16);
        w = Math.max(1, w);
        h = Math.max(1, h);
        return new int[]{(sr.getScaledWidth()-w)/2, (sr.getScaledHeight()-h)/2, w, h};
    }

    private String scrollKey() {
        return searchQuery.length() > 0 ? "__search__" :
                selectedCategory == null ? "__all__" : selectedCategory.name();
    }

    private List<Module> filteredModules() {
        List<Module> source = selectedCategory == null || searchQuery.length() > 0
                ? AstroClient.INSTANCE.getModuleManager().getModules()
                : AstroClient.INSTANCE.getModuleManager().getByCategory(selectedCategory);
        if (searchQuery.length() == 0) return source;
        List<Module> result = new ArrayList<Module>();
        String q = searchQuery.toLowerCase();
        for (Module m : source) {
            if (m.getName().toLowerCase().contains(q) ||
                    m.getDescription().toLowerCase().contains(q)) result.add(m);
        }
        return result;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Keep the live game scene visible behind the translucent interface.
        int[] b = bounds();
        drawPanel(mc.fontRendererObj, b[0], b[1], b[2], b[3], mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawPanel(FontRenderer fr, int x, int y, int w, int h, int mx, int my) {
        int right=x+w, bottom=y+h;
        RenderUtil.drawRoundedRect(x-3,y-3,right+3,bottom+3,8,0x50000000);
        RenderUtil.drawRoundedRect(x,y,right,bottom,5,PANEL_BG);
        RenderUtil.drawRect(x,y,right,y+HEADER_H,HEADER_BG);
        RenderUtil.drawRect(x,y+HEADER_H-1,right,y+HEADER_H,LINE);

        // Badlion reference structure: brand at the top, tabs across the header,
        // filter rail on the left and equal-sized module tiles in a 3-column grid.
        // Original FIRED flame mark (no third-party client branding).
        RenderUtil.drawRoundedRect(x+8,y+9,x+34,y+35,6,ACCENT);
        RenderUtil.drawRoundedRect(x+17,y+14,x+25,y+29,4,0xFFFFB044);
        RenderUtil.drawRoundedRect(x+20,y+18,x+25,y+29,3,0xFFFFE0A0);
        fr.drawStringWithShadow("FIRED",x+42,y+11,TEXT);
        fr.drawStringWithShadow("CLIENT  /  MODS",x+42,y+24,MUTED);

        int tabY=y+9;
        fr.drawStringWithShadow("Available Mods",x+SIDEBAR_W+12,y+23,TEXT);
        int searchW=Math.max(82,Math.min(190,w/3));
        int searchX=right-searchW-12;
        int addW=100, addX=searchX-addW-8;
        RenderUtil.drawRoundedRect(addX,tabY,addX+addW,tabY+27,4,0x603A3A40);
        RenderUtil.drawOutline(addX,tabY,addX+addW,tabY+27,0x66777777);
        fr.drawStringWithShadow("Add Mods",addX+18,tabY+9,TEXT);
        RenderUtil.drawRoundedRect(searchX,tabY,searchX+searchW,tabY+27,4,0x70202027);
        fr.drawStringWithShadow("⌕",searchX+9,tabY+9,ACCENT_2);
        String searchLabel=searchQuery.length()==0?"Search...":searchQuery;
        fr.drawStringWithShadow(searchLabel,searchX+25,tabY+9,
                searchQuery.length()==0?MUTED:TEXT);

        RenderUtil.drawRect(x,y+HEADER_H,x+SIDEBAR_W,bottom,SIDEBAR_BG);
        RenderUtil.drawRect(x+SIDEBAR_W,y+HEADER_H,x+SIDEBAR_W+1,bottom,LINE);
        drawSidebar(fr,x,y,h,mx,my);

        int contentX=x+SIDEBAR_W+12, contentY=y+HEADER_H+12;
        int contentW=w-SIDEBAR_W-24, contentH=h-HEADER_H-22;
        fr.drawStringWithShadow(selectedCategory==null?"All Mods":selectedCategory.getDisplayName(),
                contentX,contentY,TEXT);
        fr.drawStringWithShadow(String.valueOf(filteredModules().size())+" mods",
                contentX,contentY+13,MUTED);

        int gridY=contentY+31;
        int cols=contentW>=700?4:(contentW>=480?3:(contentW>=300?2:1));
        int cardW=(contentW-(cols-1)*GAP-6)/cols;
        List<Module> mods=filteredModules();
        int rows=(mods.size()+cols-1)/cols;
        int totalH=rows*(CARD_H+GAP);
        int viewportH=Math.max(30,contentH-31);
        int maxScroll=Math.max(0,totalH-viewportH);
        int scroll=scrollOffsets.containsKey(scrollKey())?scrollOffsets.get(scrollKey()):0;
        scroll=Math.max(0,Math.min(scroll,maxScroll));
        scrollOffsets.put(scrollKey(),scroll);

        for(int i=0;i<mods.size();i++){
            int col=i%cols, row=i/cols;
            int rx=contentX+col*(cardW+GAP);
            int ry=gridY+row*(CARD_H+GAP)-scroll;
            if(ry+CARD_H<gridY||ry>bottom-6)continue;
            Module m=mods.get(i);
            boolean hover=mx>=rx&&mx<rx+cardW&&my>=Math.max(gridY,ry)&&my<Math.min(bottom-6,ry+CARD_H);
            RenderUtil.drawRoundedRect(rx,ry,rx+cardW,ry+CARD_H,3,
                    hover?0xFF303541:CARD_BG);
            RenderUtil.drawOutline(rx,ry,rx+cardW,ry+CARD_H,
                    m.isEnabled()?0xA0FF6428:0x305F4538);
            String name=m.getName();
            // Compact module tile: FIRED icon, title, description and a clear state control.
            RenderUtil.drawRoundedRect(rx+10,ry+10,rx+34,ry+34,5,
                    m.isEnabled()?0xFF63301F:0xFF303039);
            String glyph=name.length()>0?name.substring(0,1).toUpperCase():"?";
            fr.drawStringWithShadow(glyph,rx+18,ry+18,m.isEnabled()?ACCENT_2:TEXT);
            fr.drawStringWithShadow(name,rx+42,ry+12,TEXT);
            String description=m.getDescription()==null?"":m.getDescription();
            if(description.length()>0){
                int maxChars=Math.max(12,(cardW-58)/4);
                if(description.length()>maxChars)description=description.substring(0,maxChars-3)+"...";
                fr.drawStringWithShadow(description,rx+42,ry+25,MUTED);
            }
            fr.drawStringWithShadow(m.isEnabled()?"ATIVADO":"DESATIVADO",
                    rx+12,ry+CARD_H-19,m.isEnabled()?ACCENT_2:MUTED);
            int toggleW=30,toggleX=rx+cardW-48;
            drawToggle(toggleX,ry+CARD_H-22,toggleW,13,m.isEnabled()?1f:0f);
            if(m.hasSettings()){
                RenderUtil.drawRoundedRect(rx+cardW-25,ry+8,rx+cardW-7,ry+26,4,
                        settingsModule==m?0xFF493027:0xFF34343D);
                fr.drawStringWithShadow("⚙",rx+cardW-21,ry+12,
                        settingsModule==m?ACCENT_2:MUTED);
            }
        }
        if(maxScroll>0){
            int trackX=right-5,trackY=gridY,trackH=viewportH;
            int thumbH=Math.max(18,trackH*trackH/Math.max(trackH,totalH));
            int thumbY=trackY+(int)((float)scroll/maxScroll*(trackH-thumbH));
            RenderUtil.drawRoundedRect(trackX,trackY,trackX+3,trackY+trackH,2,0x30FFFFFF);
            RenderUtil.drawRoundedRect(trackX,thumbY,trackX+3,thumbY+thumbH,2,ACCENT);
        }
        if(settingsModule!=null)drawSettingsPopup(fr,x,y,w,h,mx,my);
    }

    private void drawSidebar(FontRenderer fr,int x,int y,int h,int mx,int my){
        int sx=x+8, sy=y+HEADER_H+15;
        fr.drawStringWithShadow("FILTERS",sx+6,sy,MUTED);
        sy+=19;
        drawSideItem(fr,"All",null,sx,sy,mx,my); sy+=27;
        for(Category category:Category.values()){
            drawSideItem(fr,category.getDisplayName(),category,sx,sy,mx,my);
            sy+=27;
        }
    }

    private void drawSideItem(FontRenderer fr,String label,Category category,int x,int y,int mx,int my){
        boolean active=selectedCategory==category;
        boolean hover=mx>=x && mx<x+SIDEBAR_W-16 && my>=y-5 && my<y+17;
        if(active)RenderUtil.drawRoundedRect(x,y-5,x+SIDEBAR_W-16,y+17,3,0xFF343E4C);
        else if(hover)RenderUtil.drawRoundedRect(x,y-5,x+SIDEBAR_W-16,y+17,3,0xFF202630);
        if(active)RenderUtil.drawRect(x,y-4,x+2,y+16,ACCENT);
        fr.drawStringWithShadow(label,x+9,y+2,active?ACCENT_2:TEXT);
    }

    private void drawToggle(int x,int y,int w,int h,float value){
        int track=lerp(OFF,ACCENT,value);
        RenderUtil.drawRoundedRect(x,y,x+w,y+h,4,track);
        int k=h-4;
        float kx=x+2+(w-k-4)*value;
        RenderUtil.drawRoundedRect(kx,y+2,kx+k,y+h-2,3,0xFFFFFFFF);
    }

    private void drawSettingsPopup(FontRenderer fr,int x,int y,int w,int h,int mx,int my){
        int pw=Math.min(270,w-30), ph=Math.min(h-50,70+settingsModule.getSettings().size()*24);
        int px=x+w-pw-14, py=y+(h-ph)/2;
        RenderUtil.drawRoundedRect(px,py,px+pw,py+ph,5,0xFF17171D);
        RenderUtil.drawOutline(px,py,px+pw,py+ph,0x80FF4A16);
        fr.drawStringWithShadow(settingsModule.getName()+" - Configurações",px+12,py+11,TEXT);
        fr.drawStringWithShadow("X",px+pw-18,py+11,ACCENT_2);
        int ry=py+32;
        for(Setting<?> s:settingsModule.getSettings()){
            RenderUtil.drawRoundedRect(px+9,ry,px+pw-9,ry+20,2,0xFF222229);
            fr.drawStringWithShadow(s.getName(),px+16,ry+6,MUTED);
            if(s instanceof BooleanSetting){
                drawToggle(px+pw-43,ry+5,24,11,((BooleanSetting)s).getValue()?1:0);
            }else if(s instanceof NumberSetting){
                NumberSetting n=(NumberSetting)s;
                String v=n.getStep()>=1?String.valueOf(n.getIntValue()):String.format("%.1f",n.getValue());
                fr.drawStringWithShadow(v,px+pw-48,ry+6,TEXT);
            }else if(s instanceof ModeSetting){
                fr.drawStringWithShadow(((ModeSetting)s).getValue(),px+pw-95,ry+6,ACCENT_2);
            }else if(s instanceof ColorSetting){
                int dx=px+pw-92;
                for(int c:COLORS){RenderUtil.drawRect(dx,ry+5,dx+9,ry+14,c);dx+=11;}
            }
            ry+=24;
        }
    }

    @Override
    protected void mouseClicked(int mx,int my,int button)throws IOException{
        super.mouseClicked(mx,my,button);
        int[] b=bounds(); int x=b[0],y=b[1],w=b[2],h=b[3],right=x+w;
        if(button==0 && my>=y && my<y+HEADER_H){
            int searchW=Math.max(82,Math.min(190,w/3));
            int searchX=right-searchW-12;
            searchFocused=mx>=searchX&&mx<=searchX+searchW;
            return;
        }
        if(settingsModule!=null){
            int pw=Math.min(270,w-30),ph=Math.min(h-50,70+settingsModule.getSettings().size()*24);
            int px=x+w-pw-14,py=y+(h-ph)/2;
            if(button==0 && mx>=px+pw-26 && mx<=px+pw && my>=py && my<py+26){settingsModule=null;return;}
            int ry=py+32;
            for(Setting<?> s:settingsModule.getSettings()){
                if(my>=ry && my<ry+20 && mx>=px+9 && mx<px+pw-9){
                    if(s instanceof BooleanSetting && button==0)((BooleanSetting)s).toggle();
                    else if(s instanceof ModeSetting){if(button==1)((ModeSetting)s).cycleBack();else ((ModeSetting)s).cycle();}
                    else if(s instanceof ColorSetting && button==0){
                        int dx=px+pw-92;
                        for(int color:COLORS){if(mx>=dx&&mx<dx+9){((ColorSetting)s).setValue(color);break;}dx+=11;}
                    }else if(s instanceof NumberSetting && button==0){draggingSlider=(NumberSetting)s;updateSlider(mx,px,pw);}
                    saveConfig(); return;
                }
                ry+=24;
            }
            if(mx>=px&&mx<=px+pw&&my>=py&&my<=py+ph)return;
            settingsModule=null;
        }
        if(button!=0&&button!=1)return;
        int sx=x+8,sy=y+HEADER_H+34;
        if(mx>=sx&&mx<sx+SIDEBAR_W-16){
            if(my>=sy-5&&my<sy+17){selectedCategory=null;return;}
            sy+=27;
            for(Category category:Category.values()){
                if(my>=sy-5&&my<sy+17){selectedCategory=category;return;}
                sy+=27;
            }
        }
        int contentX=x+SIDEBAR_W+12,contentY=y+HEADER_H+12;
        int contentW=w-SIDEBAR_W-24,contentH=h-HEADER_H-22;
        int cols=contentW>=760?4:(contentW>=520?3:(contentW>=340?2:1));
        int cardW=(contentW-(cols-1)*GAP-6)/cols;
        int gridY=contentY+31;
        List<Module> mods=filteredModules();
        int rows=(mods.size()+cols-1)/cols,totalH=rows*(CARD_H+GAP);
        int maxScroll=Math.max(0,totalH-Math.max(30,contentH-31));
        int scroll=scrollOffsets.containsKey(scrollKey())?scrollOffsets.get(scrollKey()):0;
        scroll=Math.max(0,Math.min(scroll,maxScroll));
        for(int i=0;i<mods.size();i++){
            int col=i%cols,row=i/cols,cx=contentX+col*(cardW+GAP),cy=gridY+row*(CARD_H+GAP)-scroll;
            if(mx<cx||mx>=cx+cardW||my<cy||my>=cy+CARD_H)continue;
            Module module=mods.get(i);
            if(module.hasSettings()&&mx>=cx+cardW-30&&mx<=cx+cardW-5&&my>=cy+5&&my<=cy+31){settingsModule=module;return;}
            module.toggle();saveConfig();return;
        }
    }

    private void updateSlider(int mouseX,int px,int pw){
        if(draggingSlider==null)return;
        float pct=(float)(mouseX-(px+66))/(float)Math.max(1,pw-120);
        draggingSlider.setPercent(Math.max(0f,Math.min(1f,pct)));
    }

    @Override
    protected void mouseClickMove(int mx,int my,int button,long elapsed){
        if(draggingSlider!=null&&button==0){int[] b=bounds();int pw=Math.min(270,b[2]-30);updateSlider(mx,b[0]+b[2]-pw-14,pw);}
        super.mouseClickMove(mx,my,button,elapsed);
    }

    @Override
    protected void mouseReleased(int mx,int my,int state){
        if(draggingSlider!=null){draggingSlider=null;saveConfig();}
        super.mouseReleased(mx,my,state);
    }

    @Override
    public void handleMouseInput()throws IOException{
        super.handleMouseInput();
        int wheel=org.lwjgl.input.Mouse.getEventDWheel();
        if(wheel!=0){String key=scrollKey();int value=scrollOffsets.containsKey(key)?scrollOffsets.get(key):0;
            value-=(wheel>0?1:-1)*(CARD_H+GAP)*2;scrollOffsets.put(key,Math.max(0,value));}
    }

    @Override
    protected void keyTyped(char c,int key)throws IOException{
        if(key==1){if(settingsModule!=null)settingsModule=null;else if(searchQuery.length()>0)searchQuery="";else mc.displayGuiScreen(null);return;}
        if(!searchFocused)return;
        if(key==14){if(searchQuery.length()>0)searchQuery=searchQuery.substring(0,searchQuery.length()-1);return;}
        if(c>=' '&&c<127)searchQuery+=c;
    }

    private void saveConfig(){
        if(AstroClient.INSTANCE.getConfigManager()!=null)AstroClient.INSTANCE.getConfigManager().save();
    }

    @Override public boolean doesGuiPauseGame(){return false;}

    private static int lerp(int a,int b,float t){
        int aa=(int)(((a>>>24)&255)+(((b>>>24)&255)-((a>>>24)&255))*t);
        int r=(int)(((a>>>16)&255)+(((b>>>16)&255)-((a>>>16)&255))*t);
        int g=(int)(((a>>>8)&255)+(((b>>>8)&255)-((a>>>8)&255))*t);
        int bl=(int)((a&255)+((b&255)-(a&255))*t);
        return (aa<<24)|(r<<16)|(g<<8)|bl;
    }
}
