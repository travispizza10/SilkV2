package cc.silk.mixin;

import cc.silk.SilkClient;
import com.mojang.blaze3d.systems.RenderSystem;
import cc.silk.event.impl.render.Render2DEvent;
import cc.silk.utils.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At(value = "TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        RenderUtils.unscaledProjection();
        RenderUtils.scaledProjection();
        
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        
        SilkClient.INSTANCE.getSilkEventBus()
                .post(new Render2DEvent(context, context.getScaledWindowWidth(), context.getScaledWindowHeight()));
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }
}
