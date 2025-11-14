package cc.silk.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import cc.silk.utils.render.CompatShaders;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class TextureRenderer {
    private TextureRenderer() {
    }

    public static void enableLinearFiltering() {
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }

    public static void disableLinearFiltering() {
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }

    public static void enableMask() {
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(GL11.GL_ALWAYS);
        GlStateManager._depthMask(false);
        GlStateManager._colorMask(false, false, false, false);
    }

    public static void applyMask() {
        GlStateManager._depthFunc(GL11.GL_EQUAL);
        GlStateManager._depthMask(false);
        GlStateManager._colorMask(true, true, true, true);
    }

    public static void disableMask() {
        GlStateManager._depthFunc(GL11.GL_LEQUAL);
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._disableDepthTest();
    }

    public static void drawCenteredQuad(MatrixStack matrices, Identifier texture, float width, float height,
            int color) {
        drawCenteredQuad(matrices, texture, width, height, color, false);
    }

    public static void drawCenteredQuad(MatrixStack matrices, Identifier texture, float width, float height, int color,
            boolean linearFilter) {
        CompatShaders.usePositionTexColor();
        RenderSystem.setShaderTexture(0, texture);

        if (linearFilter) {
            enableLinearFiltering();
        }

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        int a = color >> 24 & 255;
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix, -halfWidth, halfHeight, 0.0f).texture(0.0f, 1.0f).color(r, g, b, a);
        builder.vertex(matrix, halfWidth, halfHeight, 0.0f).texture(1.0f, 1.0f).color(r, g, b, a);
        builder.vertex(matrix, halfWidth, -halfHeight, 0.0f).texture(1.0f, 0.0f).color(r, g, b, a);
        builder.vertex(matrix, -halfWidth, -halfHeight, 0.0f).texture(0.0f, 0.0f).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(builder.end());

        if (linearFilter) {
            disableLinearFiltering();
        }
    }

    public static void drawCenteredQuad(DrawContext context, Identifier texture, float x, float y, float width,
            float height, float rotationDeg, int color) {
        drawCenteredQuad(context, texture, x, y, width, height, rotationDeg, color, false);
    }

    public static void drawCenteredQuad(DrawContext context, Identifier texture, float x, float y, float width,
            float height, float rotationDeg, int color, boolean linearFilter) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0.0f);
        if (rotationDeg != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationDeg));
        }
        drawCenteredQuad(matrices, texture, width, height, color, linearFilter);
        matrices.pop();
    }

    public static void drawMaskedQuad(DrawContext context, Identifier texture, float x, float y, float width,
            float height, int color) {
        drawMaskedQuad(context, texture, x, y, width, height, 0.0f, color, false);
    }

    public static void drawMaskedQuad(DrawContext context, Identifier texture, float x, float y, float width,
            float height, float rotationDeg, int color, boolean linearFilter) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0.0f);
        if (rotationDeg != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationDeg));
        }

        CompatShaders.usePositionTexColor();
        RenderSystem.setShaderTexture(0, texture);

        if (linearFilter) {
            enableLinearFiltering();
        }

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        int a = color >> 24 & 255;
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix, -halfWidth, halfHeight, 0.0f).texture(0.0f, 1.0f).color(r, g, b, a);
        builder.vertex(matrix, halfWidth, halfHeight, 0.0f).texture(1.0f, 1.0f).color(r, g, b, a);
        builder.vertex(matrix, halfWidth, -halfHeight, 0.0f).texture(1.0f, 0.0f).color(r, g, b, a);
        builder.vertex(matrix, -halfWidth, -halfHeight, 0.0f).texture(0.0f, 0.0f).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(builder.end());

        if (linearFilter) {
            disableLinearFiltering();
        }

        matrices.pop();
    }
}
