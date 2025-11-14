package cc.silk.gui.newgui.components;

import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.utils.render.nanovg.NanoVGRenderer;
import cc.silk.utils.render.GuiGlowHelper;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {
    private final Category category;
    private float x, y;
    private final int width;
    private final List<ModuleButton> moduleButtons = new ArrayList<>();

    private static final int HEADER_HEIGHT = 16;
    private static final int MODULE_HEIGHT = 14;

    private static final Color HEADER_COLOR = new Color(28, 28, 32, 255);
    private static final Color PANEL_BG = new Color(18, 18, 22, 255);

    private static Color getAccentColor() {
        return cc.silk.module.modules.client.NewClickGUIModule.getAccentColor();
    }

    private static final float CORNER_RADIUS = 4f;

    private boolean dragging = false;
    private float dragOffsetX, dragOffsetY;

    public boolean isDragging() {
        return dragging;
    }

    private SettingsPanel settingsPanel = null;
    private float settingsPanelSourceY = 0;

    private String searchQuery = "";

    private boolean collapsed = false;
    private float collapseProgress = 0f;

    private float scrollOffset = 0f;
    private static final int MAX_VISIBLE_MODULES = 8;
    private static final int SCROLLBAR_WIDTH = 3;

    private Module hoveredModule = null;

    public CategoryPanel(Category category, float x, float y, int width) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;

        initModules();
    }

    private void initModules() {
        List<Module> modules = cc.silk.SilkClient.INSTANCE.getModuleManager().getModulesByCategory(category);

        for (Module module : modules) {
            moduleButtons.add(new ModuleButton(module, 0, 0, width, MODULE_HEIGHT));
        }
    }

    public void update(float deltaTime) {
        if (settingsPanel != null) {
            settingsPanel.update(deltaTime);
        }

        for (ModuleButton button : moduleButtons) {
            button.updateSearch(searchQuery, deltaTime);
        }

        if (collapsed) {
            collapseProgress += deltaTime * 10f;
            if (collapseProgress > 1f)
                collapseProgress = 1f;
        } else {
            collapseProgress -= deltaTime * 10f;
            if (collapseProgress < 0f)
                collapseProgress = 0f;
        }

    }

    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase();
    }

    public void render(int mouseX, int mouseY, float alpha, float scale, int centerX, int centerY) {
        int visibleCount = 0;
        for (ModuleButton button : moduleButtons) {
            if (button.getSearchAlpha() > 0.01f) {
                visibleCount++;
            }
        }

        boolean scrollable = cc.silk.module.modules.client.ClientSettingsModule.isScrollable();
        float expandProgress = 1f - collapseProgress;

        int displayedModules = scrollable ? Math.min(visibleCount, MAX_VISIBLE_MODULES) : visibleCount;
        int moduleAreaHeight = (int) ((displayedModules * MODULE_HEIGHT) * expandProgress);
        int totalHeight = HEADER_HEIGHT + moduleAreaHeight;

        float transparency = cc.silk.module.modules.client.ClientSettingsModule.getGuiTransparency();
        int panelAlpha = (int) (255 * alpha * (1f - transparency));
        Color panelBg = new Color(PANEL_BG.getRed(), PANEL_BG.getGreen(), PANEL_BG.getBlue(), panelAlpha);
        Color headerColor = new Color(HEADER_COLOR.getRed(), HEADER_COLOR.getGreen(), HEADER_COLOR.getBlue(),
                panelAlpha);
        Color borderColor = new Color(40, 40, 46, panelAlpha);

        NanoVGRenderer.drawRoundedRect(x, y, width, totalHeight, CORNER_RADIUS, panelBg);
        NanoVGRenderer.drawRoundedRectOutline(x, y, width, totalHeight, CORNER_RADIUS, 1f, borderColor);

        NanoVGRenderer.drawRoundedRect(x, y, width, HEADER_HEIGHT, CORNER_RADIUS, headerColor);
        NanoVGRenderer.drawRect(x, y + HEADER_HEIGHT - CORNER_RADIUS, width, CORNER_RADIUS, headerColor);

        String categoryName = category.getName();
        float fontSize = 10f;
        float iconSize = CategoryIcon.getIconSize();
        float iconX = Math.round(x + 6);
        float iconY = Math.round(y + (HEADER_HEIGHT - iconSize) / 2f);
        float textX = Math.round(x + 6 + iconSize + 4);
        float textY = Math.round(y + (HEADER_HEIGHT - fontSize) / 2f);

        Color accentColor = getAccentColor();
        Color iconColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(),
                (int) (255 * alpha));
        Color textColor = new Color(240, 240, 245, (int) (255 * alpha));

        CategoryIcon.drawIcon(category, iconX, iconY, iconColor);
        NanoVGRenderer.drawText(categoryName, textX, textY, fontSize, textColor);

        if (expandProgress > 0.01f) {
            NanoVGRenderer.scissor(x, y + HEADER_HEIGHT, width, moduleAreaHeight);

            float moduleY = y + HEADER_HEIGHT - scrollOffset;
            float moduleAlpha = alpha * expandProgress;

            Module newHoveredModule = null;

            for (ModuleButton button : moduleButtons) {
                if (button.getSearchAlpha() > 0.01f) {
                    button.render(x, moduleY, mouseX, mouseY, moduleAlpha);

                    if (mouseX >= x && mouseX <= x + width &&
                            mouseY >= moduleY && mouseY <= moduleY + MODULE_HEIGHT) {
                        newHoveredModule = button.getModule();
                    }

                    moduleY += MODULE_HEIGHT;
                }
            }

            hoveredModule = newHoveredModule;

            NanoVGRenderer.resetScissor();

            if (scrollable && visibleCount > MAX_VISIBLE_MODULES) {
                float scrollbarHeight = ((float) displayedModules / visibleCount) * moduleAreaHeight;
                float scrollbarY = y + HEADER_HEIGHT
                        + (scrollOffset / (visibleCount * MODULE_HEIGHT)) * moduleAreaHeight;

                Color scrollbarColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(),
                        (int) (150 * alpha * expandProgress));
                NanoVGRenderer.drawRoundedRect(x + width - SCROLLBAR_WIDTH - 2, scrollbarY, SCROLLBAR_WIDTH,
                        scrollbarHeight, SCROLLBAR_WIDTH / 2f, scrollbarColor);
            }
        }
    }

    public void renderSettingsPanel(int mouseX, int mouseY, float alpha, int screenWidth, int screenHeight) {
        if (settingsPanel != null && settingsPanel.isAnimating()) {
            settingsPanel.setScreenDimensions(screenWidth, screenHeight);
            settingsPanel.render(mouseX, mouseY, alpha);
        }
    }

    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }

    public boolean hasActiveSettingsPanel() {
        return settingsPanel != null && settingsPanel.isAnimating();
    }

    private boolean isMouseOver(double mouseX, double mouseY) {
        int visibleCount = 0;
        for (ModuleButton button : moduleButtons) {
            if (button.getSearchAlpha() > 0.01f) {
                visibleCount++;
            }
        }
        int totalHeight = HEADER_HEIGHT + (visibleCount * MODULE_HEIGHT);
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + totalHeight;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverHeader(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                x = Math.round(x);
                y = Math.round(y);
                dragOffsetX = (float) (mouseX - x);
                dragOffsetY = (float) (mouseY - y);
                return true;
            } else if (button == 1) {
                collapsed = !collapsed;
                if (collapsed && settingsPanel != null) {
                    settingsPanel.hide();
                }
                return true;
            }
        }

        if (settingsPanel != null && settingsPanel.isAnimating()) {
            if (settingsPanel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        if (collapsed) {
            return false;
        }

        boolean scrollable = cc.silk.module.modules.client.ClientSettingsModule.isScrollable();
        float moduleY = y + HEADER_HEIGHT - (scrollable ? scrollOffset : 0);

        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.getSearchAlpha() > 0.01f) {
                if (moduleButton.mouseClicked(x, moduleY, mouseX, mouseY, button)) {
                    if (button == 1 && moduleButton.getModule().getSettings().size() > 1) {
                        float settingsX = x + width + 5;
                        float settingsY = moduleY;
                        settingsPanelSourceY = moduleY;

                        if (settingsPanel != null && settingsPanel.isVisible()) {
                            settingsPanel.hide();
                        } else {
                            settingsPanel = new SettingsPanel(moduleButton.getModule(), settingsX, settingsY);
                            settingsPanel.show(settingsX, settingsY);
                        }
                    }
                    return true;
                }
                moduleY += MODULE_HEIGHT;
            }
        }

        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        if (settingsPanel != null) {
            settingsPanel.mouseReleased(mouseX, mouseY, button);
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (settingsPanel != null && settingsPanel.mouseDragged(mouseX, mouseY, button)) {
            return true;
        }
        if (dragging) {
            float oldY = y;
            x = Math.round((float) (mouseX - dragOffsetX));
            y = Math.round((float) (mouseY - dragOffsetY));

            if (settingsPanel != null && settingsPanel.isVisible()) {
                float deltaYMove = y - oldY;
                float newSettingsX = x + width + 5;
                settingsPanelSourceY += deltaYMove;
                settingsPanel.show(newSettingsX, settingsPanelSourceY);
            }

            return true;
        }
        return false;
    }

    private boolean isMouseOverHeader(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (settingsPanel != null && settingsPanel.isAnimating()) {
            return settingsPanel.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!cc.silk.module.modules.client.ClientSettingsModule.isScrollable()) {
            return false;
        }

        if (collapsed) {
            return false;
        }

        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        int visibleCount = 0;
        for (ModuleButton button : moduleButtons) {
            if (button.getSearchAlpha() > 0.01f) {
                visibleCount++;
            }
        }

        if (visibleCount <= MAX_VISIBLE_MODULES) {
            return false;
        }

        float maxScroll = (visibleCount - MAX_VISIBLE_MODULES) * MODULE_HEIGHT;
        scrollOffset -= (float) amount * 10f;

        if (scrollOffset < 0)
            scrollOffset = 0;
        if (scrollOffset > maxScroll)
            scrollOffset = maxScroll;

        return true;
    }

    public String getTooltipText() {
        if (hoveredModule != null && cc.silk.module.modules.client.ClientSettingsModule.isModuleDescriptionsEnabled()) {
            return hoveredModule.getDescription();
        }
        return null;
    }

    public int getTooltipX(int mouseX) {
        return mouseX + 10;
    }

    public int getTooltipY(int mouseY) {
        return mouseY + 10;
    }
    
    public void renderGlow(DrawContext context, float alpha, float scale, int centerX, int centerY) {
        int visibleCount = 0;
        for (ModuleButton button : moduleButtons) {
            if (button.getSearchAlpha() > 0.01f) {
                visibleCount++;
            }
        }
        
        boolean scrollable = cc.silk.module.modules.client.ClientSettingsModule.isScrollable();
        float expandProgress = 1f - collapseProgress;
        
        int displayedModules = scrollable ? Math.min(visibleCount, MAX_VISIBLE_MODULES) : visibleCount;
        int moduleAreaHeight = (int) ((displayedModules * MODULE_HEIGHT) * expandProgress);
        
        int totalHeight = HEADER_HEIGHT + moduleAreaHeight;
        
        float transformedX = (x - centerX) * scale + centerX;
        float transformedY = (y - centerY) * scale + centerY;
        float transformedWidth = width * scale;
        float transformedHeight = totalHeight * scale;
        
        GuiGlowHelper.drawGuiGlow(context, transformedX, transformedY, transformedWidth, transformedHeight, CORNER_RADIUS * scale);
    }
    
    public void renderSettingsPanelGlow(DrawContext context, float alpha) {
        if (settingsPanel != null && settingsPanel.isAnimating()) {
            settingsPanel.renderGlow(context, alpha);
        }
    }
}
