package cc.silk.utils.render.nanovg;

import static org.lwjgl.nanovg.NanoVG.*;

public class NanoVGTransform {
    public static void save() {
        if (NanoVGContext.isValid()) {
            nvgSave(NanoVGContext.getHandle());
        }
    }

    public static void restore() {
        if (NanoVGContext.isValid()) {
            nvgRestore(NanoVGContext.getHandle());
        }
    }

    public static void translate(float x, float y) {
        if (NanoVGContext.isValid()) {
            nvgTranslate(NanoVGContext.getHandle(), x, y);
        }
    }

    public static void scale(float x, float y) {
        if (NanoVGContext.isValid()) {
            nvgScale(NanoVGContext.getHandle(), x, y);
        }
    }

    public static void scissor(float x, float y, float width, float height) {
        if (NanoVGContext.isValid()) {
            nvgScissor(NanoVGContext.getHandle(), x, y, width, height);
        }
    }

    public static void resetScissor() {
        if (NanoVGContext.isValid()) {
            nvgResetScissor(NanoVGContext.getHandle());
        }
    }
}

