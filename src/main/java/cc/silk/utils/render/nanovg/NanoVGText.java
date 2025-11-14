package cc.silk.utils.render.nanovg;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryStack;

import java.awt.*;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class NanoVGText {
    private static final float INV_255 = 1f / 255f;

    public static void drawText(String text, float x, float y, float size, Color color, boolean bold) {
        if (!NanoVGFrameManager.isInFrame() || !NanoVGContext.isValid()) {
            return;
        }

        int fontId = NanoVGFontManager.getFontId(bold);
        long vg = NanoVGContext.getHandle();

        nvgSave(vg);
        try (MemoryStack stack = stackPush()) {
            NVGColor nvgColor = NVGColor.malloc(stack);
            nvgColor.r(color.getRed() * INV_255);
            nvgColor.g(color.getGreen() * INV_255);
            nvgColor.b(color.getBlue() * INV_255);
            nvgColor.a(color.getAlpha() * INV_255);

            nvgFontFaceId(vg, fontId);
            nvgFontSize(vg, size);
            nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
            nvgFillColor(vg, nvgColor);
            nvgText(vg, x, y, text);
        } finally {
            nvgRestore(vg);
        }
    }

    public static void drawText(String text, float x, float y, float size, Color color) {
        drawText(text, x, y, size, color, false);
    }

    public static void drawTextWithFont(String text, float x, float y, float size, Color color, int fontId) {
        if (!NanoVGFrameManager.isInFrame() || !NanoVGContext.isValid()) {
            return;
        }

        long vg = NanoVGContext.getHandle();
        nvgSave(vg);
        try (MemoryStack stack = stackPush()) {
            NVGColor nvgColor = NVGColor.malloc(stack);
            nvgColor.r(color.getRed() * INV_255);
            nvgColor.g(color.getGreen() * INV_255);
            nvgColor.b(color.getBlue() * INV_255);
            nvgColor.a(color.getAlpha() * INV_255);

            nvgFontFaceId(vg, fontId);
            nvgFontSize(vg, size);
            nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
            nvgFillColor(vg, nvgColor);
            nvgText(vg, x, y, text);
        } finally {
            nvgRestore(vg);
        }
    }

    public static void drawIcon(String icon, float x, float y, float size, Color color) {
        if (!NanoVGFrameManager.isInFrame() || !NanoVGContext.isValid()) {
            return;
        }

        int fontId = NanoVGFontManager.getIconFont() > 0 ? NanoVGFontManager.getIconFont() : NanoVGFontManager.getRegularFont();
        long vg = NanoVGContext.getHandle();

        nvgSave(vg);
        try (MemoryStack stack = stackPush()) {
            NVGColor nvgColor = NVGColor.malloc(stack);
            nvgColor.r(color.getRed() * INV_255);
            nvgColor.g(color.getGreen() * INV_255);
            nvgColor.b(color.getBlue() * INV_255);
            nvgColor.a(color.getAlpha() * INV_255);

            nvgFontFaceId(vg, fontId);
            nvgFontSize(vg, size);
            nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
            nvgFillColor(vg, nvgColor);
            nvgText(vg, x, y, icon);
        } finally {
            nvgRestore(vg);
        }
    }

    public static float getTextWidth(String text, float size, boolean bold) {
        if (!NanoVGContext.isValid()) {
            return 0;
        }

        long vg = NanoVGContext.getHandle();
        try (MemoryStack stack = stackPush()) {
            float[] bounds = new float[4];
            nvgFontFaceId(vg, NanoVGFontManager.getFontId(bold));
            nvgFontSize(vg, size);
            nvgTextBounds(vg, 0, 0, text, bounds);
            return bounds[2] - bounds[0];
        }
    }

    public static float getTextWidth(String text, float size) {
        return getTextWidth(text, size, false);
    }

    public static float getTextWidthWithFont(String text, float size, int fontId) {
        if (!NanoVGContext.isValid()) {
            return 0;
        }

        long vg = NanoVGContext.getHandle();
        try (MemoryStack stack = stackPush()) {
            float[] bounds = new float[4];
            nvgFontFaceId(vg, fontId);
            nvgFontSize(vg, size);
            nvgTextBounds(vg, 0, 0, text, bounds);
            return bounds[2] - bounds[0];
        }
    }

    public static float getTextHeight(float size) {
        return size;
    }
}

