package cc.silk.mixin;

import cc.silk.gui.ClickGui;
import cc.silk.gui.newgui.NewClickGUI;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow
    @Nullable
    protected MinecraftClient client;

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void renderBackgroundInject(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (client == null) return;
        Screen currentScreen = client.currentScreen;
        if (currentScreen instanceof ClickGui) {
            ci.cancel();
            return;
        }
        if (currentScreen instanceof NewClickGUI && !cc.silk.module.modules.client.ClientSettingsModule.isGuiBlurEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (client == null) return;
        Screen currentScreen = client.currentScreen;
        if (currentScreen instanceof NewClickGUI || currentScreen instanceof ClickGui) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(515);
            RenderSystem.enableCull();
            RenderSystem.disableScissor();
            RenderSystem.resetTextureMatrix();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
        }
    }
}
