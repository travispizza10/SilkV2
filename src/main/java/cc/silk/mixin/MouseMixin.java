package cc.silk.mixin;

import cc.silk.SilkClient;
import cc.silk.event.impl.input.MouseClickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (SilkClient.INSTANCE == null) return;
        if (window != client.getWindow().getHandle()) return;
        if (client.currentScreen != null) return;

        MouseClickEvent event = new MouseClickEvent(button, action, mods);
        SilkClient.INSTANCE.getSilkEventBus().post(event);
    }
}
