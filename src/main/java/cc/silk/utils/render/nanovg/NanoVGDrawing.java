package cc.silk.utils.render.nanovg;

import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.system.MemoryStack;

import java.awt.*;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class NanoVGDrawing {
    private static final float INV_255 = 1f / 255f;

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
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

            nvgBeginPath(vg);
            nvgRoundedRect(vg, x, y, width, height, radius);
            nvgFillColor(vg, nvgColor);
            nvgFill(vg);
        } finally {
            nvgRestore(vg);
        }
    }

    public static void drawRoundedRectOutline(float x, float y, float width, float height, float radius,
            float strokeWidth, Color color) {
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

            nvgBeginPath(vg);
            nvgRoundedRect(vg, x, y, width, height, radius);
            nvgStrokeColor(vg, nvgColor);
            nvgStrokeWidth(vg, strokeWidth);
            nvgStroke(vg);
        } finally {
            nvgRestore(vg);
        }
    }

    public static void drawRoundedRectGradient(float x, float y, float width, float height, float radius,
            Color colorTop, Color colorBottom) {
        if (!NanoVGFrameManager.isInFrame() || !NanoVGContext.isValid()) {
            return;
        }

        long vg = NanoVGContext.getHandle();
        nvgSave(vg);
        try (MemoryStack stack = stackPush()) {
            NVGColor color1 = NVGColor.malloc(stack);
            color1.r(colorTop.getRed() * INV_255);
            color1.g(colorTop.getGreen() * INV_255);
            color1.b(colorTop.getBlue() * INV_255);
            color1.a(colorTop.getAlpha() * INV_255);

            NVGColor color2 = NVGColor.malloc(stack);
            color2.r(colorBottom.getRed() * INV_255);
            color2.g(colorBottom.getGreen() * INV_255);
            color2.b(colorBottom.getBlue() * INV_255);
            color2.a(colorBottom.getAlpha() * INV_255);

            NVGPaint paint = nvgLinearGradient(vg, x, y, x, y + height, color1, color2, NVGPaint.malloc(stack));

            nvgBeginPath(vg);
            nvgRoundedRect(vg, x, y, width, height, radius);
            nvgFillPaint(vg, paint);
            nvgFill(vg);
        } finally {
            nvgRestore(vg);
        }
    }

    public static void drawCircle(float x, float y, float radius, Color color) {
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

            nvgBeginPath(vg);
            nvgCircle(vg, x, y, radius);
            nvgFillColor(vg, nvgColor);
            nvgFill(vg);
        } finally {
            nvgRestore(vg);
        }
    }

    public static void drawRect(float x, float y, float width, float height, Color color) {
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

            nvgBeginPath(vg);
            nvgRect(vg, x, y, width, height);
            nvgFillColor(vg, nvgColor);
            nvgFill(vg);
        } finally {
            nvgRestore(vg);
        }
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float strokeWidth, Color color) {
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

            nvgBeginPath(vg);
            nvgMoveTo(vg, x1, y1);
            nvgLineTo(vg, x2, y2);
            nvgStrokeColor(vg, nvgColor);
            nvgStrokeWidth(vg, strokeWidth);
            nvgStroke(vg);
        } finally {
            nvgRestore(vg);
        }
    }

    public static void drawRoundedRectWithShadow(float x, float y, float width, float height, float radius, Color color, Color shadowColor, float shadowBlur, float shadowSpread) {
        if (!NanoVGFrameManager.isInFrame() || !NanoVGContext.isValid()) {
            return;
        }

        long vg = NanoVGContext.getHandle();
        nvgSave(vg);
        try (MemoryStack stack = stackPush()) {
            NVGColor shadowNvgColor = NVGColor.malloc(stack);
            shadowNvgColor.r(shadowColor.getRed() * INV_255);
            shadowNvgColor.g(shadowColor.getGreen() * INV_255);
            shadowNvgColor.b(shadowColor.getBlue() * INV_255);
            shadowNvgColor.a(shadowColor.getAlpha() * INV_255);

            NVGColor transparentColor = NVGColor.malloc(stack);
            transparentColor.r(0);
            transparentColor.g(0);
            transparentColor.b(0);
            transparentColor.a(0);

            NVGPaint shadowPaint = nvgBoxGradient(vg, x, y + shadowSpread, width, height, 
                radius, shadowBlur, shadowNvgColor, transparentColor, NVGPaint.malloc(stack));

            nvgBeginPath(vg);
            nvgRect(vg, x - shadowBlur, y - shadowBlur, width + shadowBlur * 2, height + shadowBlur * 2 + shadowSpread);
            nvgRoundedRect(vg, x, y, width, height, radius);
            nvgPathWinding(vg, NVG_HOLE);
            nvgFillPaint(vg, shadowPaint);
            nvgFill(vg);

            NVGColor nvgColor = NVGColor.malloc(stack);
            nvgColor.r(color.getRed() * INV_255);
            nvgColor.g(color.getGreen() * INV_255);
            nvgColor.b(color.getBlue() * INV_255);
            nvgColor.a(color.getAlpha() * INV_255);

            nvgBeginPath(vg);
            nvgRoundedRect(vg, x, y, width, height, radius);
            nvgFillColor(vg, nvgColor);
            nvgFill(vg);
        } finally {
            nvgRestore(vg);
        }
    }
}

