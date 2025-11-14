package cc.silk.module.events;

import cc.silk.SilkClient;
import cc.silk.event.impl.input.MouseClickEvent;
import cc.silk.module.Module;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

public class MouseModuleHandler {

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        if (event.action() == GLFW.GLFW_PRESS) {
            int button = event.button();

            for (Module module : SilkClient.INSTANCE.getModuleManager().getModules()) {
                int moduleKey = module.getKey();

                if (moduleKey == -1 || moduleKey == 0) continue;

                boolean matches = moduleKey == button ||
                        moduleKey == -(button + 1) ||
                        moduleKey == (-100 - button);

                if (matches) {
                    module.toggle();
                    break;
                }
            }
        }
    }
}