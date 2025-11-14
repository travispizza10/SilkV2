package cc.silk.utils.mc;

import lombok.experimental.UtilityClass;

import static cc.silk.SilkClient.mc;

@UtilityClass
public final class MovementUtil {

    public static boolean isMoving() {
        return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
    }
}
