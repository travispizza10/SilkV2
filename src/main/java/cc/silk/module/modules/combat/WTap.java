package cc.silk.module.modules.combat;


import cc.silk.event.impl.player.AttackEvent;
import cc.silk.event.impl.player.DoAttackEvent;
import cc.silk.event.impl.player.TickEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.keybinding.KeyUtils;
import cc.silk.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

public class WTap extends Module {
    public static final NumberSetting chance = new NumberSetting("Chance (%)", 1, 100, 100, 1);
    private final NumberSetting msDelay = new NumberSetting("Ms", 1, 500, 60, 1);
    private final BooleanSetting onlyOnGround = new BooleanSetting("Only on ground", true);
    boolean wasSprinting;
    TimerUtil timer = new TimerUtil();

    public WTap() {
        super("WTap", "Makes you automatically WTAP", -1, Category.COMBAT);
        this.addSettings(msDelay, chance, onlyOnGround);
    }

    @EventHandler
    private void onAttackEvent(DoAttackEvent event) {
        if (isNull()) return;
        if (Math.random() * 100 > chance.getValueFloat()) return;
        var target = mc.targetedEntity;
        if (!mc.player.isOnGround() && onlyOnGround.getValue()) return;
        if (target == null) return;
        if (!target.isAlive()) return;
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W)) return;
        if (mc.player.isSprinting()) {
            wasSprinting = true;
            mc.options.forwardKey.setPressed(false);
        }
    }


    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W)) return;

        if (wasSprinting) {
            if (timer.hasElapsedTime(msDelay.getValueInt(), true)) {
                mc.options.forwardKey.setPressed(true);
                wasSprinting = false;
            }
        }
    }
}
