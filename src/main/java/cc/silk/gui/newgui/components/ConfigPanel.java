package cc.silk.gui.newgui.components;

import cc.silk.SilkClient;
import cc.silk.profiles.ProfileManager;
import cc.silk.utils.render.nanovg.NanoVGRenderer;
import cc.silk.utils.render.GuiGlowHelper;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigPanel {
    private static final float SCROLL_SPEED = 20f;
    private static final int HEADER_HEIGHT = 16;
    private static final int CONFIG_ENTRY_HEIGHT = 14;
    private static final int MAX_VISIBLE_CONFIGS = 8;
    private static final Color HEADER_COLOR = new Color(28, 28, 32, 255);
    private static final Color PANEL_BG = new Color(18, 18, 22, 255);
    private static final Color ENTRY_HOVER_BG = new Color(40, 40, 45, 255);
    private static final Color TEXT_COLOR = new Color(240, 240, 245, 255);
    private static final float CORNER_RADIUS = 4f;
    private static final long DELETE_CONFIRM_TIMEOUT = 3000;
    private final int width;
    private final List<ConfigEntry> configs = new ArrayList<>();
    private float x, y;
    private boolean dragging = false;
    private float dragOffsetX, dragOffsetY;
    private float scrollOffset = 0f;
    private float targetScrollOffset = 0f;
    private String newConfigName = "";
    private boolean namingNewConfig = false;
    private String selectedConfig = null;
    private String deleteConfirmConfig = null;
    private long deleteConfirmTime = 0;

    public ConfigPanel(float x, float y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        refreshConfigs();
    }

    public void refreshConfigs() {
        configs.clear();
        ProfileManager profileManager = SilkClient.INSTANCE.getProfileManager();
        if (profileManager != null) {
            File profileDir = profileManager.getProfileDir();
            if (profileDir.exists() && profileDir.isDirectory()) {
                File[] files = profileDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (files != null) {
                    Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                    for (File file : files) {
                        String name = file.getName().replace(".json", "");
                        configs.add(new ConfigEntry(name));
                    }
                }
            }
        }
    }

    public void render(int mouseX, int mouseY, float alpha, float scale, int centerX, int centerY) {
        int bgAlpha = (int) (255 * alpha);
        float transparency = cc.silk.module.modules.client.ClientSettingsModule.getGuiTransparency();

        int panelAlpha = (int) (255 * alpha * (1f - transparency));
        Color panelBg = new Color(PANEL_BG.getRed(), PANEL_BG.getGreen(), PANEL_BG.getBlue(), panelAlpha);
        Color headerBg = new Color(HEADER_COLOR.getRed(), HEADER_COLOR.getGreen(), HEADER_COLOR.getBlue(), panelAlpha);
        Color borderColor = new Color(40, 40, 46, panelAlpha);
        Color textColor = new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), bgAlpha);
        Color accentColor = cc.silk.module.modules.client.NewClickGUIModule.getAccentColor();

        int displayedConfigs = Math.min(configs.size() + 1, MAX_VISIBLE_CONFIGS);
        int totalHeight = HEADER_HEIGHT + (displayedConfigs * CONFIG_ENTRY_HEIGHT);
        boolean scrollable = configs.size() + 1 > MAX_VISIBLE_CONFIGS;

        NanoVGRenderer.drawRoundedRect(x, y, width, totalHeight, CORNER_RADIUS, panelBg);
        NanoVGRenderer.drawRoundedRectOutline(x, y, width, totalHeight, CORNER_RADIUS, 1f, borderColor);

        NanoVGRenderer.drawRoundedRect(x, y, width, HEADER_HEIGHT, CORNER_RADIUS, headerBg);
        NanoVGRenderer.drawRect(x, y + HEADER_HEIGHT - CORNER_RADIUS, width, CORNER_RADIUS, headerBg);

        float iconSize = 12f;
        float iconX = Math.round(x + 6 + iconSize / 2f);
        float iconY = Math.round(y + HEADER_HEIGHT / 2f);
        Color iconColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), bgAlpha);
        NanoVGRenderer.drawIcon("\uf0f6", iconX, iconY, iconSize, iconColor);

        float fontSize = 10f;
        float textX = Math.round(x + 6 + iconSize + 4);
        float textY = Math.round(y + (HEADER_HEIGHT - fontSize) / 2f);
        NanoVGRenderer.drawText("Configs", textX, textY, fontSize, textColor);

        float loadIconX = Math.round(x + width - 22f);
        float loadIconY = Math.round(y + HEADER_HEIGHT / 2f);
        boolean loadHover = isMouseOver(mouseX, mouseY, x + width - 28f, y, 14f, HEADER_HEIGHT);
        Color loadColor = (selectedConfig != null && loadHover)
                ? new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), bgAlpha)
                : new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(),
                selectedConfig != null ? (int) (bgAlpha * 0.7f) : (int) (bgAlpha * 0.3f));
        NanoVGRenderer.drawIcon("\uf019", loadIconX, loadIconY, 9f, loadColor);

        float saveIconX = Math.round(x + width - 10f);
        float saveIconY = Math.round(y + HEADER_HEIGHT / 2f);
        boolean saveHover = isMouseOver(mouseX, mouseY, x + width - 16f, y, 12f, HEADER_HEIGHT);
        boolean willOverride = selectedConfig != null;
        Color saveColor = (saveHover || willOverride)
                ? new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), bgAlpha)
                : new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int) (bgAlpha * 0.7f));
        NanoVGRenderer.drawIcon("\uf0c7", saveIconX, saveIconY, 9f, saveColor);

        float contentY = y + HEADER_HEIGHT;
        int moduleAreaHeight = displayedConfigs * CONFIG_ENTRY_HEIGHT;
        if (scrollable) {
            NanoVGRenderer.scissor(x, contentY, width, moduleAreaHeight);
        }

        scrollOffset += (targetScrollOffset - scrollOffset) * 0.3f;

        float currentY = contentY - scrollOffset;
        for (ConfigEntry config : configs) {
            renderConfigEntry(config, currentY, mouseX, mouseY, alpha, textColor, accentColor);
            currentY += CONFIG_ENTRY_HEIGHT;
        }

        renderNewConfigEntry(currentY, mouseX, mouseY, alpha, textColor, accentColor);

        if (scrollable) {
            NanoVGRenderer.resetScissor();

            float scrollbarHeight = ((float) displayedConfigs / (configs.size() + 1)) * moduleAreaHeight;
            float scrollbarY = contentY
                    + (scrollOffset / ((configs.size() + 1) * CONFIG_ENTRY_HEIGHT)) * moduleAreaHeight;

            Color scrollbarColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(),
                    (int) (100 * alpha));
            NanoVGRenderer.drawRoundedRect(x + width - 3, scrollbarY, 2, scrollbarHeight, 1f, scrollbarColor);
        }
    }

    private void renderConfigEntry(ConfigEntry config, float entryY, int mouseX, int mouseY, float alpha,
                                   Color textColor, Color accentColor) {
        boolean hover = isMouseOver(mouseX, mouseY, x, entryY, width, CONFIG_ENTRY_HEIGHT);
        boolean selected = config.name.equals(selectedConfig);

        if (selected) {
            int selectedAlpha = (int) (50 * alpha);
            Color selectedBg = new Color(accentColor.getRed(), accentColor.getGreen(),
                    accentColor.getBlue(), selectedAlpha);
            NanoVGRenderer.drawRect(x, entryY, width, CONFIG_ENTRY_HEIGHT, selectedBg);
        } else if (hover) {
            int hoverAlpha = (int) (30 * alpha);
            Color hoverBg = new Color(ENTRY_HOVER_BG.getRed(), ENTRY_HOVER_BG.getGreen(),
                    ENTRY_HOVER_BG.getBlue(), hoverAlpha);
            NanoVGRenderer.drawRect(x, entryY, width, CONFIG_ENTRY_HEIGHT, hoverBg);
        }

        boolean isDeleteConfirm = config.name.equals(deleteConfirmConfig)
                && System.currentTimeMillis() - deleteConfirmTime < DELETE_CONFIRM_TIMEOUT;

        if (isDeleteConfirm) {
            int deleteAlpha = (int) (80 * alpha);
            Color deleteBg = new Color(180, 50, 50, deleteAlpha);
            NanoVGRenderer.drawRect(x, entryY, width, CONFIG_ENTRY_HEIGHT, deleteBg);
        }

        String displayName = isDeleteConfirm ? "Delete?" : config.name;
        float textWidth = NanoVGRenderer.getTextWidth(displayName, 8f);
        if (!isDeleteConfirm && textWidth > width - 8) {
            while (textWidth > width - 12 && displayName.length() > 3) {
                displayName = displayName.substring(0, displayName.length() - 1);
                textWidth = NanoVGRenderer.getTextWidth(displayName + "...", 8f);
            }
            displayName += "...";
        }

        float entryFontSize = 8f;
        float entryTextX = Math.round(x + (isDeleteConfirm ? (width - textWidth) / 2f : 4f));
        float entryTextY = Math.round(entryY + (CONFIG_ENTRY_HEIGHT - entryFontSize) / 2f);
        NanoVGRenderer.drawText(displayName, entryTextX, entryTextY, entryFontSize, textColor);

        config.bounds = new float[]{x, entryY, width, CONFIG_ENTRY_HEIGHT};
    }

    private void renderNewConfigEntry(float entryY, int mouseX, int mouseY, float alpha,
                                      Color textColor, Color accentColor) {
        boolean hover = isMouseOver(mouseX, mouseY, x, entryY, width, CONFIG_ENTRY_HEIGHT);

        if (hover || namingNewConfig) {
            int hoverAlpha = (int) (50 * alpha);
            Color hoverBg = new Color(accentColor.getRed(), accentColor.getGreen(),
                    accentColor.getBlue(), hoverAlpha);
            NanoVGRenderer.drawRect(x, entryY, width, CONFIG_ENTRY_HEIGHT, hoverBg);
        }

        String displayText = namingNewConfig ? (newConfigName.isEmpty() ? "_" : newConfigName) : "+ New";

        float newEntryFontSize = 8f;
        float newEntryTextX = Math.round(x + 4f);
        float newEntryTextY = Math.round(entryY + (CONFIG_ENTRY_HEIGHT - newEntryFontSize) / 2f);
        NanoVGRenderer.drawText(displayText, newEntryTextX, newEntryTextY, newEntryFontSize, textColor);

        if (namingNewConfig && System.currentTimeMillis() % 1000 < 500) {
            float cursorX = x + 4f + NanoVGRenderer.getTextWidth(newConfigName, 8f);
            NanoVGRenderer.drawRect(cursorX, entryY + 2f, 1f, 10f, textColor);
        }
    }

    private boolean isMouseOver(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1)
            return false;

        if (button == 0 && isMouseOver(mouseX, mouseY, x + width - 28f, y, 14f, HEADER_HEIGHT)) {
            if (selectedConfig != null) {
                ProfileManager pm = SilkClient.INSTANCE.getProfileManager();
                if (pm != null) {
                    pm.loadProfile(selectedConfig);
                }
            }
            return true;
        }

        if (button == 0 && isMouseOver(mouseX, mouseY, x + width - 16f, y, 12f, HEADER_HEIGHT)) {
            ProfileManager pm = SilkClient.INSTANCE.getProfileManager();
            if (pm != null) {
                String saveName = selectedConfig != null ? selectedConfig : "quicksave";
                pm.saveProfile(saveName, true);
                refreshConfigs();
            }
            return true;
        }

        if (isMouseOver(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
            if (button == 1) {
                refreshConfigs();
                return true;
            }
            if (button == 0) {
                dragging = true;
                x = Math.round(x);
                y = Math.round(y);
                dragOffsetX = (float) (mouseX - x);
                dragOffsetY = (float) (mouseY - y);
                return true;
            }
        }

        float contentY = y + HEADER_HEIGHT;
        float currentY = contentY - scrollOffset;

        for (ConfigEntry config : configs) {
            if (config.bounds != null && isMouseOver(mouseX, mouseY,
                    config.bounds[0], config.bounds[1], config.bounds[2], config.bounds[3])) {

                if (button == 0) {
                    selectedConfig = config.name;
                    deleteConfirmConfig = null;
                } else if (button == 1) {
                    boolean isAlreadyConfirming = config.name.equals(deleteConfirmConfig)
                            && System.currentTimeMillis() - deleteConfirmTime < DELETE_CONFIRM_TIMEOUT;

                    if (isAlreadyConfirming) {
                        ProfileManager pm = SilkClient.INSTANCE.getProfileManager();
                        if (pm != null) {
                            File profileFile = new File(pm.getProfileDir(), config.name + ".json");
                            if (profileFile.delete()) {
                                if (config.name.equals(selectedConfig)) {
                                    selectedConfig = null;
                                }
                                deleteConfirmConfig = null;
                                refreshConfigs();
                            }
                        }
                    } else {
                        deleteConfirmConfig = config.name;
                        deleteConfirmTime = System.currentTimeMillis();
                    }
                }
                return true;
            }
            currentY += CONFIG_ENTRY_HEIGHT;
        }

        if (isMouseOver(mouseX, mouseY, x, currentY, width, CONFIG_ENTRY_HEIGHT)) {
            if (namingNewConfig && !newConfigName.isEmpty()) {
                ProfileManager pm = SilkClient.INSTANCE.getProfileManager();
                if (pm != null) {
                    pm.saveProfile(newConfigName);
                    refreshConfigs();
                }
                newConfigName = "";
                namingNewConfig = false;
            } else {
                namingNewConfig = true;
                newConfigName = "";
            }
            return true;
        }

        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            x = Math.round((float) (mouseX - dragOffsetX));
            y = Math.round((float) (mouseY - dragOffsetY));
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int displayedConfigs = Math.min(configs.size() + 1, MAX_VISIBLE_CONFIGS);
        int totalHeight = HEADER_HEIGHT + (displayedConfigs * CONFIG_ENTRY_HEIGHT);

        if (isMouseOver(mouseX, mouseY, x, y, width, totalHeight)) {
            if (configs.size() + 1 > MAX_VISIBLE_CONFIGS) {
                float maxScroll = Math.max(0, (configs.size() + 1 - MAX_VISIBLE_CONFIGS) * CONFIG_ENTRY_HEIGHT);
                targetScrollOffset = Math.max(0,
                        Math.min(maxScroll, targetScrollOffset - (float) amount * SCROLL_SPEED));
            }
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (namingNewConfig) {
            if (keyCode == 259) {
                if (!newConfigName.isEmpty()) {
                    newConfigName = newConfigName.substring(0, newConfigName.length() - 1);
                }
                return true;
            } else if (keyCode == 257 || keyCode == 335) {
                if (!newConfigName.isEmpty()) {
                    ProfileManager pm = SilkClient.INSTANCE.getProfileManager();
                    if (pm != null) {
                        pm.saveProfile(newConfigName);
                        refreshConfigs();
                    }
                    newConfigName = "";
                }
                namingNewConfig = false;
                return true;
            } else if (keyCode == 256) {
                namingNewConfig = false;
                newConfigName = "";
                return true;
            }
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (namingNewConfig) {
            if (Character.isLetterOrDigit(chr) || chr == '_' || chr == '-') {
                newConfigName += chr;
                return true;
            }
        }
        return false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void update(float deltaTime) {
        if (deleteConfirmConfig != null && System.currentTimeMillis() - deleteConfirmTime >= DELETE_CONFIRM_TIMEOUT) {
            deleteConfirmConfig = null;
        }
    }

    public void renderGlow(DrawContext context, float alpha, float scale, int centerX, int centerY) {
        int displayedConfigs = Math.min(configs.size() + 1, MAX_VISIBLE_CONFIGS);
        int totalHeight = HEADER_HEIGHT + (displayedConfigs * CONFIG_ENTRY_HEIGHT);
        
        float transformedX = (x - centerX) * scale + centerX;
        float transformedY = (y - centerY) * scale + centerY;
        float transformedWidth = width * scale;
        float transformedHeight = totalHeight * scale;
        
        GuiGlowHelper.drawGuiGlow(context, transformedX, transformedY, transformedWidth, transformedHeight, CORNER_RADIUS * scale);
    }
    
    private static class ConfigEntry {
        String name;
        float[] bounds;

        ConfigEntry(String name) {
            this.name = name;
        }
    }
}
