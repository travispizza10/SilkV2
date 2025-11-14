package cc.silk.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.awt.*;

public class GlowRenderer {
    private static final String SHADER_NAME = "glow";
    private static int vao = -1;
    private static int vbo = -1;
    private static int ebo = -1;
    private static boolean initialized = false;

    private static final float[] QUAD_VERTICES = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f
    };

    private static final int[] QUAD_INDICES = {
            0, 1, 2,
            2, 3, 0
    };

    public static void init() {
        if (initialized)
            return;

        int shaderProgram = ShaderManager.loadShaderProgram(SHADER_NAME, "shaders/post/glow.vsh", "shaders/post/glow.fsh");
        if (shaderProgram == 0) {
            System.err.println("Failed to load glow shader program");
            return;
        }

        vao = GL30.glGenVertexArrays();
        vbo = GL20.glGenBuffers();
        ebo = GL20.glGenBuffers();

        GL30.glBindVertexArray(vao);

        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, QUAD_VERTICES, GL20.GL_STATIC_DRAW);

        GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, QUAD_INDICES, GL20.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        initialized = true;
    }

    public static void drawGlowBorder(MatrixStack matrices, float x, float y, float width, float height,
            float radius, Color color) {
        drawGlowBorder(matrices, x, y, width, height, radius, color, 1.0f, 10.0f, 15.0f);
    }

    public static void drawGlowBorder(MatrixStack matrices, float x, float y, float width, float height,
            float radius, Color color, float intensity) {
        drawGlowBorder(matrices, x, y, width, height, radius, color, intensity, 10.0f, 15.0f);
    }

    public static void drawGlowBorder(MatrixStack matrices, float x, float y, float width, float height,
            float radius, Color color, float glowIntensity,
            float glowThickness, float bloomRadius) {
        if (!initialized) {
            init();
        }

        Integer shaderProgram = ShaderManager.getShaderProgram(SHADER_NAME);
        if (shaderProgram == null || shaderProgram == 0) {
            return;
        }

        if (vao == -1 || vbo == -1) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        int[] currentVAO = new int[1];
        GL11.glGetIntegerv(GL30.GL_VERTEX_ARRAY_BINDING, currentVAO);
        int[] currentProgram = new int[1];
        GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, currentProgram);
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);

        try {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();

            GL30.glBindVertexArray(vao);

            ShaderManager.useShader(SHADER_NAME);

            ShaderManager.setUniform2f(SHADER_NAME, "u_resolution", screenWidth, screenHeight);
            ShaderManager.setUniform2f(SHADER_NAME, "u_position", x, y);
            ShaderManager.setUniform2f(SHADER_NAME, "u_size", width, height);
            ShaderManager.setUniform1f(SHADER_NAME, "u_radius", radius);
            ShaderManager.setUniform4f(SHADER_NAME, "u_color",
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    color.getAlpha() / 255f);
            ShaderManager.setUniform1f(SHADER_NAME, "u_glowIntensity", glowIntensity);
            ShaderManager.setUniform1f(SHADER_NAME, "u_glowThickness", glowThickness);
            ShaderManager.setUniform1f(SHADER_NAME, "u_bloomRadius", bloomRadius);

            GL20.glDrawElements(GL20.GL_TRIANGLES, 6, GL20.GL_UNSIGNED_INT, 0);

        } finally {
            GL30.glBindVertexArray(currentVAO[0]);
            GL20.glUseProgram(currentProgram[0]);

            RenderSystem.depthMask(true);
            if (depthTestEnabled) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
            if (cullEnabled) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            RenderSystem.defaultBlendFunc();
            if (!blendEnabled) {
                RenderSystem.disableBlend();
            }
        }
    }

    public static void drawPulsingGlow(MatrixStack matrices, float x, float y, float width, float height,
            float radius, Color color, double time) {
        float pulseFactor = (float) (0.7f + 0.3f * Math.sin(time * 2.0));
        drawGlowBorder(matrices, x, y, width, height, radius, color, pulseFactor, 10.0f, 15.0f);
    }

    public static void drawIntenseGlow(MatrixStack matrices, float x, float y, float width, float height,
            float radius, Color color) {
        drawGlowBorder(matrices, x, y, width, height, radius, color, 0.8f, 8.0f, 12.0f);
        drawGlowBorder(matrices, x, y, width, height, radius, color, 0.6f, 12.0f, 20.0f);
        drawGlowBorder(matrices, x, y, width, height, radius, color, 0.4f, 16.0f, 28.0f);
    }

    public static void cleanup() {
        if (vao != -1) {
            GL30.glDeleteVertexArrays(vao);
            vao = -1;
        }
        if (vbo != -1) {
            GL20.glDeleteBuffers(vbo);
            vbo = -1;
        }
        if (ebo != -1) {
            GL20.glDeleteBuffers(ebo);
            ebo = -1;
        }
        initialized = false;
    }
}
