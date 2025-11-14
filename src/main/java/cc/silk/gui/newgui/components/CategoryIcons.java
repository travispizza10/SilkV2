package cc.silk.gui.newgui.components;

import cc.silk.module.Category;
import cc.silk.utils.render.nanovg.NanoVGRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CategoryIcons {
    private static final Map<Category, String> ICON_MAP = new HashMap<>();

    static {
        ICON_MAP.put(Category.COMBAT, "\uf6cf");
        ICON_MAP.put(Category.PLAYER, "\uf007");
        ICON_MAP.put(Category.MOVEMENT, "\uf70c");
        ICON_MAP.put(Category.RENDER, "\uf06e");
        ICON_MAP.put(Category.MISC, "\uf013");
        ICON_MAP.put(Category.CLIENT, "\uf108");
        ICON_MAP.put(Category.CONFIG, "\uf085");
    }

    public static void drawIcon(Category category, float x, float y, float size, Color color) {
        String icon = ICON_MAP.getOrDefault(category, "●");
        NanoVGRenderer.drawIcon(icon, x, y, size, color);
    }

    public static float getIconSize() {
        return 12f;
    }
}
