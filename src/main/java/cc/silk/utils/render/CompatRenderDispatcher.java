package cc.silk.utils.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public final class CompatRenderDispatcher {
    private CompatRenderDispatcher() {}

    public static void render(EntityRenderDispatcher dispatcher, Entity entity, double x, double y, double z,
                              float yaw, float tickDelta, MatrixStack matrices,
                              VertexConsumerProvider provider, int light) {
        try {
            // Newer signature without tickDelta
            dispatcher.getClass()
                    .getMethod("render", Entity.class, double.class, double.class, double.class,
                            float.class, MatrixStack.class, VertexConsumerProvider.class, int.class)
                    .invoke(dispatcher, entity, x, y, z, yaw, matrices, provider, light);
            return;
        } catch (NoSuchMethodException ignored) {
            // fall through to try older signature
        } catch (Throwable ignored) {
        }

        try {
            // Older signature with tickDelta
            dispatcher.getClass()
                    .getMethod("render", Entity.class, double.class, double.class, double.class,
                            float.class, float.class, MatrixStack.class, VertexConsumerProvider.class, int.class)
                    .invoke(dispatcher, entity, x, y, z, yaw, tickDelta, matrices, provider, light);
        } catch (Throwable ignored) {
            // give up quietly
        }
    }
}


