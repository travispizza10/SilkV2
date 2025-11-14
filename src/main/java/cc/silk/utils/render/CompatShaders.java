package cc.silk.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;

public final class CompatShaders {
    private CompatShaders() {}

    public static void usePositionColor() {
        // Try RenderSystem.setShader(ShaderProgramKey) with ShaderProgramKeys.POSITION_COLOR
        try {
            Class<?> keysClass = Class.forName("net.minecraft.client.render.ShaderProgramKeys");
            Object key = keysClass.getField("POSITION_COLOR").get(null);
            Class<?> keyClass = Class.forName("net.minecraft.client.render.ShaderProgramKey");
            RenderSystem.class.getMethod("setShader", keyClass).invoke(null, key);
            return;
        } catch (Throwable ignored) {
        }

        // Fallback: RenderSystem.setShader(ShaderProgram) using GameRenderer getter via reflection
        try {
            Class<?> gameRenderer = Class.forName("net.minecraft.client.render.GameRenderer");
            Object program = gameRenderer.getMethod("getPositionColorProgram").invoke(null);
            Class<?> programClass = Class.forName("net.minecraft.client.gl.ShaderProgram");
            RenderSystem.class.getMethod("setShader", programClass).invoke(null, program);
        } catch (Throwable ignored) {
            // As a last resort, no-op
        }
    }

    public static void usePositionTexColor() {
        // Try RenderSystem.setShader(ShaderProgramKey) with ShaderProgramKeys.POSITION_TEX_COLOR
        try {
            Class<?> keysClass = Class.forName("net.minecraft.client.render.ShaderProgramKeys");
            Object key = keysClass.getField("POSITION_TEX_COLOR").get(null);
            Class<?> keyClass = Class.forName("net.minecraft.client.render.ShaderProgramKey");
            RenderSystem.class.getMethod("setShader", keyClass).invoke(null, key);
            return;
        } catch (Throwable ignored) {
        }

        // Fallback: RenderSystem.setShader(ShaderProgram) using GameRenderer getter via reflection
        try {
            Class<?> gameRenderer = Class.forName("net.minecraft.client.render.GameRenderer");
            Object program = gameRenderer.getMethod("getPositionTexColorProgram").invoke(null);
            Class<?> programClass = Class.forName("net.minecraft.client.gl.ShaderProgram");
            RenderSystem.class.getMethod("setShader", programClass).invoke(null, program);
        } catch (Throwable ignored) {
            // As a last resort, no-op
        }
    }
}



