package cc.silk.module.modules.player;

import cc.silk.event.impl.player.TickEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.ItemUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;

public final class AutoDoubleHand extends Module {
    private static final BooleanSetting inventorySwitch = new BooleanSetting("Inventory Switch", true);
    private static final NumberSetting totemSlot = new NumberSetting("Totem Slot", 1, 9, 9, 1);
    private static final BooleanSetting healthSwitch = new BooleanSetting("Health Switch", false);
    private static final NumberSetting healthThreshold = new NumberSetting("Health Threshold", 1.0, 20.0, 15.0, 0.5);

    private int originalSlot = -1;

    public AutoDoubleHand() {
        super("Auto Double Hand", "Automatically switches to totem based on conditions", -1, Category.PLAYER);
        this.addSettings(inventorySwitch, totemSlot, healthSwitch, healthThreshold);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (isNull()) return;

        boolean needsTotem = shouldHoldTotem();
        boolean hasTotem = isHoldingTotem();

        if (needsTotem && !hasTotem) {
            switchToTotem();
        } else if (!needsTotem && hasTotem && originalSlot != -1) {
            switchBack();
        }
    }

    private boolean shouldHoldTotem() {
        if (isNull()) return false;

        if (inventorySwitch.getValue() && mc.currentScreen instanceof InventoryScreen) {
            return true;
        }

        return healthSwitch.getValue() && shouldSwitchForHealth();
    }

    private boolean shouldSwitchForHealth() {
        if (mc.player.getHealth() > healthThreshold.getValue()) return false;
        if (mc.player.isUsingItem()) return false;
        return !ItemUtil.isFood(mc.player.getMainHandStack());
    }

    private boolean isHoldingTotem() {
        if (isNull()) return false;
        
        int currentSlot = mc.player.getInventory().selectedSlot;
        if (originalSlot != -1 && currentSlot == (totemSlot.getValueInt() - 1)) {
            return true;
        }
        
        return mc.player.getInventory().getStack(currentSlot).getItem() == Items.TOTEM_OF_UNDYING;
    }

    private void switchToTotem() {
        if (isNull()) return;

        int totemSlotIndex = findTotemInHotbar();
        if (totemSlotIndex == -1 && inventorySwitch.getValue() && mc.currentScreen instanceof InventoryScreen) {
            totemSlotIndex = totemSlot.getValueInt() - 1;
        }

        if (totemSlotIndex != -1) {
            originalSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = totemSlotIndex;
        }
    }

    private void switchBack() {
        if (isNull()) return;
        mc.player.getInventory().selectedSlot = originalSlot;
        originalSlot = -1;
    }

    private int findTotemInHotbar() {
        if (isNull()) return -1;

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        if (!isNull() && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
            originalSlot = -1;
        }
        super.onDisable();
    }
}