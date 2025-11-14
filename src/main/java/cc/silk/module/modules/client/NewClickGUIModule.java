package cc.silk.module.modules.client;

import cc.silk.gui.newgui.NewClickGUI;
import cc.silk.module.Category;
import cc.silk.module.Module;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class NewClickGUIModule extends Module {

    public NewClickGUIModule() {
        super("NewClickGUI", "Modern NanoVG-based ClickGUI", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.CLIENT);
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen == null) {
            mc.setScreen(new NewClickGUI());
        }
        setEnabled(false);
    }

    public static Color getAccentColor() {
        return ClientSettingsModule.getAccentColor();
    }

    public static String getFontStyle() {
        return ClientSettingsModule.getFontStyle();
    }
}
