package cc.silk.module.modules.combat;

import cc.silk.event.impl.player.TickEvent;
import cc.silk.mixin.MinecraftClientAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.KeybindSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.keybinding.KeyUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class KeyLava extends Module {

    private final KeybindSetting lavaKeybind = new KeybindSetting("Lava Key", GLFW.GLFW_KEY_L, false);
    private final NumberSetting restoreDelayTicks = new NumberSetting("Restore Delay", 1, 20, 2, 1);

    private boolean keyPressed = false;
    private int originalSlot = -1;
    private int tickCounter = 0;
    private boolean cycleComplete = false;
    private boolean pendingRestoreSlot = false;
    private int pendingRestoreTicksLeft = 0;

    public KeyLava() {
        super("Key Lava", "Places lava bucket and picks it back up in a 2-tick cycle", -1, Category.COMBAT);
        this.addSettings(lavaKeybind, restoreDelayTicks);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(lavaKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || !isEnabled())
            return;
        if (mc.currentScreen != null)
            return;

        boolean currentKeyState = KeyUtils.isKeyPressed(lavaKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            startLavaPlace();
        } else if (!currentKeyState && keyPressed) {
            resetState();
        }

        keyPressed = currentKeyState;

        if (keyPressed && !cycleComplete) {
            processLavaCycle();
        }

        if (pendingRestoreSlot) {
            if (pendingRestoreTicksLeft <= 0) {
                restoreOriginalSlot();
                pendingRestoreSlot = false;
            } else {
                pendingRestoreTicksLeft--;
            }
        }
    }

    private void startLavaPlace() {
        originalSlot = mc.player.getInventory().selectedSlot;
        tickCounter = 0;
        cycleComplete = false;
    }

    private void resetState() {
        if (originalSlot != -1 && !pendingRestoreSlot) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }
        originalSlot = -1;
        tickCounter = 0;
        cycleComplete = false;
        pendingRestoreSlot = false;
        pendingRestoreTicksLeft = 0;
    }

    private void processLavaCycle() {
        if (!(mc.crosshairTarget instanceof BlockHitResult blockHit))
            return;

        BlockPos targetBlock = blockHit.getBlockPos();
        var blockState = mc.world.getBlockState(targetBlock);

        if (blockState.isAir())
            return;

        if (tickCounter == 0) {
            if (swapToItem(Items.LAVA_BUCKET)) {
                ((MinecraftClientAccessor) mc).invokeDoItemUse();
                tickCounter++;
            }
        } else if (tickCounter == 1) {
            ((MinecraftClientAccessor) mc).invokeDoItemUse();
            scheduleRestoreOriginalSlot();
            cycleComplete = true;
        }
    }

    private void scheduleRestoreOriginalSlot() {
        if (originalSlot != -1) {
            pendingRestoreSlot = true;
            pendingRestoreTicksLeft = restoreDelayTicks.getValueInt();
        }
    }

    private boolean swapToItem(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                mc.player.getInventory().selectedSlot = i;
                return true;
            }
        }
        return false;
    }

    private void restoreOriginalSlot() {
        if (originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }
    }

    @Override
    public void onEnable() {
        keyPressed = false;
        originalSlot = -1;
        tickCounter = 0;
        cycleComplete = false;
        pendingRestoreSlot = false;
        pendingRestoreTicksLeft = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        resetState();
        super.onDisable();
    }

    @Override
    public int getKey() {
        return -1;
    }
}
