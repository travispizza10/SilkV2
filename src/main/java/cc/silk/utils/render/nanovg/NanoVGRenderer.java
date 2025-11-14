package cc.silk.utils.render.nanovg;

import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class NanoVGRenderer {
    public static void init() {
        NanoVGContext.init();
        NanoVGFontManager.loadFonts();
    }

    public static void reinit() {
        NanoVGContext.reinit();
        NanoVGFontManager.loadFonts();
    }

    public static void beginFrame() {
        NanoVGFrameManager.beginFrame();
    }

    public static void endFrame() {
        NanoVGFrameManager.endFrame();
    }

    public static boolean isInFrame() {
        return NanoVGFrameManager.isInFrame();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        NanoVGDrawing.drawRoundedRect(x, y, width, height, radius, color);
    }

    public static void drawRoundedRectOutline(float x, float y, float width, float height, float radius,
            float strokeWidth, Color color) {
        NanoVGDrawing.drawRoundedRectOutline(x, y, width, height, radius, strokeWidth, color);
    }

    public static void drawRoundedRectGradient(float x, float y, float width, float height, float radius,
            Color colorTop, Color colorBottom) {
        NanoVGDrawing.drawRoundedRectGradient(x, y, width, height, radius, colorTop, colorBottom);
    }

    public static void drawCircle(float x, float y, float radius, Color color) {
        NanoVGDrawing.drawCircle(x, y, radius, color);
    }

    public static void drawRect(float x, float y, float width, float height, Color color) {
        NanoVGDrawing.drawRect(x, y, width, height, color);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float strokeWidth, Color color) {
        NanoVGDrawing.drawLine(x1, y1, x2, y2, strokeWidth, color);
    }

    public static void drawRoundedRectWithShadow(float x, float y, float width, float height, float radius, Color color, Color shadowColor, float shadowBlur, float shadowSpread) {
        NanoVGDrawing.drawRoundedRectWithShadow(x, y, width, height, radius, color, shadowColor, shadowBlur, shadowSpread);
    }

    public static void drawText(String text, float x, float y, float size, Color color, boolean bold) {
        NanoVGText.drawText(text, x, y, size, color, bold);
    }

    public static void drawText(String text, float x, float y, float size, Color color) {
        NanoVGText.drawText(text, x, y, size, color);
    }

    public static void drawTextWithFont(String text, float x, float y, float size, Color color, int fontId) {
        NanoVGText.drawTextWithFont(text, x, y, size, color, fontId);
    }

    public static void drawIcon(String icon, float x, float y, float size, Color color) {
        NanoVGText.drawIcon(icon, x, y, size, color);
    }

    public static float getTextWidth(String text, float size, boolean bold) {
        return NanoVGText.getTextWidth(text, size, bold);
    }

    public static float getTextWidth(String text, float size) {
        return NanoVGText.getTextWidth(text, size);
    }

    public static float getTextWidthWithFont(String text, float size, int fontId) {
        return NanoVGText.getTextWidthWithFont(text, size, fontId);
    }

    public static float getTextHeight(float size) {
        return NanoVGText.getTextHeight(size);
    }

    public static int getPoppinsFontId() {
        return NanoVGFontManager.getPoppinsFont() > 0 ? NanoVGFontManager.getPoppinsFont() : NanoVGFontManager.getRegularFont();
    }

    public static int getJetBrainsFontId() {
        return NanoVGFontManager.getJetbrainsFont() > 0 ? NanoVGFontManager.getJetbrainsFont() : NanoVGFontManager.getRegularFont();
    }

    public static int getRegularFontId() {
        return NanoVGFontManager.getRegularFont() > 0 ? NanoVGFontManager.getRegularFont() : 0;
    }

    public static int getMonacoFontId() {
        return NanoVGFontManager.getMonacoFont() > 0 ? NanoVGFontManager.getMonacoFont() : NanoVGFontManager.getRegularFont();
    }

    public static int loadImage(String path) {
        return NanoVGImage.loadImage(path);
    }

    public static void drawImage(int imageId, float x, float y, float width, float height, Color tint) {
        NanoVGImage.drawImage(imageId, x, y, width, height, tint);
    }

    public static void save() {
        NanoVGTransform.save();
    }

    public static void restore() {
        NanoVGTransform.restore();
    }

    public static void translate(float x, float y) {
        NanoVGTransform.translate(x, y);
    }

    public static void scale(float x, float y) {
        NanoVGTransform.scale(x, y);
    }

    public static void scissor(float x, float y, float width, float height) {
        NanoVGTransform.scissor(x, y, width, height);
    }

    public static void resetScissor() {
        NanoVGTransform.resetScissor();
    }

    public static void cleanup() {
        NanoVGFrameManager.resetInFrame();
        NanoVGContext.cleanup();
    }
}
