package cc.silk.utils.render;

import cc.silk.module.modules.client.ClientSettingsModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class GuiGlowHelper {
    
    public static void drawGuiGlow(DrawContext context, float x, float y, float width, float height, float radius) {
        if (!ClientSettingsModule.isGuiGlowEnabled()) {
            return;
        }
        
        MatrixStack matrices = context.getMatrices();
        Color glowColor = ClientSettingsModule.getGlowColor();
        float intensity = ClientSettingsModule.getGlowIntensity();
        float thickness = ClientSettingsModule.getGlowThickness();
        float bloomRadius = ClientSettingsModule.getBloomRadius();
        
        GlowRenderer.drawGlowBorder(matrices, x, y, width, height, 
                                   radius, glowColor, intensity, thickness, bloomRadius);
    }
    
    public static void drawGuiGlow(DrawContext context, float x, float y, float width, float height, 
                                  float radius, Color customColor) {
        if (!ClientSettingsModule.isGuiGlowEnabled()) {
            return;
        }
        
        MatrixStack matrices = context.getMatrices();
        float intensity = ClientSettingsModule.getGlowIntensity();
        float thickness = ClientSettingsModule.getGlowThickness();
        float bloomRadius = ClientSettingsModule.getBloomRadius();
        
        GlowRenderer.drawGlowBorder(matrices, x, y, width, height, 
                                   radius, customColor, intensity, thickness, bloomRadius);
    }
    
    public static void drawCustomGlow(DrawContext context, float x, float y, float width, float height,
                                     float radius, Color color, float intensity, 
                                     float thickness, float bloomRadius) {
        MatrixStack matrices = context.getMatrices();
        GlowRenderer.drawGlowBorder(matrices, x, y, width, height, radius, color, 
                                   intensity, thickness, bloomRadius);
    }
    
    public static void drawPulsingGuiGlow(DrawContext context, float x, float y, float width, float height, 
                                         float radius) {
        if (!ClientSettingsModule.isGuiGlowEnabled()) {
            return;
        }
        
        MatrixStack matrices = context.getMatrices();
        Color glowColor = ClientSettingsModule.getGlowColor();
        double time = System.currentTimeMillis() / 1000.0;
        
        GlowRenderer.drawPulsingGlow(matrices, x, y, width, height, radius, glowColor, time);
    }
    
    public static void drawIntenseGuiGlow(DrawContext context, float x, float y, float width, float height, 
                                         float radius) {
        if (!ClientSettingsModule.isGuiGlowEnabled()) {
            return;
        }
        
        MatrixStack matrices = context.getMatrices();
        Color glowColor = ClientSettingsModule.getGlowColor();
        
        GlowRenderer.drawIntenseGlow(matrices, x, y, width, height, radius, glowColor);
    }
    
    public static void drawSubtleGlow(DrawContext context, float x, float y, float width, float height, 
                                     float radius, Color color) {
        MatrixStack matrices = context.getMatrices();
        GlowRenderer.drawGlowBorder(matrices, x, y, width, height,
                                   radius, color, 0.5f, 6.0f, 10.0f);
    }
    
    public static void drawPanelGlow(DrawContext context, float x, float y, float width, float height) {
        drawGuiGlow(context, x, y, width, height, 8.0f);
    }
    
    public static void drawButtonGlow(DrawContext context, float x, float y, float width, float height, 
                                     boolean hovered) {
        if (hovered) {
            drawGuiGlow(context, x, y, width, height, 4.0f);
        } else {
            drawSubtleGlow(context, x, y, width, height, 4.0f, 
                          ClientSettingsModule.getGlowColor());
        }
    }
}

