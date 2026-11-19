package dev.astro.ui;

import dev.astro.AstroClient;
import dev.astro.module.Category;
import dev.astro.module.Module;
import dev.astro.module.setting.BooleanSetting;
import dev.astro.module.setting.ColorSetting;
import dev.astro.module.setting.ModeSetting;
import dev.astro.module.setting.NumberSetting;
import dev.astro.module.setting.Setting;
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

/** FIRED slideout ClickGUI, styled after the compact Badlion 1.8.9 panel. */
public final class ClickGUI extends GuiScreen {
    private static final int PANEL_W = 210;
    private static final int HEADER_H = 31;
    private static final int ROW_H = 27;
    private static final int ORANGE = 0xFFFF5728;
    private static final int ORANGE_LIGHT = 0xFFFF9252;
    private static final int BG = 0xF0141418;
    private static final int PANEL = 0xFF1B1B20;
    private static final int ROW = 0xFF25252B;
    private static final int MUTED = 0xFF9999A2;
    private static final int WHITE = 0xFFF5F5F5;
    private static final int[] COLORS = {0xFFFFFFFF,0xFFFF5728,0xFFFF963D,0xFFE83D3D,0xFF42A5F5,0xFF39C987,0xFFB36BFF};

    private Category category = Category.COMBAT;
    private final Map<Category,Integer> offsets = new HashMap<Category,Integer>();
    private String search = "";
    private boolean searchFocused;
    private Module settingsModule;
    private NumberSetting draggingSlider;
    private boolean showCosmetics;
    private int panelX, panelY, panelH;

    private void layout() {
        ScaledResolution sr = new ScaledResolution(mc);
        panelH = sr.getScaledHeight();
        panelX = sr.getScaledWidth() - PANEL_W;
        panelY = 0;
    }

    private List<Module> filteredModules() {
        List<Module> list = AstroClient.INSTANCE.getModuleManager().getByCategory(category);
        if (search.length() == 0) return list;
        List<Module> filtered = new ArrayList<Module>();
        String q = search.toLowerCase();
        for (Module module : list) {
            if (module.getName().toLowerCase().contains(q)
                    || (module.getDescription() != null && module.getDescription().toLowerCase().contains(q))) {
                filtered.add(module);
            }
        }
        return filtered;
    }

    @Override
    public void initGui() {
        layout();
        settingsModule = null;
        draggingSlider = null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        layout();
        drawDefaultBackground();
        Gui.drawRect(panelX - 3, 0, panelX, panelH, 0x55000000);
        Gui.drawRect(panelX, 0, panelX + PANEL_W, panelH, BG);
        drawHeader();
        if (settingsModule != null) drawSettings();
        else drawMain(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHeader() {
        Gui.drawRect(panelX, 0, panelX + PANEL_W, HEADER_H, 0xFF202026);
        mc.getTextureManager().bindTexture(new ResourceLocation("firedclient", "textures/gui/fired.png"));
        Gui.drawModalRectWithCustomSizedTexture(panelX + 7, 5, 0, 0, 21, 21, 21, 21);
        mc.fontRendererObj.drawString("FIRED CLIENT", panelX + 34, 7, WHITE);
        mc.fontRendererObj.drawString("1.8.9", panelX + 34, 18, ORANGE_LIGHT);
        Gui.drawRect(panelX, HEADER_H - 1, panelX + PANEL_W, HEADER_H, ORANGE);
    }

    private void drawMain(int mouseX, int mouseY) {
        int y = HEADER_H + 7;
        // Main slideout tabs, like the original compact client panel.
        int tabW = (PANEL_W - 18) / 2;
        drawTab(panelX + 6, y, tabW, "MODS", !showCosmetics);
        drawTab(panelX + 12 + tabW, y, tabW, "COSMETICS", showCosmetics);
        y += 27;

        if (showCosmetics) {
            Gui.drawRect(panelX + 7, y, panelX + PANEL_W - 7, y + 30, ROW);
            mc.fontRendererObj.drawString("COSMETICS", panelX + 14, y + 7, ORANGE_LIGHT);
            mc.fontRendererObj.drawString("Capes  |  Trails  |  Hand", panelX + 14, y + 18, MUTED);
            mc.fontRendererObj.drawString("Cosmetics are being added", panelX + 10, y + 43, MUTED);
            return;
        }

        Gui.drawRect(panelX + 7, y, panelX + PANEL_W - 7, y + 22, 0xFF292930);
        Gui.drawRect(panelX + 7, y, panelX + PANEL_W - 7, y + 22, 0xFF292930);
        mc.fontRendererObj.drawString(search.length() == 0 ? "Search mods..." : search,
                panelX + 14, y + 7, searchFocused ? WHITE : MUTED);
        y += 29;

        // Category strip: compact, two columns to retain the narrow slideout proportions.
        int catStart = y;
        int colW = (PANEL_W - 18) / 2;
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = panelX + 7 + col * (colW + 4);
            int cy = catStart + row * 23;
            boolean active = cats[i] == category;
            Gui.drawRect(cx, cy, cx + colW, cy + 20, active ? 0xFF48271F : 0xFF24242A);
            if (active) Gui.drawRect(cx, cy, cx + 2, cy + 20, ORANGE);
            mc.fontRendererObj.drawString(cats[i].getDisplayName(), cx + 7, cy + 6, active ? ORANGE_LIGHT : MUTED);
        }
        y += 3 * 23 + 5;
        Gui.drawRect(panelX + 7, y, panelX + PANEL_W - 7, y + 1, 0xFF393940);
        y += 7;

        List<Module> modules = filteredModules();
        int available = panelH - y - 8;
        int visible = Math.max(1, available / ROW_H);
        int max = Math.max(0, modules.size() - visible);
        int offset = offsets.containsKey(category) ? offsets.get(category) : 0;
        offset = Math.max(0, Math.min(max, offset));
        offsets.put(category, offset);

        for (int i = 0; i < visible && i + offset < modules.size(); i++) {
            Module module = modules.get(i + offset);
            int ry = y + i * ROW_H;
            boolean hover = mouseX >= panelX + 7 && mouseX <= panelX + PANEL_W - 7
                    && mouseY >= ry && mouseY < ry + ROW_H - 2;
            Gui.drawRect(panelX + 7, ry, panelX + PANEL_W - 7, ry + ROW_H - 2,
                    hover ? 0xFF303037 : ROW);
            mc.fontRendererObj.drawString(module.getName(), panelX + 13, ry + 5,
                    module.isEnabled() ? ORANGE_LIGHT : WHITE);
            if (module.hasSettings()) mc.fontRendererObj.drawString("⚙", panelX + PANEL_W - 48, ry + 7, MUTED);
            int tx = panelX + PANEL_W - 32;
            int ty = ry + 7;
            Gui.drawRect(tx, ty, tx + 19, ty + 11, module.isEnabled() ? ORANGE_DARK : 0xFF505058);
            Gui.drawRect(module.isEnabled() ? tx + 9 : tx + 1, ty + 1,
                    module.isEnabled() ? tx + 18 : tx + 10, ty + 10, WHITE);
        }
        if (modules.isEmpty()) mc.fontRendererObj.drawString("No mods found", panelX + 14, y + 8, MUTED);
    }

    private void drawTab(int x, int y, int w, String label, boolean active) {
        Gui.drawRect(x, y, x + w, y + 21, active ? 0xFF48271F : 0xFF292930);
        if (active) Gui.drawRect(x, y + 19, x + w, y + 21, ORANGE);
        int tw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, x + (w - tw) / 2, y + 7, active ? ORANGE_LIGHT : MUTED);
    }

    private void drawSettings() {
        Gui.drawRect(panelX, HEADER_H, panelX + PANEL_W, panelH, BG);
        int y = HEADER_H + 8;
        Gui.drawRect(panelX + 7, y, panelX + PANEL_W - 7, y + 25, 0xFF29232A);
        mc.fontRendererObj.drawString("< BACK", panelX + 13, y + 8, ORANGE_LIGHT);
        mc.fontRendererObj.drawString(settingsModule.getName(), panelX + 7, y + 41, WHITE);
        y += 52;
        for (Setting<?> setting : settingsModule.getSettings()) {
            if (y + 25 > panelH - 5) break;
            Gui.drawRect(panelX + 7, y, panelX + PANEL_W - 7, y + 23, ROW);
            mc.fontRendererObj.drawString(setting.getName(), panelX + 12, y + 8, WHITE);
            if (setting instanceof BooleanSetting) {
                boolean on = ((BooleanSetting) setting).getValue();
                Gui.drawRect(panelX + PANEL_W - 39, y + 6, panelX + PANEL_W - 16, y + 17, on ? ORANGE : 0xFF505058);
                Gui.drawRect(on ? panelX + PANEL_W - 27 : panelX + PANEL_W - 38, y + 7,
                        on ? panelX + PANEL_W - 17 : panelX + PANEL_W - 28, y + 16, WHITE);
            } else if (setting instanceof NumberSetting) {
                NumberSetting number = (NumberSetting) setting;
                String value = number.getStep() >= 1 ? String.valueOf(number.getIntValue()) : String.format("%.1f", number.getValue());
                mc.fontRendererObj.drawString(value, panelX + PANEL_W - 43, y + 7, ORANGE_LIGHT);
                Gui.drawRect(panelX + 12, y + 18, panelX + PANEL_W - 48, y + 20, 0xFF55505A);
                int knob = panelX + 12 + (int) ((PANEL_W - 60) * number.getPercent());
                Gui.drawRect(knob - 3, y + 15, knob + 3, y + 22, ORANGE);
            } else if (setting instanceof ModeSetting) {
                String value = ((ModeSetting) setting).getValue();
                mc.fontRendererObj.drawString(value, panelX + 12, y + 17, ORANGE_LIGHT);
            } else if (setting instanceof ColorSetting) {
                int cx = panelX + PANEL_W - 85;
                for (int color : COLORS) {
                    Gui.drawRect(cx, y + 6, cx + 9, y + 16, color);
                    cx += 10;
                }
            }
            y += 27;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        layout();
        if (button != 0 && button != 1) return;

        if (settingsModule != null) {
            if (mouseX >= panelX + 7 && mouseX <= panelX + PANEL_W - 7
                    && mouseY >= HEADER_H + 8 && mouseY <= HEADER_H + 33) {
                settingsModule = null;
                return;
            }
            int y = HEADER_H + 60;
            for (Setting<?> setting : settingsModule.getSettings()) {
                if (mouseY >= y && mouseY < y + 23) {
                    if (setting instanceof BooleanSetting && button == 0) {
                        ((BooleanSetting) setting).toggle();
                    } else if (setting instanceof ModeSetting) {
                        if (button == 1) ((ModeSetting) setting).cycleBack();
                        else ((ModeSetting) setting).cycle();
                    } else if (setting instanceof NumberSetting && button == 0) {
                        draggingSlider = (NumberSetting) setting;
                        updateSlider(mouseX);
                    } else if (setting instanceof ColorSetting && button == 0) {
                        int index = (mouseX - (panelX + PANEL_W - 85)) / 10;
                        if (index >= 0 && index < COLORS.length) ((ColorSetting) setting).setValue(COLORS[index]);
                    }
                    saveConfig();
                    return;
                }
                y += 27;
            }
            return;
        }

        int tabY = HEADER_H + 7;
        if (mouseY >= tabY && mouseY <= tabY + 21) {
            showCosmetics = mouseX >= panelX + PANEL_W / 2;
            return;
        }
        if (showCosmetics) return;

        int searchY = tabY + 27;
        if (mouseY >= searchY && mouseY <= searchY + 22) {
            searchFocused = true;
            return;
        }
        searchFocused = false;

        int catY = searchY + 29;
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int cx = panelX + 7 + (i % 2) * ((PANEL_W - 18) / 2 + 4);
            int cy = catY + (i / 2) * 23;
            if (mouseX >= cx && mouseX <= cx + (PANEL_W - 18) / 2 && mouseY >= cy && mouseY <= cy + 20) {
                category = cats[i];
                return;
            }
        }

        int listY = catY + 3 * 23 + 12;
        List<Module> modules = filteredModules();
        int visible = Math.max(1, (panelH - listY - 8) / ROW_H);
        int offset = offsets.containsKey(category) ? offsets.get(category) : 0;
        int index = (mouseY - listY) / ROW_H + offset;
        if (mouseY < listY || index < 0 || index >= modules.size() || index >= offset + visible) return;
        Module module = modules.get(index);
        if (button == 1 || (mouseX >= panelX + PANEL_W - 53 && module.hasSettings())) {
            if (module.hasSettings()) settingsModule = module;
            return;
        }
        module.toggle();
        saveConfig();
    }

    private void updateSlider(int mouseX) {
        if (draggingSlider == null) return;
        float pct = (float) (mouseX - (panelX + 12)) / (float) (PANEL_W - 60);
        draggingSlider.setPercent(Math.max(0F, Math.min(1F, pct)));
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
        if (draggingSlider != null && button == 0) updateSlider(mouseX);
        super.mouseClickMove(mouseX, mouseY, button, elapsed);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (draggingSlider != null) {
            draggingSlider = null;
            saveConfig();
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0 || settingsModule != null || showCosmetics) return;
        int listY = HEADER_H + 7 + 27 + 29 + 3 * 23 + 12;
        int mouseX = Mouse.getX() * screenWidth() / mc.displayWidth;
        int mouseY = screenHeight() - Mouse.getY() * screenHeight() / mc.displayHeight - 1;
        if (mouseX >= panelX && mouseX <= panelX + PANEL_W && mouseY >= listY && mouseY < panelH) {
            int visible = Math.max(1, (panelH - listY - 8) / ROW_H);
            int max = Math.max(0, filteredModules().size() - visible);
            int current = offsets.containsKey(category) ? offsets.get(category) : 0;
            offsets.put(category, Math.max(0, Math.min(max, current + (wheel > 0 ? -2 : 2))));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            if (settingsModule != null) settingsModule = null;
            else if (searchFocused) searchFocused = false;
            else mc.displayGuiScreen(null);
            return;
        }
        if (!searchFocused) return;
        if (keyCode == 14) {
            if (!search.isEmpty()) search = search.substring(0, search.length() - 1);
            return;
        }
        if (typedChar >= 32 && typedChar < 127) {
            search += typedChar;
            offsets.put(category, 0);
        }
    }

    private void saveConfig() {
        if (AstroClient.INSTANCE.getConfigManager() != null) AstroClient.INSTANCE.getConfigManager().save();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
