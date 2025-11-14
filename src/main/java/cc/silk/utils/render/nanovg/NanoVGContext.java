package cc.silk.utils.render.nanovg;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;

import static org.lwjgl.nanovg.NanoVGGL3.*;

public class NanoVGContext {
    @Getter
    private static long handle = 0;
    private static boolean initialized = false;

    public static void init() {
        if (initialized && isValid()) {
            return;
        }

        RenderSystem.assertOnRenderThread();

        if (handle != 0) {
            cleanup();
        }

        handle = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (!isValid()) {
            throw new RuntimeException("Failed to initialize NanoVG");
        }

        initialized = true;
    }

    public static void reinit() {
        cleanup();
        init();
    }

    public static boolean isValid() {
        return handle != 0 && handle != -1;
    }

    public static void assertValid() {
        if (!isValid()) {
            throw new IllegalStateException("Invalid NanoVG context");
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void cleanup() {
        if (handle != 0) {
            try {
                nvgDelete(handle);
            } catch (Exception ignored) {
            }
            handle = 0;
        }
        initialized = false;
    }
}

