package cc.silk.module.modules.client;

import cc.silk.gui.newgui.NewClickGUI;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.ModeSetting;
import cc.silk.gui.theme.ThemeManager;
import org.lwjgl.glfw.GLFW;

public final class ClickGUIModule extends Module {
    public static final ModeSetting theme = new ModeSetting("Theme", ThemeManager.getDefaultName(),
            ThemeManager.getThemeNamesArray());

    public ClickGUIModule() {
        super("Click Gui", "Toggles the Silk GUI", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.CLIENT);
        addSettings(theme);
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen == null) {
            mc.setScreen(new NewClickGUI());
        }
        setEnabled(false);
    }
}
