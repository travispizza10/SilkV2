package cc.silk.gui;

import cc.silk.SilkClient;
import cc.silk.module.Module;
import cc.silk.utils.render.nanovg.NanoVGRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeybindsScreen extends Screen {
    private static final int KEY_SIZE = 45;
    private static final int KEY_SPACING = 4;
    
    private static final int POPUP_WIDTH = 350;
    private static final int POPUP_HEIGHT = 400;
    private static final int MODULE_ENTRY_HEIGHT = 35;
    
    private static final Color TEXT_COLOR = new Color(220, 220, 220, 255);
    private static final Color TEXT_MUTED = new Color(130, 130, 140, 255);
    
    private final Map<Integer, KeyButton> keyButtons = new HashMap<>();
    private String searchQuery = "";
    private List<Module> filteredModules = new ArrayList<>();
    private Module selectedModule = null;
    private Integer hoveredKey = null;
    private boolean moduleSearchActive = false;
    private boolean keyboardInitialized = false;
    
    public KeybindsScreen() {
        super(Text.literal("Keybinds"));
        updateFilteredModules();
    }
    
    private void initializeKeyboard() {
        String[][] layout = {
            {"ESC", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"},
            {"`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "BACKSPACE"},
            {"TAB", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\"},
            {"CAPS", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "ENTER"},
            {"SHIFT", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "SHIFT"},
            {"CTRL", "WIN", "ALT", "SPACE", "ALT", "FN", "CTRL"}
        };
        
        int[][] keyCodes = {
            {GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_F1, GLFW.GLFW_KEY_F2, GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F4, 
             GLFW.GLFW_KEY_F5, GLFW.GLFW_KEY_F6, GLFW.GLFW_KEY_F7, GLFW.GLFW_KEY_F8, GLFW.GLFW_KEY_F9, 
             GLFW.GLFW_KEY_F10, GLFW.GLFW_KEY_F11, GLFW.GLFW_KEY_F12},
            {GLFW.GLFW_KEY_GRAVE_ACCENT, GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4, 
             GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8, GLFW.GLFW_KEY_9, 
             GLFW.GLFW_KEY_0, GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_BACKSPACE},
            {GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_Q, GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_E, GLFW.GLFW_KEY_R, 
             GLFW.GLFW_KEY_T, GLFW.GLFW_KEY_Y, GLFW.GLFW_KEY_U, GLFW.GLFW_KEY_I, GLFW.GLFW_KEY_O, 
             GLFW.GLFW_KEY_P, GLFW.GLFW_KEY_LEFT_BRACKET, GLFW.GLFW_KEY_RIGHT_BRACKET, GLFW.GLFW_KEY_BACKSLASH},
            {GLFW.GLFW_KEY_CAPS_LOCK, GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_F, 
             GLFW.GLFW_KEY_G, GLFW.GLFW_KEY_H, GLFW.GLFW_KEY_J, GLFW.GLFW_KEY_K, GLFW.GLFW_KEY_L, 
             GLFW.GLFW_KEY_SEMICOLON, GLFW.GLFW_KEY_APOSTROPHE, GLFW.GLFW_KEY_ENTER},
            {GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_V, 
             GLFW.GLFW_KEY_B, GLFW.GLFW_KEY_N, GLFW.GLFW_KEY_M, GLFW.GLFW_KEY_COMMA, GLFW.GLFW_KEY_PERIOD, 
             GLFW.GLFW_KEY_SLASH, GLFW.GLFW_KEY_RIGHT_SHIFT},
            {GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_LEFT_ALT, 
             GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_RIGHT_ALT, GLFW.GLFW_KEY_MENU, GLFW.GLFW_KEY_RIGHT_CONTROL}
        };
        
        float maxRowWidth = calculateMaxRowWidth(layout);
        float keyboardStartX = (width - maxRowWidth) / 2f;
        float keyboardHeight = layout.length * (KEY_SIZE + KEY_SPACING);
        float keyboardStartY = (height - keyboardHeight) / 2f;
        
        float currentY = keyboardStartY;
        
        for (int row = 0; row < layout.length; row++) {
            float rowWidth = calculateRowWidth(layout[row]);
            float currentX = keyboardStartX + (maxRowWidth - rowWidth) / 2f;
            
            for (int col = 0; col < layout[row].length; col++) {
                String label = layout[row][col];
                int keyCode = keyCodes[row][col];
                float keyWidth = getKeyWidth(label);
                
                keyButtons.put(keyCode, new KeyButton(currentX, currentY, keyWidth, KEY_SIZE, label, keyCode));
                currentX += keyWidth + KEY_SPACING;
            }
            
            currentY += KEY_SIZE + KEY_SPACING;
        }
    }
    
    private float calculateMaxRowWidth(String[][] layout) {
        float maxWidth = 0;
        for (String[] row : layout) {
            float rowWidth = calculateRowWidth(row);
            if (rowWidth > maxWidth) {
                maxWidth = rowWidth;
            }
        }
        return maxWidth;
    }
    
    private float calculateRowWidth(String[] row) {
        float width = 0;
        for (String label : row) {
            width += getKeyWidth(label) + KEY_SPACING;
        }
        return width - KEY_SPACING;
    }
    
    private float getKeyWidth(String label) {
        return switch (label) {
            case "BACKSPACE" -> KEY_SIZE * 1.5f;
            case "TAB" -> KEY_SIZE * 1.3f;
            case "CAPS" -> KEY_SIZE * 1.5f;
            case "ENTER" -> KEY_SIZE * 1.8f;
            case "SHIFT" -> KEY_SIZE * 1.8f;
            case "SPACE" -> KEY_SIZE * 5.5f;
            case "CTRL", "ALT" -> KEY_SIZE * 1.2f;
            default -> KEY_SIZE;
        };
    }
    
    private void updateFilteredModules() {
        filteredModules.clear();
        String query = searchQuery.toLowerCase();
        
        for (Module module : SilkClient.INSTANCE.getModuleManager().getModules()) {
            if (query.isEmpty() || module.getName().toLowerCase().contains(query)) {
                filteredModules.add(module);
            }
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!keyboardInitialized) {
            initializeKeyboard();
            keyboardInitialized = true;
        }
        
        hoveredKey = null;
        
        NanoVGRenderer.beginFrame();
        
        renderBackground();
        renderHeader();
        
        if (moduleSearchActive) {
            renderModuleSearchPopup(mouseX, mouseY);
        } else {
            renderKeyboard(mouseX, mouseY);
        }
        
        renderInstructions();
        
        NanoVGRenderer.endFrame();
    }
    
    private void renderBackground() {
        NanoVGRenderer.drawRect(0, 0, width, height, cc.silk.module.modules.client.KeybindsModule.getBackgroundColor());
    }
    
    private void renderHeader() {
        String title = "Keybind Editor";
        float fontSize = 24f;
        float titleWidth = NanoVGRenderer.getTextWidth(title, fontSize);
        float titleX = (width - titleWidth) / 2f;
        NanoVGRenderer.drawText(title, titleX, 20, fontSize, TEXT_COLOR);
    }
    
    private void renderModuleSearchPopup(int mouseX, int mouseY) {
        float popupX = (width - POPUP_WIDTH) / 2f;
        float popupY = (height - POPUP_HEIGHT) / 2f;
        
        NanoVGRenderer.drawRoundedRect(popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 8f, new Color(25, 25, 30, 250));
        NanoVGRenderer.drawRoundedRectOutline(popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 8f, 2f, cc.silk.module.modules.client.KeybindsModule.getAccentColor());
        
        renderModulePopupHeader(popupX, popupY);
        renderPopupSearchBar(popupX, popupY);
        renderPopupModuleList(popupX, popupY, mouseX, mouseY);
        renderModulePopupHint(popupX, popupY);
    }
    
    private void renderModulePopupHeader(float popupX, float popupY) {
        String title = selectedModule != null ? "Select key for: " + selectedModule.getName() : "Search Modules";
        float titleSize = 16f;
        float titleWidth = NanoVGRenderer.getTextWidth(title, titleSize);
        NanoVGRenderer.drawText(title, popupX + (POPUP_WIDTH - titleWidth) / 2f, popupY + 15, titleSize, TEXT_COLOR);
    }
    
    private void renderPopupSearchBar(float popupX, float popupY) {
        float searchBarY = popupY + 50;
        float searchBarHeight = 35;
        float searchBarWidth = POPUP_WIDTH - 30;
        
        NanoVGRenderer.drawRoundedRect(popupX + 15, searchBarY, searchBarWidth, searchBarHeight, 6f, new Color(35, 35, 40, 255));
        
        String displayText = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
        Color textColor = searchQuery.isEmpty() ? TEXT_MUTED : TEXT_COLOR;
        NanoVGRenderer.drawText(displayText, popupX + 25, searchBarY + 10, 12f, textColor);
        
        if (System.currentTimeMillis() % 1000 < 500) {
            float cursorX = popupX + 25 + NanoVGRenderer.getTextWidth(searchQuery, 12f);
            NanoVGRenderer.drawRect(cursorX, searchBarY + 8, 1.5f, 18, TEXT_COLOR);
        }
    }
    
    private void renderPopupModuleList(float popupX, float popupY, int mouseX, int mouseY) {
        float searchBarY = popupY + 50;
        float listStartY = searchBarY + 50;
        float listHeight = POPUP_HEIGHT - 110;
        
        NanoVGRenderer.scissor(popupX + 15, searchBarY + 45, POPUP_WIDTH - 30, listHeight);
        
        float moduleY = listStartY;
        for (Module module : filteredModules) {
            if (moduleY > popupY + POPUP_HEIGHT - 20) break;
            
            renderModuleEntry(module, popupX, moduleY, mouseX, mouseY);
            moduleY += MODULE_ENTRY_HEIGHT;
        }
        
        NanoVGRenderer.resetScissor();
    }
    
    private void renderModuleEntry(Module module, float popupX, float moduleY, int mouseX, int mouseY) {
        boolean isHovered = isMouseOverModuleEntry(popupX, moduleY, mouseX, mouseY);
        
        if (isHovered) {
            NanoVGRenderer.drawRoundedRect(popupX + 20, moduleY, POPUP_WIDTH - 40, 30, 4f, cc.silk.module.modules.client.KeybindsModule.getKeyHoverColor());
        }
        
        NanoVGRenderer.drawText(module.getName(), popupX + 30, moduleY + 8, 11f, TEXT_COLOR);
        
        int currentKey = module.getKey();
        if (currentKey != 0 && currentKey != -1) {
            String keyName = getKeyName(currentKey);
            float keyWidth = NanoVGRenderer.getTextWidth(keyName, 9f);
            NanoVGRenderer.drawText(keyName, popupX + POPUP_WIDTH - 30 - keyWidth, moduleY + 10, 9f, new Color(150, 150, 160, 255));
        }
    }
    
    private void renderModulePopupHint(float popupX, float popupY) {
        String hint = selectedModule != null ? "Click a key to bind | ESC to cancel" : "Click a module to select | ESC to cancel";
        float hintSize = 10f;
        float hintWidth = NanoVGRenderer.getTextWidth(hint, hintSize);
        NanoVGRenderer.drawText(hint, popupX + (POPUP_WIDTH - hintWidth) / 2f, popupY + POPUP_HEIGHT - 25, hintSize, TEXT_MUTED);
    }
    
    private void renderKeyboard(int mouseX, int mouseY) {
        for (KeyButton button : keyButtons.values()) {
            boolean isHovered = button.isHovered(mouseX, mouseY);
            if (isHovered) {
                hoveredKey = button.keyCode;
            }
            
            renderKey(button, isHovered);
        }
    }
    
    private void renderKey(KeyButton button, boolean isHovered) {
        Module boundModule = getModuleForKey(button.keyCode);
        boolean isWaitingForKey = selectedModule != null;
        
        Color keyColor = getKeyColor(boundModule, isHovered, isWaitingForKey);
        
        NanoVGRenderer.drawRoundedRect(button.x, button.y, button.width, button.height, 4f, keyColor);
        
        if (boundModule != null) {
            NanoVGRenderer.drawRoundedRectOutline(button.x, button.y, button.width, button.height, 4f, 2f, 
                cc.silk.module.modules.client.KeybindsModule.getKeyAssignedColor());
        }
        
        renderKeyLabel(button);
        
        if (boundModule != null) {
            renderKeyModuleName(button, boundModule);
        }
    }
    
    private Color getKeyColor(Module boundModule, boolean isHovered, boolean isWaitingForKey) {
        if (isWaitingForKey && isHovered) return cc.silk.module.modules.client.KeybindsModule.getKeySelectedColor();
        if (boundModule != null) {
            if (isHovered) {
                Color assigned = cc.silk.module.modules.client.KeybindsModule.getKeyAssignedColor();
                return new Color(
                    Math.min(255, assigned.getRed() + 20),
                    Math.min(255, assigned.getGreen() + 20),
                    Math.min(255, assigned.getBlue() + 13),
                    assigned.getAlpha()
                );
            }
            return cc.silk.module.modules.client.KeybindsModule.getKeyAssignedColor();
        }
        if (isHovered) return cc.silk.module.modules.client.KeybindsModule.getKeyHoverColor();
        return cc.silk.module.modules.client.KeybindsModule.getKeyColor();
    }
    
    private void renderKeyLabel(KeyButton button) {
        float labelSize = button.label.length() > 5 ? 8f : 10f;
        float labelWidth = NanoVGRenderer.getTextWidth(button.label, labelSize);
        float labelX = button.x + (button.width - labelWidth) / 2f;
        float labelY = button.y + (button.height - labelSize) / 2f;
        NanoVGRenderer.drawText(button.label, labelX, labelY, labelSize, TEXT_COLOR);
    }
    
    private void renderKeyModuleName(KeyButton button, Module module) {
        String moduleName = module.getName();
        float moduleSize = 7f;
        float maxWidth = button.width - 4;
        
        moduleName = truncateText(moduleName, moduleSize, maxWidth);
        
        float moduleWidth = NanoVGRenderer.getTextWidth(moduleName, moduleSize);
        float moduleX = button.x + (button.width - moduleWidth) / 2f;
        float moduleY = button.y + button.height - 15;
        
        NanoVGRenderer.drawText(moduleName, moduleX, moduleY, moduleSize, new Color(200, 255, 200, 255));
    }
    
    private String truncateText(String text, float fontSize, float maxWidth) {
        float textWidth = NanoVGRenderer.getTextWidth(text, fontSize);
        
        if (textWidth <= maxWidth) return text;
        
        while (textWidth > maxWidth - 8 && text.length() > 3) {
            text = text.substring(0, text.length() - 1);
            textWidth = NanoVGRenderer.getTextWidth(text + "...", fontSize);
        }
        
        return text + "...";
    }
    
    private void renderInstructions() {
        String instruction;
        if (moduleSearchActive) {
            instruction = selectedModule != null ? "Click a key to bind to " + selectedModule.getName() : "Search and click a module";
        } else if (selectedModule != null) {
            instruction = "Click a key to bind to " + selectedModule.getName();
        } else {
            instruction = "Click any key to search modules";
        }
        
        float instrSize = 12f;
        float instrWidth = NanoVGRenderer.getTextWidth(instruction, instrSize);
        float instrX = (width - instrWidth) / 2f;
        float instrY = height - 40;
        
        Color instrColor = selectedModule != null ? cc.silk.module.modules.client.KeybindsModule.getKeySelectedColor() : new Color(150, 150, 160, 255);
        NanoVGRenderer.drawText(instruction, instrX, instrY, instrSize, instrColor);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        
        if (moduleSearchActive) {
            return handlePopupClick(mouseX, mouseY);
        }
        
        if (selectedModule != null && hoveredKey != null) {
            assignKeyToModule(selectedModule, hoveredKey);
            selectedModule = null;
            return true;
        }
        
        if (hoveredKey != null) {
            openModuleSearch();
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private boolean handlePopupClick(double mouseX, double mouseY) {
        float popupX = (width - POPUP_WIDTH) / 2f;
        float popupY = (height - POPUP_HEIGHT) / 2f;
        
        if (isClickOutsidePopup(mouseX, mouseY, popupX, popupY)) {
            closeModuleSearch();
            return true;
        }
        
        Module clickedModule = getClickedModule(mouseX, mouseY, popupX, popupY);
        if (clickedModule != null) {
            selectedModule = clickedModule;
            closeModuleSearch();
            return true;
        }
        
        return true;
    }
    
    private boolean isClickOutsidePopup(double mouseX, double mouseY, float popupX, float popupY) {
        return mouseX < popupX || mouseX > popupX + POPUP_WIDTH || 
               mouseY < popupY || mouseY > popupY + POPUP_HEIGHT;
    }
    
    private Module getClickedModule(double mouseX, double mouseY, float popupX, float popupY) {
        float searchBarY = popupY + 50;
        float listStartY = searchBarY + 50;
        
        if (mouseX < popupX + 15 || mouseX > popupX + POPUP_WIDTH - 15) return null;
        if (mouseY < searchBarY + 45 || mouseY > popupY + POPUP_HEIGHT - 50) return null;
        
        float moduleY = listStartY;
        for (Module module : filteredModules) {
            if (moduleY > popupY + POPUP_HEIGHT - 20) break;
            
            if (mouseY >= moduleY && mouseY <= moduleY + 30) {
                return module;
            }
            
            moduleY += MODULE_ENTRY_HEIGHT;
        }
        
        return null;
    }
    
    private boolean isMouseOverModuleEntry(float popupX, float moduleY, int mouseX, int mouseY) {
        return mouseX >= popupX + 15 && mouseX <= popupX + POPUP_WIDTH - 15 && 
               mouseY >= moduleY && mouseY <= moduleY + 30;
    }
    
    private void openModuleSearch() {
        moduleSearchActive = true;
        searchQuery = "";
        updateFilteredModules();
    }
    
    private void closeModuleSearch() {
        moduleSearchActive = false;
        searchQuery = "";
        updateFilteredModules();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (moduleSearchActive) {
            return handlePopupKeyPress(keyCode);
        }
        
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (selectedModule != null) {
                selectedModule = null;
                return true;
            }
            close();
            return true;
        }
        
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            openModuleSearch();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private boolean handlePopupKeyPress(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeModuleSearch();
            return true;
        }
        
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                updateFilteredModules();
            }
            return true;
        }
        
        return true;
    }
    
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (moduleSearchActive && chr >= 32 && chr < 127) {
            searchQuery += chr;
            updateFilteredModules();
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
    
    private void assignKeyToModule(Module module, int keyCode) {
        for (Module other : SilkClient.INSTANCE.getModuleManager().getModules()) {
            if (other.getKey() == keyCode && other != module) {
                other.setKey(0);
            }
        }
        module.setKey(keyCode);
    }
    
    private Module getModuleForKey(int keyCode) {
        for (Module module : SilkClient.INSTANCE.getModuleManager().getModules()) {
            if (module.getKey() == keyCode) {
                return module;
            }
        }
        return null;
    }
    
    private String getKeyName(int keyCode) {
        if (keyCode == 0 || keyCode == -1) return "";
        
        KeyButton button = keyButtons.get(keyCode);
        if (button != null) return button.label;
        
        if (keyCode <= -100) {
            return "M" + (-100 - keyCode + 1);
        }
        
        return "KEY " + keyCode;
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    private static class KeyButton {
        final float x, y, width, height;
        final String label;
        final int keyCode;
        
        KeyButton(float x, float y, float width, float height, String label, int keyCode) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
            this.keyCode = keyCode;
        }
        
        boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
}
