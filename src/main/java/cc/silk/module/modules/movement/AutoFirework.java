package cc.silk.module.modules.movement;

import cc.silk.event.impl.player.ItemUseEvent;
import cc.silk.event.impl.player.TickEvent;
import cc.silk.mixin.MinecraftClientAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class AutoFirework extends Module {
    private final BooleanSetting onlyWhenFlying = new BooleanSetting("Only When Flying", true);
    private final BooleanSetting respectGapples = new BooleanSetting("Respect Gapples", true);
    private final BooleanSetting respectArmor = new BooleanSetting("Respect Armor", true);
    private final BooleanSetting autoSwitchBack = new BooleanSetting("Auto Switch Back", true);
    private final NumberSetting switchBackDelay = new NumberSetting("Switch Back Delay", 25, 1000, 100, 25);

    private final TimerUtil switchBackTimer = new TimerUtil();
    private boolean wasRightClickPressed = false;
    private boolean pendingSwitchBack = false;
    private int originalSlot = -1;
    private long lastFireworkTime = 0;
    private boolean shouldCancelNextUse = false;

    public AutoFirework() {
        super("Auto Firework", "Automatically uses firework rockets while elytra flying", -1, Category.MOVEMENT);
        this.addSettings(onlyWhenFlying, respectGapples, respectArmor, autoSwitchBack, switchBackDelay);
    }

    @EventHandler
    private void onItemUse(ItemUseEvent event) {
        if (shouldCancelNextUse) {
            event.cancel();
            shouldCancelNextUse = false;
        }
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull())
            return;
        if (mc.currentScreen != null)
            return;

        boolean currentRightClick = mc.options.useKey.isPressed();

        if (!wasRightClickPressed && currentRightClick && shouldUseFirework() && canUseFirework()) {
            lastFireworkTime = System.currentTimeMillis();
            shouldCancelNextUse = true;
            useFirework();
        }

        if (pendingSwitchBack && switchBackTimer.hasElapsedTime(switchBackDelay.getValueInt())) {
            performSwitchBack();
        }

        wasRightClickPressed = currentRightClick;
    }

    private boolean canUseFirework() {
        return System.currentTimeMillis() - lastFireworkTime > 200;
    }

    private boolean shouldUseFirework() {
        if (!isWearingElytra())
            return false;
        if (onlyWhenFlying.getValue() && !isElytraFlying())
            return false;
        if (isHoldingImportantItem())
            return false;
        return findFireworkInHotbar() != -1;
    }

    private boolean isElytraFlying() {
        try {
            return (Boolean) mc.player.getClass().getMethod("isGliding").invoke(mc.player);
        } catch (Throwable ignored) {
            try {
                return (Boolean) mc.player.getClass().getMethod("isFallFlying").invoke(mc.player);
            } catch (Throwable ignored2) {
                return false;
            }
        }
    }

    private boolean isWearingElytra() {
        ItemStack chestplate = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chestplate.isOf(Items.ELYTRA))
            return false;
        return chestplate.getMaxDamage() == 0 || chestplate.getDamage() < chestplate.getMaxDamage() - 1;
    }

    private boolean isHoldingImportantItem() {
        if (respectGapples.getValue()
                && (isGoldenApple(mc.player.getMainHandStack()) || isGoldenApple(mc.player.getOffHandStack()))) {
            return true;
        }
        return respectArmor.getValue()
                && (isArmor(mc.player.getMainHandStack()) || isArmor(mc.player.getOffHandStack()));
    }

    private boolean isGoldenApple(ItemStack stack) {
        return stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
    }

    private boolean isArmor(ItemStack stack) {
        String name = stack.getItem().toString().toLowerCase();
        return name.contains("helmet") || name.contains("chestplate") || name.contains("leggings")
                || name.contains("boots");
    }

    private int findFireworkInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof FireworkRocketItem) {
                return i;
            }
        }
        return -1;
    }

    private void useFirework() {
        int fireworkSlot = findFireworkInHotbar();
        if (fireworkSlot == -1)
            return;

        originalSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = fireworkSlot;

        ((MinecraftClientAccessor) mc).invokeDoItemUse();

        if (autoSwitchBack.getValue()) {
            if (switchBackDelay.getValueInt() > 0) {
                pendingSwitchBack = true;
                switchBackTimer.reset();
            } else {
                mc.player.getInventory().selectedSlot = originalSlot;
            }
        }
    }

    private void performSwitchBack() {
        if (originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }
        pendingSwitchBack = false;
        originalSlot = -1;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        pendingSwitchBack = false;
        originalSlot = -1;
    }
}
