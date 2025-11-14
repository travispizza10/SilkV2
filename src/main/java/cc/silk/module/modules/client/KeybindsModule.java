package cc.silk.module.modules.client;

import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.ColorSetting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class KeybindsModule extends Module {
    public static final ColorSetting backgroundColor = new ColorSetting("Background Color", new Color(18, 18, 22, 240));
    public static final ColorSetting keyColor = new ColorSetting("Key Color", new Color(35, 35, 40, 255));
    public static final ColorSetting keyHoverColor = new ColorSetting("Key Hover Color", new Color(50, 50, 60, 255));
    public static final ColorSetting keyAssignedColor = new ColorSetting("Key Assigned Color", new Color(88, 101, 242, 255));
    public static final ColorSetting keySelectedColor = new ColorSetting("Key Selected Color", new Color(100, 255, 100, 255));
    public static final ColorSetting accentColor = new ColorSetting("Accent Color", new Color(88, 101, 242, 255));

    public KeybindsModule() {
        super("Keybinds", "Visual keyboard keybind editor", GLFW.GLFW_KEY_K, Category.CLIENT);
        addSettings(backgroundColor, keyColor, keyHoverColor, keyAssignedColor, keySelectedColor, accentColor);
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen == null) {
            mc.setScreen(new cc.silk.gui.KeybindsScreen());
        }
        setEnabled(false);
    }

    public static Color getBackgroundColor() {
        return backgroundColor.getValue();
    }

    public static Color getKeyColor() {
        return keyColor.getValue();
    }

    public static Color getKeyHoverColor() {
        return keyHoverColor.getValue();
    }

    public static Color getKeyAssignedColor() {
        return keyAssignedColor.getValue();
    }

    public static Color getKeySelectedColor() {
        return keySelectedColor.getValue();
    }

    public static Color getAccentColor() {
        return accentColor.getValue();
    }
}

