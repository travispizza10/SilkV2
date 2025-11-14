package cc.silk.gui.theme;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Simple Theme registry. Register default themes here and use ThemeManager.getTheme(name) to retrieve.
 */
public final class ThemeManager {
    private static final Map<String, Theme> THEMES = new LinkedHashMap<>();
    private static final String DEFAULT_NAME;

    static {
        // Default theme: matches previous GUI colours
        Theme defaultTheme = new Theme("Default",
                new Color(0, 0, 0, 200),
                new Color(8, 8, 10, 134),
                new Color(12, 12, 12, 200),
                new Color(18, 18, 20, 255),
                new Color(16, 16, 18, 255),
                new Color(18, 18, 20, 200),
                new Color(28, 28, 32, 200),
                new Color(124, 77, 255, 255),
                Color.WHITE,
                new Color(180, 180, 200));

        Theme purple = new Theme("Purple",
                new Color(6, 3, 15, 200),
                new Color(18, 8, 40, 160),
                new Color(14, 10, 20, 220),
                new Color(26, 12, 48, 240),
                new Color(18, 10, 28, 230),
                new Color(20, 12, 30, 200),
                new Color(36, 24, 48, 200),
                new Color(156, 100, 255, 255),
                Color.WHITE,
                new Color(200, 190, 230));

        Theme solar = new Theme("Solar",
                new Color(10, 16, 10, 200),
                new Color(32, 38, 22, 160),
                new Color(20, 24, 18, 200),
                new Color(34, 36, 26, 255),
                new Color(28, 30, 22, 255),
                new Color(22, 26, 18, 200),
                new Color(30, 34, 24, 200),
                new Color(180, 133, 0, 255),
                Color.WHITE,
                new Color(200, 200, 180));

        register(defaultTheme);
        register(purple);
        register(solar);

        DEFAULT_NAME = defaultTheme.name();
    }

    public static void register(Theme theme) {
        THEMES.put(theme.name(), theme);
    }

    public static Theme getTheme(String name) {
        return THEMES.getOrDefault(name, THEMES.values().iterator().next());
    }

    public static String[] getThemeNamesArray() {
        List<String> keys = new ArrayList<>(THEMES.keySet());
        return keys.toArray(new String[0]);
    }

    public static List<Theme> getAllThemes() {
        return Collections.unmodifiableList(new ArrayList<>(THEMES.values()));
    }

    public static String getDefaultName() {
        return DEFAULT_NAME;
    }
}
