package cc.silk.module.modules.combat;

import cc.silk.event.impl.player.AttackEvent;
import cc.silk.event.impl.player.TickEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.friend.FriendManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class StunCob extends Module {
    
    private final NumberSetting predictionTime = new NumberSetting("Prediction Time", 0.1, 2.0, 0.5, 0.1);
    private final NumberSetting placeDelay = new NumberSetting("Place Delay", 0, 200, 50, 10);
    private final BooleanSetting targetPlayers = new BooleanSetting("Target Players", true);
    private final BooleanSetting targetMobs = new BooleanSetting("Target Mobs", false);
    private final BooleanSetting requireCobweb = new BooleanSetting("Require Cobweb", true);
    
    private Entity lastHitTarget = null;
    private long lastHitTime = 0;
    
    public StunCob() {
        super("Stun Cob", "Predicts player movement and places cobweb", -1, Category.COMBAT);
        this.addSettings(predictionTime, placeDelay, targetPlayers, targetMobs, requireCobweb);
    }
    
    @EventHandler
    private void onAttack(AttackEvent event) {
        if (isNull()) return;
        
        Entity target = event.getTarget();
        if (target == null || !isValidTarget(target)) return;
        
        lastHitTarget = target;
        lastHitTime = System.currentTimeMillis();
    }
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (isNull()) return;
        
        if (lastHitTarget == null) return;
        if (System.currentTimeMillis() - lastHitTime < placeDelay.getValue()) return;
        
        if (requireCobweb.getValue() && !hasCobwebInInventory()) return;
        
        Vec3d predictedPos = predictLandingPosition(lastHitTarget);
        if (predictedPos != null) {
            placeCobwebAtPosition(predictedPos);
        }
        
        lastHitTarget = null;
    }
    
    private Vec3d predictLandingPosition(Entity target) {
        if (!(target instanceof LivingEntity)) return null;
        
        Vec3d currentPos = target.getPos();
        Vec3d velocity = target.getVelocity();
        
        boolean isSprinting = mc.player.isSprinting();
        double knockbackMultiplier = isSprinting ? 1.5 : 1.0;
        Vec3d adjustedVelocity = new Vec3d(
            velocity.x * knockbackMultiplier,
            velocity.y,
            velocity.z * knockbackMultiplier
        );
        
        double predictionTime = this.predictionTime.getValueFloat();
        double gravity = 0.08;
        double airResistance = 0.98;
        
        Vec3d predictedPos = currentPos;
        Vec3d predictedVel = adjustedVelocity;
        
        for (double t = 0; t < predictionTime; t += 0.05) {
            predictedVel = new Vec3d(
                predictedVel.x * airResistance,
                predictedVel.y - gravity * 0.05,
                predictedVel.z * airResistance
            );
            predictedPos = predictedPos.add(predictedVel.multiply(0.05));
            
            if (predictedPos.y <= 0) {
                predictedPos = new Vec3d(predictedPos.x, 0, predictedPos.z);
                break;
            }
        }
        
        BlockPos blockPos = new BlockPos((int) Math.floor(predictedPos.x), (int) Math.floor(predictedPos.y), (int) Math.floor(predictedPos.z));
        
        if (canPlaceCobweb(blockPos)) {
            return new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        }
        
        return null;
    }
    
    private boolean canPlaceCobweb(BlockPos pos) {
        if (mc.world == null) return false;
        
        if (!mc.world.getBlockState(pos).isAir()) return false;
        if (!mc.world.getBlockState(pos.down()).isSolidBlock(mc.world, pos.down())) return false;
        
        double distance = mc.player.getPos().distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
        return distance <= 4.5;
    }
    
    private void placeCobwebAtPosition(Vec3d pos) {
        BlockPos blockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
        
        if (!canPlaceCobweb(blockPos)) return;
        
        int cobwebSlot = getCobwebSlot();
        if (cobwebSlot == -1) return;
        
        int originalSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = cobwebSlot;
        
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, 
            new BlockHitResult(pos, Direction.UP, blockPos, false));
        
        mc.player.getInventory().selectedSlot = originalSlot;
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
    
    private boolean hasCobwebInInventory() {
        return getCobwebSlot() != -1;
    }
    
    private boolean isValidTarget(Entity entity) {
        if (entity == null || entity == mc.player || entity == mc.cameraEntity) return false;
        if (!(entity instanceof LivingEntity livingEntity)) return false;
        if (!livingEntity.isAlive() || livingEntity.isDead()) return false;
        if (FriendManager.isFriend(entity.getUuid())) return false;
        
        if (entity instanceof PlayerEntity) {
            return targetPlayers.getValue();
        } else {
            return targetMobs.getValue();
        }
    }
    
    @Override
    public void onEnable() {
        lastHitTarget = null;
        lastHitTime = 0;
    }
    
    @Override
    public void onDisable() {
        lastHitTarget = null;
        lastHitTime = 0;
    }
}
