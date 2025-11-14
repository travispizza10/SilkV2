package cc.silk.module.modules.misc;

import cc.silk.event.impl.player.TickEvent;
import cc.silk.mixin.MinecraftClientAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.KeybindSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.keybinding.KeyUtils;
import cc.silk.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public final class PearlCatch extends Module {
    private final KeybindSetting pearlChargeKeybind = new KeybindSetting("Pearl Charge Key", GLFW.GLFW_KEY_H, true);
    private final NumberSetting windDelay = new NumberSetting("Wind Delay", 0, 2000, 200, 1);
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 0, 500, 50, 10);

    private final TimerUtil pearlDelayTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();
    private boolean keyPressed = false;
    private boolean pearlThrown = false;
    private int originalSlot = -1;
    private boolean needsSwitchBack = false;

    public PearlCatch() {
        super("Pearl Catch", "Throws pearl then windcharge", -1, Category.MISC);
        this.addSettings(pearlChargeKeybind, windDelay, switchDelay);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(pearlChargeKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || mc.currentScreen != null) return;

        boolean currentKeyState = KeyUtils.isKeyPressed(pearlChargeKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            throwPearl();
        }

        if (pearlThrown && pearlDelayTimer.hasElapsedTime(windDelay.getValueInt())) {
            throwWindCharge();
            pearlThrown = false;
        }

        if (needsSwitchBack && switchTimer.hasElapsedTime(switchDelay.getValueInt())) {
            mc.player.getInventory().selectedSlot = originalSlot;
            needsSwitchBack = false;
            originalSlot = -1;
        }

        keyPressed = currentKeyState;
    }

    private void throwPearl() {
        int pearlSlot = findPearlSlot();
        if (pearlSlot == -1) return;

        if (mc.player.getItemCooldownManager().isCoolingDown(new net.minecraft.item.ItemStack(Items.ENDER_PEARL))) {
            return;
        }

        originalSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = pearlSlot;
        mc.player.swingHand(Hand.MAIN_HAND);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        needsSwitchBack = true;
        switchTimer.reset();

        pearlThrown = true;
        pearlDelayTimer.reset();
    }

    private void throwWindCharge() {
        int windChargeSlot = findWindChargeSlot();
        if (windChargeSlot == -1) return;

        if (mc.player.getItemCooldownManager().isCoolingDown(new net.minecraft.item.ItemStack(Items.WIND_CHARGE))) {
            return;
        }

        originalSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = windChargeSlot;
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        needsSwitchBack = true;
        switchTimer.reset();
    }

    private int findPearlSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }

    private int findWindChargeSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.WIND_CHARGE) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        keyPressed = false;
        pearlThrown = false;
        originalSlot = -1;
        needsSwitchBack = false;
        pearlDelayTimer.reset();
        switchTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        pearlThrown = false;
        super.onDisable();
    }

    @Override
    public int getKey() {
        return -1;
    }
}