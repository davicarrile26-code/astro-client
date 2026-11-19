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
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FIRED dropdown Click GUI, adapted for the FIRED module system.
 */
public final class ClickGUI extends GuiScreen {
    private static final int HEADER_H = 25;
    private static final int GAP = 7;
    private static final int PANEL_HEADER_H = 16;
    private static final int MODULE_ROW_H = 15;

    private static final int BG = 0xB80D0D12;
    private static final int HEADER_BG = 0xE6111118;
    private static final int PANEL_BG = 0xC91A1A22;
    private static final int PANEL_HEAD = 0xE522222B;
    private static final int ROW_BG = 0xA522222A;
    private static final int ROW_HOVER = 0xD832323D;
    private static final int ACCENT = 0xFFFF4A16;
    private static final int ACCENT_LIGHT = 0xFFFF8A32;
    private static final int TEXT = 0xFFF4F4F6;
    private static final int MUTED = 0xFF9B9BA5;
    private static final int OFF = 0xFF555560;
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFFFF4A16, 0xFFFF8528, 0xFFFF3030,
            0xFF00D084, 0xFF45A5FF, 0xFFFFD43B
    };

    private final Map<Category, Integer> categoryScroll = new HashMap<Category, Integer>();
    private Category selectedScrollCategory;
    private String searchQuery = "";
    private boolean searchFocused;
    private Module settingsModule;
    private NumberSetting draggingSlider;

    private int[] bounds() {
        ScaledResolution sr = new ScaledResolution(mc);
        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();
        int w = Math.max(300, Math.min(sw - 16, (int)(sw * 0.94f)));
        int h = Math.max(220, Math.min(sh - 16, (int)(sh * 0.90f)));
        return new int[] {(sw - w) / 2, (sh - h) / 2, w, h};
    }

    private List<Module> modules(Category category) {
        List<Module> source = AstroClient.INSTANCE.getModuleManager().getByCategory(category);
        if (searchQuery.isEmpty()) return source;
        List<Module> result = new ArrayList<Module>();
        String query = searchQuery.toLowerCase();
        for (Module module : source) {
            if (module.getName().toLowerCase().contains(query)
                    || module.getDescription().toLowerCase().contains(query)) {
                result.add(module);
            }
        }
        return result;
    }

    private int panelWidth(int width) { return Math.min(120, Math.max(92, (width - GAP * 7) / 6)); }

    private int panelHeight(int height) { return height - HEADER_H - GAP * 2; }

    private int[] panelBounds(int index, int x, int y, int width, int height) {
        int pw = panelWidth(width);
        int total = Category.values().length * pw + (Category.values().length - 1) * GAP;
        int startX = x + (width - total) / 2;
        return new int[] { startX + index * (pw + GAP), y + HEADER_H + GAP, pw, panelHeight(height) };
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int[] b = bounds();
        int x = b[0], y = b[1], w = b[2], h = b[3];
        RenderUtil.drawRoundedRect(x - 3, y - 3, x + w + 3, y + h + 3, 7, 0x50000000);
        RenderUtil.drawRoundedRect(x, y, x + w, y + h, 5, BG);
        drawHeader(mc.fontRendererObj, x, y, w, mouseX, mouseY);

        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            int[] p = panelBounds(i, x, y, w, h);
            drawCategoryPanel(mc.fontRendererObj, categories[i], p[0], p[1], p[2], p[3], mouseX, mouseY);
        }
        if (settingsModule != null) drawSettings(mc.fontRendererObj, x, y, w, h, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHeader(FontRenderer fr, int x, int y, int w, int mx, int my) {
        RenderUtil.drawRoundedRect(x, y, x + w, y + HEADER_H, 5, HEADER_BG);
        RenderUtil.drawRect(x, y + HEADER_H - 1, x + w, y + HEADER_H, 0x55FFFFFF);

        RenderUtil.drawRoundedRect(x + 9, y + 7, x + 31, y + 29, 5, ACCENT);
        RenderUtil.drawRoundedRect(x + 17, y + 10, x + 24, y + 25, 3, 0xFFFFB044);
        RenderUtil.drawRoundedRect(x + 20, y + 15, x + 24, y + 25, 2, 0xFFFFE4A8);
        fr.drawStringWithShadow("FIRED", x + 38, y + 8, TEXT);
        fr.drawStringWithShadow("CLICK GUI", x + 38, y + 21, MUTED);

        int searchW = Math.min(190, Math.max(105, w / 4));
        int sx = x + w - searchW - 10;
        int sy = y + 7;
        RenderUtil.drawRoundedRect(sx, sy, sx + searchW, sy + 24, 4,
                searchFocused ? 0xD52A2020 : 0xA51E1E26);
        RenderUtil.drawOutline(sx, sy, sx + searchW, sy + 24,
                searchFocused ? 0xCCFF6A2A : 0x44666666);
        fr.drawStringWithShadow("Search", sx + 8, sy + 8, MUTED);
        if (!searchQuery.isEmpty()) {
            RenderUtil.drawRect(sx + 43, sy + 5, sx + 44, sy + 19, ACCENT);
            fr.drawStringWithShadow(searchQuery, sx + 49, sy + 8, TEXT);
        }
        fr.drawStringWithShadow("FIRED CLIENT", x + 155, y + 10, MUTED);
    }

    private void drawCategoryPanel(FontRenderer fr, Category category, int x, int y,
                                   int w, int h, int mx, int my) {
        RenderUtil.drawRoundedRect(x, y, x + w, y + h, 4, PANEL_BG);
        RenderUtil.drawRoundedRect(x, y, x + w, y + PANEL_HEADER_H, 4, PANEL_HEAD);
        RenderUtil.drawRect(x, y + PANEL_HEADER_H - 1, x + w, y + PANEL_HEADER_H, 0x35FFFFFF);

        List<Module> list = modules(category);
        fr.drawStringWithShadow(category.getDisplayName().toUpperCase(), x + 5, y + 5, ACCENT_LIGHT);
        fr.drawStringWithShadow(String.valueOf(list.size()), x + w - 15, y + 5, MUTED);

        int viewTop = y + PANEL_HEADER_H + 2;
        int viewBottom = y + h - 3;
        int visibleRows = Math.max(1, (viewBottom - viewTop) / MODULE_ROW_H);
        int maxScroll = Math.max(0, list.size() - visibleRows);
        int scroll = categoryScroll.containsKey(category) ? categoryScroll.get(category) : 0;
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        categoryScroll.put(category, scroll);

        for (int i = 0; i < visibleRows && i + scroll < list.size(); i++) {
            Module module = list.get(i + scroll);
            int ry = viewTop + i * MODULE_ROW_H;
            boolean hover = mx >= x + 4 && mx < x + w - 4 && my >= ry && my < ry + MODULE_ROW_H - 1;
            RenderUtil.drawRect(x + 2, ry, x + w - 2, ry + MODULE_ROW_H - 1,
                    hover ? ROW_HOVER : ROW_BG);
            fr.drawStringWithShadow(module.getName(), x + 5, ry + 4,
                    module.isEnabled() ? ACCENT_LIGHT : TEXT);
            if (module.hasSettings()) {
                fr.drawStringWithShadow("+", x + w - 31, ry + 4, MUTED);
            }
            int tx = x + w - 22;
            int ty = ry + 5;
            RenderUtil.drawRoundedRect(tx, ty, tx + 12, ty + 8, 4,
                    module.isEnabled() ? ACCENT : OFF);
            RenderUtil.drawRoundedRect(module.isEnabled() ? tx + 7 : tx + 1,
                    ty + 1, module.isEnabled() ? tx + 13 : tx + 7, ty + 8, 3, 0xFFFFFFFF);
        }

        if (maxScroll > 0) {
            int trackX = x + w - 3;
            int trackY = viewTop;
            int trackH = viewBottom - viewTop;
            int thumbH = Math.max(14, trackH * visibleRows / list.size());
            int thumbY = trackY + (trackH - thumbH) * scroll / maxScroll;
            RenderUtil.drawRoundedRect(trackX, trackY, trackX + 2, trackY + trackH, 1, 0x44FFFFFF);
            RenderUtil.drawRoundedRect(trackX, thumbY, trackX + 2, thumbY + thumbH, 1, ACCENT);
        }
    }

    private void drawSettings(FontRenderer fr, int x, int y, int w, int h, int mx, int my) {
        int pw = Math.min(300, w - 24);
        int ph = Math.min(h - 24, 54 + settingsModule.getSettings().size() * 25);
        int px = x + (w - pw) / 2;
        int py = y + (h - ph) / 2;
        RenderUtil.drawRoundedRect(px - 3, py - 3, px + pw + 3, py + ph + 3, 6, 0x90000000);
        RenderUtil.drawRoundedRect(px, py, px + pw, py + ph, 5, 0xF015151B);
        RenderUtil.drawRoundedRect(px, py, px + pw, py + 28, 4, 0xF0262022);
        fr.drawStringWithShadow(settingsModule.getName() + " SETTINGS", px + 10, py + 10, ACCENT_LIGHT);
        fr.drawStringWithShadow("X", px + pw - 16, py + 10, TEXT);

        int ry = py + 35;
        for (Setting<?> setting : settingsModule.getSettings()) {
            if (ry + 21 > py + ph - 4) break;
            RenderUtil.drawRoundedRect(px + 7, ry, px + pw - 7, ry + 21, 2, 0xAA292931);
            fr.drawStringWithShadow(setting.getName(), px + 13, ry + 7, TEXT);
            if (setting instanceof BooleanSetting) {
                boolean on = ((BooleanSetting) setting).getValue();
                RenderUtil.drawRoundedRect(px + pw - 39, ry + 5, px + pw - 14, ry + 16, 4,
                        on ? ACCENT : OFF);
                RenderUtil.drawRoundedRect(on ? px + pw - 26 : px + pw - 37, ry + 6,
                        on ? px + pw - 16 : px + pw - 27, ry + 15, 4, 0xFFFFFFFF);
            } else if (setting instanceof NumberSetting) {
                NumberSetting number = (NumberSetting) setting;
                String value = number.getStep() >= 1
                        ? String.valueOf(number.getIntValue())
                        : String.format("%.1f", number.getValue());
                fr.drawStringWithShadow(value, px + pw - 48, ry + 7, ACCENT_LIGHT);
            } else if (setting instanceof ModeSetting) {
                fr.drawStringWithShadow(((ModeSetting) setting).getValue(),
                        px + pw - 100, ry + 7, ACCENT_LIGHT);
            } else if (setting instanceof ColorSetting) {
                int cx = px + pw - 88;
                for (int color : COLORS) {
                    RenderUtil.drawRect(cx, ry + 6, cx + 9, ry + 15, color);
                    cx += 11;
                }
            }
            ry += 25;
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) throws IOException {
        super.mouseClicked(mx, my, button);
        int[] b = bounds();
        int x = b[0], y = b[1], w = b[2], h = b[3];
        int searchW = Math.min(190, Math.max(105, w / 4));
        int sx = x + w - searchW - 10;

        if (button == 0 && mx >= sx && mx <= sx + searchW && my >= y + 7 && my <= y + 31) {
            searchFocused = true;
            return;
        }
        if (button == 0 && searchFocused && (my < y || my > y + HEADER_H)) searchFocused = false;

        if (settingsModule != null) {
            int pw = Math.min(300, w - 24);
            int ph = Math.min(h - 24, 54 + settingsModule.getSettings().size() * 25);
            int px = x + (w - pw) / 2;
            int py = y + (h - ph) / 2;
            if (button == 0 && mx >= px + pw - 25 && mx <= px + pw
                    && my >= py && my <= py + 28) {
                settingsModule = null;
                return;
            }
            int ry = py + 35;
            for (Setting<?> setting : settingsModule.getSettings()) {
                if (my >= ry && my < ry + 21 && mx >= px + 7 && mx <= px + pw - 7) {
                    if (setting instanceof BooleanSetting && button == 0) {
                        ((BooleanSetting) setting).toggle();
                    } else if (setting instanceof ModeSetting) {
                        if (button == 1) ((ModeSetting) setting).cycleBack();
                        else ((ModeSetting) setting).cycle();
                    } else if (setting instanceof ColorSetting && button == 0) {
                        int cx = px + pw - 88;
                        for (int color : COLORS) {
                            if (mx >= cx && mx < cx + 9) {
                                ((ColorSetting) setting).setValue(color);
                                break;
                            }
                            cx += 11;
                        }
                    } else if (setting instanceof NumberSetting && button == 0) {
                        draggingSlider = (NumberSetting) setting;
                        updateSlider(mx, px, pw);
                    }
                    saveConfig();
                    return;
                }
                ry += 25;
            }
            if (mx >= px && mx <= px + pw && my >= py && my <= py + ph) return;
            settingsModule = null;
        }

        if (button != 0 && button != 1) return;
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            Category category = categories[i];
            int[] p = panelBounds(i, x, y, w, h);
            int px = p[0], py = p[1], pw = p[2], panelH = p[3];
            int viewTop = py + PANEL_HEADER_H + 2;
            int viewBottom = py + panelH - 3;
            if (mx < px || mx > px + pw || my < py || my > py + panelH) continue;
            if (my < viewTop) return;

            List<Module> list = modules(category);
            int visibleRows = Math.max(1, (viewBottom - viewTop) / MODULE_ROW_H);
            int scroll = categoryScroll.containsKey(category) ? categoryScroll.get(category) : 0;
            for (int row = 0; row < visibleRows && row + scroll < list.size(); row++) {
                int ry = viewTop + row * MODULE_ROW_H;
                if (my < ry || my >= ry + MODULE_ROW_H) continue;
                Module module = list.get(row + scroll);
                if (button == 1 && module.hasSettings()) {
                    settingsModule = module;
                    return;
                }
                if (button == 0) {
                    if (mx >= px + pw - 38 && module.hasSettings()) settingsModule = module;
                    else module.toggle();
                    saveConfig();
                    return;
                }
            }
        }
    }

    private void updateSlider(int mouseX, int px, int pw) {
        if (draggingSlider == null) return;
        float pct = (float)(mouseX - (px + 110)) / (float)Math.max(1, pw - 130);
        draggingSlider.setPercent(Math.max(0f, Math.min(1f, pct)));
    }

    @Override
    protected void mouseClickMove(int mx, int my, int button, long elapsed) {
        if (draggingSlider != null && button == 0) {
            int[] b = bounds();
            int pw = Math.min(300, b[2] - 24);
            int px = b[0] + (b[2] - pw) / 2;
            updateSlider(mx, px, pw);
        }
        super.mouseClickMove(mx, my, button, elapsed);
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        if (draggingSlider != null) {
            draggingSlider = null;
            saveConfig();
        }
        super.mouseReleased(mx, my, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        int mx = Mouse.getX() * width / mc.displayWidth;
        int my = height - Mouse.getY() * height / mc.displayHeight - 1;
        int[] b = bounds();
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            int[] p = panelBounds(i, b[0], b[1], b[2], b[3]);
            if (mx >= p[0] && mx <= p[0] + p[2] && my >= p[1] && my <= p[1] + p[3]) {
                Category category = categories[i];
                int current = categoryScroll.containsKey(category) ? categoryScroll.get(category) : 0;
                int max = Math.max(0, modules(category).size()
                        - Math.max(1, (p[3] - PANEL_HEADER_H - 7) / MODULE_ROW_H));
                categoryScroll.put(category, Math.max(0, Math.min(max, current + (wheel > 0 ? -2 : 2))));
                break;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            if (settingsModule != null) settingsModule = null;
            else if (!searchQuery.isEmpty()) searchQuery = "";
            else mc.displayGuiScreen(null);
            return;
        }
        if (!searchFocused) return;
        if (keyCode == 14) {
            if (!searchQuery.isEmpty()) searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return;
        }
        if (typedChar >= 32 && typedChar < 127) searchQuery += typedChar;
    }

    private void saveConfig() {
        if (AstroClient.INSTANCE.getConfigManager() != null) {
            AstroClient.INSTANCE.getConfigManager().save();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
