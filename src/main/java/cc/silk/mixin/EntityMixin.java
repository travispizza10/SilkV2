package cc.silk.mixin;

import cc.silk.module.modules.render.OutlineESP;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract BlockPos getLandingPos();
    @Shadow public abstract boolean isOnGround();
    @Shadow public abstract World getWorld();
    @Shadow protected abstract void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        OutlineESP outlineESP = OutlineESP.getInstance();
        if (outlineESP != null && outlineESP.isEnabled()) {
            Entity self = (Entity) (Object) this;
            if (outlineESP.shouldEntityGlow(self)) {
                cir.setReturnValue(true);
            }
        }
       
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"))
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
      
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isRemoved()Z"))
    private void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        if (getWorld().isClient) {
            BlockPos blockPos = getLandingPos();
            BlockState blockState = getWorld().getBlockState(blockPos);
            fall(movement.y, isOnGround(), blockState, blockPos);
        }
    }
}
