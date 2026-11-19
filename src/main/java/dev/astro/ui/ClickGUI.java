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
import java.util.List;

/**
 * FIRED implementation of the Badlion 1.8.9 slideout layout:
 * narrow right-edge drawer, branded top strip, two top tabs, search + cog,
 * three-column square module tiles, page arrows and a separate settings page.
 */
public final class ClickGUI extends GuiScreen {
    private static final int DRAWER_W = 165;
    private static final int TILE = 51;
    private static final int GAP = 2;
    private static final int ACCENT = 0xFFFF5428;
    private static final int ACCENT_LIGHT = 0xFFFF9855;
    private static final int DRAWER = 0xF0141419;
    private static final int TILE_BG = 0xFF292930;
    private static final int TEXT = 0xFFF1F1F3;
    private static final int MUTED = 0xFF9A9AA3;
    private static final int[] SWATCHES = {0xFFFFFFFF,0xFFFF5428,0xFFFF963D,0xFFE83D3D,0xFF42A5F5,0xFF39C987,0xFFB36BFF};

    private String page = "mods";
    private String search = "";
    private boolean searchFocused;
    private int currentPage;
    private Module settingsModule;
    private NumberSetting draggingSlider;
    private int drawerX;
    private int drawerH;
    private List<Module> visibleModules = new ArrayList<Module>();

    private void layout() {
        ScaledResolution sr = new ScaledResolution(mc);
        drawerX = sr.getScaledWidth() - DRAWER_W;
        drawerH = sr.getScaledHeight();
    }

    private List<Module> allModules() {
        List<Module> result = new ArrayList<Module>();
        for (Category category : Category.values()) {
            result.addAll(AstroClient.INSTANCE.getModuleManager().getByCategory(category));
        }
        return result;
    }

    private List<Module> filteredModules() {
        List<Module> result = new ArrayList<Module>();
        String q = search.toLowerCase();
        for (Module module : allModules()) {
            if (q.isEmpty() || module.getName().toLowerCase().contains(q)
                    || (module.getDescription() != null && module.getDescription().toLowerCase().contains(q))) {
                result.add(module);
            }
        }
        return result;
    }

    @Override
    public void initGui() {
        layout();
        page = "mods";
        search = "";
        searchFocused = false;
        currentPage = 0;
        settingsModule = null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        layout();
        drawDefaultBackground();
        Gui.drawRect(drawerX - 2, 0, drawerX, drawerH, 0x55000000);
        Gui.drawRect(drawerX, 0, drawerX + DRAWER_W, drawerH, DRAWER);
        drawBrandAndTabs();
        if (settingsModule != null) drawSettingsPage();
        else if ("mods".equals(page)) drawModsPage(mouseX, mouseY);
        else drawCosmeticsPage();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawBrandAndTabs() {
        // Badlion's original proportions: small logo block at top-left and tab labels beside it.
        Gui.drawRect(drawerX, 0, drawerX + DRAWER_W, 24, 0xFF202026);
        mc.getTextureManager().bindTexture(new ResourceLocation("firedclient", "textures/gui/fired.png"));
        Gui.drawModalRectWithCustomSizedTexture(drawerX + 2, 1, 0, 0, 58, 22, 58, 22);
        Gui.drawRect(drawerX + 60, 0, drawerX + DRAWER_W, 24, 0xFF24242A);
        mc.fontRendererObj.drawString("MODS", drawerX + 65, 8, "mods".equals(page) ? TEXT : MUTED);
        mc.fontRendererObj.drawString("COSMETICS", drawerX + 103, 8, "cosmetics".equals(page) ? TEXT : MUTED);
        if ("mods".equals(page)) Gui.drawRect(drawerX + 63, 21, drawerX + 96, 23, ACCENT);
        else Gui.drawRect(drawerX + 101, 21, drawerX + 160, 23, ACCENT);
    }

    private void drawModsPage(int mouseX, int mouseY) {
        int sy = 32;
        Gui.drawRect(drawerX + 3, sy, drawerX + 135, sy + 15, 0xFF292930);
        Gui.drawRect(drawerX + 3, sy, drawerX + 135, sy + 15, 0xFF292930);
        mc.fontRendererObj.drawString(search.isEmpty() ? "Search" : search, drawerX + 17, sy + 4,
                searchFocused ? TEXT : MUTED);
        mc.fontRendererObj.drawString("⌕", drawerX + 7, sy + 3, MUTED);
        // Cog is a separate control at the right edge, as on the reference panel.
        Gui.drawRect(drawerX + 140, sy, drawerX + 162, sy + 15, 0xFF292930);
        mc.fontRendererObj.drawString("⚙", drawerX + 147, sy + 4, ACCENT_LIGHT);

        visibleModules = filteredModules();
        int cols = 3;
        int rows = Math.max(1, (drawerH - 63) / (TILE + GAP));
        int perPage = cols * rows;
        int pages = Math.max(1, (visibleModules.size() + perPage - 1) / perPage);
        currentPage = Math.max(0, Math.min(currentPage, pages - 1));
        int start = currentPage * perPage;
        for (int i = 0; i < perPage && start + i < visibleModules.size(); i++) {
            Module module = visibleModules.get(start + i);
            int col = i % cols;
            int row = i / cols;
            int x = drawerX + 2 + col * (TILE + GAP);
            int y = 53 + row * (TILE + GAP);
            drawModuleTile(module, x, y, mouseX, mouseY);
        }
        if (visibleModules.isEmpty()) {
            mc.fontRendererObj.drawString("No mods found", drawerX + 12, 61, MUTED);
        }
        int footerY = drawerH - 20;
        Gui.drawRect(drawerX, footerY - 3, drawerX + DRAWER_W, footerY - 2, 0xFF34343B);
        mc.fontRendererObj.drawString("<", drawerX + 12, footerY + 3, currentPage > 0 ? ACCENT_LIGHT : MUTED);
        String count = (currentPage + 1) + " / " + pages;
        mc.fontRendererObj.drawString(count, drawerX + 69 - mc.fontRendererObj.getStringWidth(count) / 2, footerY + 3, MUTED);
        mc.fontRendererObj.drawString(">", drawerX + 145, footerY + 3, currentPage + 1 < pages ? ACCENT_LIGHT : MUTED);
    }

    private void drawModuleTile(Module module, int x, int y, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + TILE && mouseY >= y && mouseY <= y + TILE;
        Gui.drawRect(x, y, x + TILE, y + TILE, hover ? 0xFF37373F : TILE_BG);
        // Small status marker and centered label mirror the original ModButton tile.
        Gui.drawRect(x + 5, y + 5, x + 8, y + 8, module.isEnabled() ? ACCENT : 0xFF62626B);
        String name = module.getName();
        if (mc.fontRendererObj.getStringWidth(name) > TILE - 8) {
            while (name.length() > 3 && mc.fontRendererObj.getStringWidth(name + "...") > TILE - 8) {
                name = name.substring(0, name.length() - 1);
            }
            name += "...";
        }
        mc.fontRendererObj.drawString(name, x + (TILE - mc.fontRendererObj.getStringWidth(name)) / 2,
                y + 17, module.isEnabled() ? ACCENT_LIGHT : TEXT);
        int switchX = x + 10;
        int switchY = y + 35;
        Gui.drawRect(switchX, switchY, switchX + 31, switchY + 10,
                module.isEnabled() ? 0xFF9E3B26 : 0xFF55555E);
        Gui.drawRect(module.isEnabled() ? switchX + 18 : switchX + 1, switchY + 1,
                module.isEnabled() ? switchX + 30 : switchX + 13, switchY + 9, TEXT);
        if (module.hasSettings()) mc.fontRendererObj.drawString("⚙", x + 40, y + 5, MUTED);
    }

    private void drawCosmeticsPage() {
        int y = 34;
        mc.fontRendererObj.drawString("COSMETICS", drawerX + 9, y, ACCENT_LIGHT);
        y += 18;
        String[] entries = {"Capes", "Auras / Trails", "Hand"};
        for (String entry : entries) {
            Gui.drawRect(drawerX + 4, y, drawerX + DRAWER_W - 4, y + 31, TILE_BG);
            mc.fontRendererObj.drawString(entry, drawerX + 12, y + 11, TEXT);
            y += 35;
        }
    }

    private void drawSettingsPage() {
        Gui.drawRect(drawerX, 24, drawerX + DRAWER_W, drawerH, DRAWER);
        Gui.drawRect(drawerX + 4, 32, drawerX + DRAWER_W - 4, 49, 0xFF292930);
        mc.fontRendererObj.drawString("< BACK", drawerX + 10, 37, ACCENT_LIGHT);
        mc.fontRendererObj.drawString(settingsModule.getName(), drawerX + 8, 56, TEXT);
        int y = 72;
        for (Setting<?> setting : settingsModule.getSettings()) {
            if (y + 24 > drawerH - 4) break;
            Gui.drawRect(drawerX + 4, y, drawerX + DRAWER_W - 4, y + 22, TILE_BG);
            mc.fontRendererObj.drawString(setting.getName(), drawerX + 9, y + 7, TEXT);
            if (setting instanceof BooleanSetting) {
                boolean on = ((BooleanSetting) setting).getValue();
                Gui.drawRect(drawerX + 124, y + 6, drawerX + 155, y + 17, on ? ACCENT : 0xFF55555E);
                Gui.drawRect(on ? drawerX + 141 : drawerX + 125, y + 7,
                        on ? drawerX + 154 : drawerX + 138, y + 16, TEXT);
            } else if (setting instanceof NumberSetting) {
                NumberSetting n = (NumberSetting) setting;
                String value = n.getStep() >= 1 ? String.valueOf(n.getIntValue()) : String.format("%.1f", n.getValue());
                mc.fontRendererObj.drawString(value, drawerX + 119, y + 7, ACCENT_LIGHT);
                Gui.drawRect(drawerX + 9, y + 18, drawerX + 155, y + 20, 0xFF55555E);
                int knob = drawerX + 9 + (int)(146 * n.getPercent());
                Gui.drawRect(knob - 3, y + 15, knob + 3, y + 22, ACCENT);
            } else if (setting instanceof ModeSetting) {
                mc.fontRendererObj.drawString(((ModeSetting) setting).getValue(), drawerX + 9, y + 16, ACCENT_LIGHT);
            } else if (setting instanceof ColorSetting) {
                int x = drawerX + 88;
                for (int color : SWATCHES) {
                    Gui.drawRect(x, y + 6, x + 8, y + 16, color);
                    x += 9;
                }
            }
            y += 26;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        layout();
        if (button != 0 && button != 1) return;

        if (settingsModule != null) {
            if (mouseY >= 32 && mouseY <= 49 && mouseX >= drawerX + 4) {
                settingsModule = null;
                return;
            }
            int y = 72;
            for (Setting<?> setting : settingsModule.getSettings()) {
                if (mouseY >= y && mouseY < y + 22 && mouseX >= drawerX + 4) {
                    if (setting instanceof BooleanSetting && button == 0) ((BooleanSetting) setting).toggle();
                    else if (setting instanceof ModeSetting) {
                        if (button == 1) ((ModeSetting) setting).cycleBack();
                        else ((ModeSetting) setting).cycle();
                    } else if (setting instanceof NumberSetting && button == 0) {
                        draggingSlider = (NumberSetting) setting;
                        updateSlider(mouseX);
                    } else if (setting instanceof ColorSetting && button == 0) {
                        int index = (mouseX - (drawerX + 88)) / 9;
                        if (index >= 0 && index < SWATCHES.length) ((ColorSetting) setting).setValue(SWATCHES[index]);
                    }
                    saveConfig();
                    return;
                }
                y += 26;
            }
            return;
        }

        if (mouseY >= 0 && mouseY <= 24) {
            if (mouseX >= drawerX + 100) page = "cosmetics";
            else page = "mods";
            return;
        }
        if (!"mods".equals(page)) return;
        if (mouseY >= 32 && mouseY <= 47) {
            if (mouseX >= drawerX + 140) {
                searchFocused = false;
                settingsModule = null;
                return;
            }
            searchFocused = true;
            return;
        }
        if (mouseY >= drawerH - 23) {
            int pages = Math.max(1, (visibleModules.size() + Math.max(1, ((drawerH - 63) / (TILE + GAP)) * 3) - 1)
                    / Math.max(1, ((drawerH - 63) / (TILE + GAP)) * 3));
            if (mouseX < drawerX + 45 && currentPage > 0) currentPage--;
            else if (mouseX > drawerX + 120 && currentPage + 1 < pages) currentPage++;
            return;
        }
        int cols = 3;
        int rows = Math.max(1, (drawerH - 63) / (TILE + GAP));
        int index = ((mouseY - 53) / (TILE + GAP)) * cols + ((mouseX - (drawerX + 2)) / (TILE + GAP));
        if (mouseY < 53 || mouseX < drawerX + 2 || index < 0 || index >= cols * rows) return;
        int actual = currentPage * cols * rows + index;
        if (actual >= visibleModules.size()) return;
        Module module = visibleModules.get(actual);
        int tileX = drawerX + 2 + (index % cols) * (TILE + GAP);
        int tileY = 53 + (index / cols) * (TILE + GAP);
        if (button == 1 || (module.hasSettings() && mouseX >= tileX + 37 && mouseY <= tileY + 15)) {
            if (module.hasSettings()) settingsModule = module;
            return;
        }
        module.toggle();
        saveConfig();
    }

    private void updateSlider(int mouseX) {
        if (draggingSlider == null) return;
        draggingSlider.setPercent(Math.max(0F, Math.min(1F, (float)(mouseX - (drawerX + 9)) / 146F)));
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
        if (wheel != 0 && settingsModule == null && "mods".equals(page)) {
            int rows = Math.max(1, (drawerH - 63) / (TILE + GAP));
            int perPage = rows * 3;
            int pages = Math.max(1, (filteredModules().size() + perPage - 1) / perPage);
            currentPage = Math.max(0, Math.min(pages - 1, currentPage + (wheel < 0 ? 1 : -1)));
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
            currentPage = 0;
            return;
        }
        if (typedChar >= 32 && typedChar < 127) {
            search += typedChar;
            currentPage = 0;
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
