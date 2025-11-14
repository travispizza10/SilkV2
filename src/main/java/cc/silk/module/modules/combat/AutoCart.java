package cc.silk.module.modules.combat;

import cc.silk.event.impl.player.TickEvent;
import cc.silk.mixin.MinecraftClientAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class AutoCart extends Module {

    private final BooleanSetting autoSwitch = new BooleanSetting("Auto Switch", true);
    
    private boolean isActive = false;
    private BlockPos targetPos = null;
    private int originalSlot = -1;
    private int tickCounter = 0;
    private boolean hasRail = false;
    private boolean hasTntCart = false;

    public AutoCart() {
        super("Auto Cart", "Places TNT minecarts on rails when shooting arrows", -1, Category.COMBAT);
        this.addSettings(autoSwitch);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        if (mc.player.isUsingItem() && mc.player.getActiveItem().getItem() == Items.BOW) {
            if (!isActive) {
                startPlacing();
            }
        } else if (isActive && !mc.player.isUsingItem()) {
            if (tickCounter == 0) {
                tickCounter = 1;
            }
        }

        if (!isActive) return;

        if (tickCounter == 1) {
            placeRail();
            tickCounter = 2;
        } else if (tickCounter == 2) {
            placeTntCart();
            stopPlacing();
        }
    }

    private void startPlacing() {
        if (isActive) return;

        if (mc.player.getMainHandStack().getItem() != Items.BOW) {
            return;
        }

        BlockPos targetPos = getTargetPosition();
        if (targetPos == null) return;

        this.targetPos = targetPos;
        isActive = true;
        tickCounter = 0;
        originalSlot = mc.player.getInventory().selectedSlot;
    }


    private void stopPlacing() {
        if (!isActive) return;

        if (autoSwitch.getValue() && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }

        resetState();
    }

    private void resetState() {
        isActive = false;
        targetPos = null;
        originalSlot = -1;
        tickCounter = 0;
        hasRail = false;
        hasTntCart = false;
    }

    private void placeRail() {
        if (hasRail) return;

        int railSlot = findAnyRailInHotbar();
        if (railSlot == -1) return;

        mc.player.getInventory().selectedSlot = railSlot;
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        hasRail = true;
    }

    private void placeTntCart() {
        if (hasTntCart) {
            stopPlacing();
            return;
        }

        int tntCartSlot = findItemInHotbar();
        if (tntCartSlot == -1) return;

        mc.player.getInventory().selectedSlot = tntCartSlot;
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        hasTntCart = true;
    }

    private BlockPos getTargetPosition() {
        HitResult hitResult = mc.crosshairTarget;
        if (hitResult == null) return null;
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            return blockHit.getBlockPos().offset(blockHit.getSide());
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            return mc.player.getBlockPos().add(0, 1, 0);
        } else {
            Vec3d cameraPos = mc.player.getCameraPosVec(1.0f);
            Vec3d rotation = mc.player.getRotationVec(1.0f);
            Vec3d end = cameraPos.add(rotation.multiply(5.0));
            
            BlockHitResult blockHit = mc.world.raycast(new RaycastContext(cameraPos, end, 
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            
            if (blockHit != null) {
                return blockHit.getBlockPos().offset(blockHit.getSide());
            } else {
                return mc.player.getBlockPos().add(0, 1, 0);
            }
        }
    }

    private int findItemInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.TNT_MINECART) {
                return i;
            }
        }
        return -1;
    }

    private int findAnyRailInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && isRail(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    private boolean isRail(Item item) {
        return item == Items.RAIL || 
               item == Items.POWERED_RAIL || 
               item == Items.DETECTOR_RAIL || 
               item == Items.ACTIVATOR_RAIL;
    }

    @Override
    public void onEnable() {
        resetState();
    }

    @Override
    public void onDisable() {
        if (isActive) {
            stopPlacing();
        }
    }
}
