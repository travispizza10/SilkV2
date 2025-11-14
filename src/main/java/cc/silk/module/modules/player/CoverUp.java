package cc.silk.module.modules.player;

import cc.silk.event.impl.player.TickEvent;
import cc.silk.mixin.MinecraftClientAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class CoverUp extends Module {

    private final NumberSetting placeDelay = new NumberSetting("Place Delay", 0, 100, 20, 5);

    private boolean isPlacing = false;
    private int placementStep = 0;
    private int savedSlot = -1;
    private float savedYaw = 0;
    private float savedPitch = 0;
    private long lastPlaceTime = 0;
    private boolean hasRotated = false;

    public CoverUp() {
        super("Cover Up", "Rotates down, places 2 cobwebs, and rotates back", -1, Category.PLAYER);
        this.addSettings(placeDelay);
    }

    @Override
    public void onEnable() {
        if (isNull()) {
            this.setEnabled(false);
            return;
        }

        int cobwebSlot = getCobwebSlot();
        if (cobwebSlot == -1) {
            this.setEnabled(false);
            return;
        }

        savedSlot = mc.player.getInventory().selectedSlot;
        savedYaw = mc.player.getYaw();
        savedPitch = mc.player.getPitch();

        isPlacing = true;
        placementStep = 0;
        lastPlaceTime = System.currentTimeMillis();
        hasRotated = false;

        mc.player.setYaw(savedYaw);
        mc.player.setPitch(89.5f);
    }

    @Override
    public void onDisable() {
        if (!isNull() && savedSlot != -1) {
            mc.player.getInventory().selectedSlot = savedSlot;
            mc.player.setYaw(savedYaw);
            mc.player.setPitch(savedPitch);
        }

        isPlacing = false;
        placementStep = 0;
        savedSlot = -1;
        hasRotated = false;
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (isNull() || !isPlacing)
            return;

        if (!hasRotated) {
            hasRotated = true;
            return;
        }

        if (System.currentTimeMillis() - lastPlaceTime < placeDelay.getValue()) {
            return;
        }

        if (placementStep < 2) {
            placeCobweb();
            placementStep++;
            lastPlaceTime = System.currentTimeMillis();
        } else {
            this.setEnabled(false);
        }
    }

    private void placeCobweb() {
        int cobwebSlot = getCobwebSlot();
        if (cobwebSlot == -1) {
            this.setEnabled(false);
            return;
        }

        mc.player.getInventory().selectedSlot = cobwebSlot;
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
    }

    private int getCobwebSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.COBWEB) {
                return i;
            }
        }
        return -1;
    }
}
