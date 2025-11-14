package cc.silk.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import cc.silk.utils.render.CompatShaders;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL40C;

import java.awt.*;

public class DrawUtils {

    public static void drawRect(MatrixStack matrices, float x, float y, float x2, float y2, Color c) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        setupRender();
        CompatShaders.usePositionColor();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y2, 0.0F).color(c.getRGB());
        buffer.vertex(matrix, x2, y2, 0.0F).color(c.getRGB());
        buffer.vertex(matrix, x2, y, 0.0F).color(c.getRGB());
        buffer.vertex(matrix, x, y, 0.0F).color(c.getRGB());
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void drawRectWithOutline(MatrixStack matrices, float x, float y, float x2, float y2, Color c, Color c2) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        setupRender();
        CompatShaders.usePositionColor();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y2, 0.0F).color(c.getRGB());
        buffer.vertex(matrix, x2, y2, 0.0F).color(c.getRGB());
        buffer.vertex(matrix, x2, y, 0.0F).color(c.getRGB());
        buffer.vertex(matrix, x, y, 0.0F).color(c.getRGB());
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y2, 0.0F).color(c2.getRGB());
        buffer.vertex(matrix, x2, y2, 0.0F).color(c2.getRGB());
        buffer.vertex(matrix, x2, y, 0.0F).color(c2.getRGB());
        buffer.vertex(matrix, x, y, 0.0F).color(c2.getRGB());
        buffer.vertex(matrix, x, y2, 0.0F).color(c2.getRGB());
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void drawHorizontalGradientRect(MatrixStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        setupRender();
        CompatShaders.usePositionColor();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x1, y1, 0.0F).color(startColor.getRGB());
        buffer.vertex(matrix, x1, y2, 0.0F).color(startColor.getRGB());
        buffer.vertex(matrix, x2, y2, 0.0F).color(endColor.getRGB());
        buffer.vertex(matrix, x2, y1, 0.0F).color(endColor.getRGB());
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void rectPoint4VerticalGradient(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float x1, float y1, Color topColor, Color bottomColor) {
        rectPoint4(bufferBuilder, matrix, x, y, x1, y1, topColor, topColor, bottomColor, bottomColor);
    }

    public static void rectPoint4HorizontalGradient(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float x1, float y1, Color leftColor, Color rightColor) {
        rectPoint4(bufferBuilder, matrix, x, y, x1, y1, leftColor, rightColor, rightColor, leftColor);
    }

    public static void rectPoint4(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float x1, float y1, Color color) {
        rectPoint4(bufferBuilder, matrix, x, y, x1, y1, color, color, color, color);
    }

    public static void rectPoint4(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float x1, float y1, Color topLeftColor, Color topRightColor, Color bottomRightColor, Color bottomLeftColor) {
        bufferBuilder.vertex(matrix, x1, y, 0.0F).color(topRightColor.getRGB());
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(topLeftColor.getRGB());
        bufferBuilder.vertex(matrix, x, y1, 0.0F).color(bottomLeftColor.getRGB());
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(bottomRightColor.getRGB());
    }
}
