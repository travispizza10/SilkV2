package cc.silk.module.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import cc.silk.event.impl.render.Render3DEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.ColorSetting;
import cc.silk.module.setting.ModeSetting;
import cc.silk.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

public final class TargetESP extends Module {

    private static final Identifier FIREFLY_TEXTURE = Identifier.of("silk", "imgs/firefly.png");

    private final ModeSetting targets = new ModeSetting("Targets", "Players", "Players", "Living");
    private final NumberSetting layers = new NumberSetting("Layers", 1, 5, 3, 1);
    private final NumberSetting orbsPerLayer = new NumberSetting("Orbs Per Layer", 5, 20, 14, 1);
    private final NumberSetting orbSize = new NumberSetting("Orb Size", 0.1, 0.8, 0.3, 0.05);
    private final NumberSetting speed = new NumberSetting("Speed", 0.5, 5.0, 2.5, 0.1);
    private final NumberSetting heightOffset = new NumberSetting("Height Offset", 0.0, 2.0, 1.0, 0.1);
    private final ModeSetting colorMode = new ModeSetting("Color Mode", "Single", "Single", "Gradient");
    private final ColorSetting color = new ColorSetting("Color", new Color(120, 240, 255, 220));
    private final ColorSetting gradientColor1 = new ColorSetting("Gradient Color 1", new Color(255, 0, 0, 220));
    private final ColorSetting gradientColor2 = new ColorSetting("Gradient Color 2", new Color(0, 0, 255, 220));

    public TargetESP() {
        super("Target ESP", "Ghost-like spiral orbs around players", -1, Category.RENDER);
        addSettings(targets, layers, orbsPerLayer, orbSize, speed, heightOffset, colorMode, color,
                gradientColor1, gradientColor2);
    }

    @EventHandler
    private void onRender3D(Render3DEvent e) {
        if (isNull())
            return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, FIREFLY_TEXTURE);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

        float size = orbSize.getValueFloat();
        float yOff = heightOffset.getValueFloat();
        Color c = color.getValue();
        int numLayers = layers.getValueInt();
        int orbs = orbsPerLayer.getValueInt();
        boolean useGradient = colorMode.isMode("Gradient");

        float iAge = (System.currentTimeMillis() % 100000) / 50.0f;

        for (var entity : mc.world.getEntities()) {
            if (!shouldRender(entity))
                continue;

            double tPosX = entity.prevX + (entity.getX() - entity.prevX) * tickDelta - cam.x;
            double tPosY = entity.prevY + (entity.getY() - entity.prevY) * tickDelta - cam.y + yOff;
            double tPosZ = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta - cam.z;

            if (mc.player.canSee(entity)) {
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);
            } else {
                RenderSystem.disableDepthTest();
            }

            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS,
                    VertexFormats.POSITION_TEXTURE_COLOR);

            float ageMultiplier = iAge * speed.getValueFloat();

            for (int j = 0; j < numLayers; j++) {
                float jOffset = j * 120;
                float jMultiplier = j + 1;

                for (int i = 0; i <= orbs; i++) {
                    float iFloat = (float) i;
                    double radians = Math
                            .toRadians(((iFloat / 1.5f + iAge * speed.getValueFloat()) * 8 + jOffset) % 2880);
                    double sinQuad = Math.sin(Math.toRadians(ageMultiplier + i * jMultiplier) * 3f) / 1.8f;
                    float offset = iFloat / orbs;

                    int orbColor;
                    if (useGradient) {
                        Color blendedColor = blendColors(gradientColor1.getValue(), gradientColor2.getValue(), offset);
                        orbColor = applyOpacity(blendedColor.getRGB(), offset);
                    } else {
                        orbColor = applyOpacity(c.getRGB(), offset);
                    }

                    double ghostX = Math.cos(radians) * entity.getWidth();
                    double ghostY = sinQuad;
                    double ghostZ = Math.sin(radians) * entity.getWidth();

                    MatrixStack matrices = new MatrixStack();
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(mc.gameRenderer.getCamera().getYaw() + 180.0F));
                    matrices.translate(tPosX + ghostX, tPosY + ghostY, tPosZ + ghostZ);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));

                    Matrix4f matrix = matrices.peek().getPositionMatrix();

                    buffer.vertex(matrix, -size, size, 0).texture(0f, 1f).color(orbColor);
                    buffer.vertex(matrix, size, size, 0).texture(1f, 1f).color(orbColor);
                    buffer.vertex(matrix, size, -size, 0).texture(1f, 0).color(orbColor);
                    buffer.vertex(matrix, -size, -size, 0).texture(0, 0).color(orbColor);
                }
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());

            if (mc.player.canSee(entity)) {
                RenderSystem.depthMask(true);
            }
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private Color blendColors(Color color1, Color color2, float ratio) {
        ratio = Math.min(1, Math.max(0, ratio));
        int r = (int) (color1.getRed() * (1 - ratio) + color2.getRed() * ratio);
        int g = (int) (color1.getGreen() * (1 - ratio) + color2.getGreen() * ratio);
        int b = (int) (color1.getBlue() * (1 - ratio) + color2.getBlue() * ratio);
        int a = (int) (color1.getAlpha() * (1 - ratio) + color2.getAlpha() * ratio);
        return new Color(r, g, b, a);
    }

    private int applyOpacity(int colorInt, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        Color color = new Color(colorInt, true);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity))
                .getRGB();
    }

    private boolean shouldRender(net.minecraft.entity.Entity entity) {
        if (entity == mc.player)
            return false;
        if (targets.isMode("Players"))
            return entity instanceof PlayerEntity;
        return entity instanceof LivingEntity;
    }

}


