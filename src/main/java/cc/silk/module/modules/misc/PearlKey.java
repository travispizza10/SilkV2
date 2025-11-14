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

public final class PearlKey extends Module {
    private final KeybindSetting pearlKeybind = new KeybindSetting("Pearl Key", GLFW.GLFW_KEY_P, true);
    private final NumberSetting throwDelay = new NumberSetting("Throw Delay", 100, 5000, 1000, 50);
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 0, 500, 50, 10);
    private final TimerUtil throwTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();
    private boolean keyPressed = false;
    private int originalSlot = -1;
    private boolean needsSwitchBack = false;

    public PearlKey() {
        super("Pearl Key", "Automatically throws ender pearls when hotkey is pressed", -1, Category.MISC);
        this.addSettings(pearlKeybind, throwDelay, switchDelay);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(pearlKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || mc.currentScreen != null) return;

        boolean currentKeyState = KeyUtils.isKeyPressed(pearlKeybind.getKeyCode());

        if (currentKeyState && !keyPressed && throwTimer.hasElapsedTime(throwDelay.getValueInt(), false)) {
            throwPearl();
            throwTimer.reset();
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

        if (mc.interactionManager != null) {
            ((MinecraftClientAccessor)mc).invokeDoItemUse();
        }

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

    @Override
    public void onEnable() {
        keyPressed = false;
        originalSlot = -1;
        needsSwitchBack = false;
        throwTimer.reset();
        switchTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public int getKey() {
        return -1;
    }
}