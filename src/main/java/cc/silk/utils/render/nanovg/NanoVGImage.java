package cc.silk.utils.render.nanovg;

import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class NanoVGImage {
    public static int loadImage(String path) {
        if (!NanoVGContext.isValid()) {
            if (!NanoVGContext.isInitialized()) {
                NanoVGContext.init();
                NanoVGFontManager.loadFonts();
            }
            if (!NanoVGContext.isValid()) {
                return -1;
            }
        }

        ByteBuffer buffer = null;
        try {
            InputStream is = NanoVGImage.class.getClassLoader().getResourceAsStream(path);
            if (is == null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            }
            if (is == null) {
                return -1;
            }

            buffer = MemoryUtil.memAlloc(8192);
            ReadableByteChannel rbc = Channels.newChannel(is);

            while (rbc.read(buffer) != -1) {
                if (buffer.remaining() == 0) {
                    ByteBuffer newBuffer = MemoryUtil.memAlloc(buffer.capacity() * 2);
                    buffer.flip();
                    newBuffer.put(buffer);
                    MemoryUtil.memFree(buffer);
                    buffer = newBuffer;
                }
            }

            buffer.flip();
            int image = nvgCreateImageMem(NanoVGContext.getHandle(), NVG_IMAGE_GENERATE_MIPMAPS, buffer);

            is.close();

            return image;
        } catch (Exception e) {
            return -1;
        } finally {
            if (buffer != null) {
                MemoryUtil.memFree(buffer);
            }
        }
    }

    public static void drawImage(int imageId, float x, float y, float width, float height, Color tint) {
        if (!NanoVGFrameManager.isInFrame() || !NanoVGContext.isValid() || imageId == -1) {
            return;
        }

        long vg = NanoVGContext.getHandle();
        nvgSave(vg);
        try (MemoryStack stack = stackPush()) {
            float alpha = tint.getAlpha() / 255f;

            NVGPaint paint = nvgImagePattern(vg, x, y, width, height, 0, imageId, alpha, NVGPaint.malloc(stack));

            nvgBeginPath(vg);
            nvgRect(vg, x, y, width, height);
            nvgFillPaint(vg, paint);
            nvgFill(vg);
        } finally {
            nvgRestore(vg);
        }
    }
}

