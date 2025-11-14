package cc.silk.module.modules.movement;

import cc.silk.event.impl.player.TickEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import meteordevelopment.orbit.EventHandler;

public final class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Makes you automatically sprint", -1, Category.MOVEMENT);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.options.getSprintToggled().getValue()) mc.options.getSprintToggled().setValue(false);

        mc.options.sprintKey.setPressed(true);
    }
}
