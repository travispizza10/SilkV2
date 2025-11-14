package cc.silk.utils.render.nanovg;

import lombok.Getter;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.lwjgl.nanovg.NanoVG.*;

public class NanoVGFontManager {
    @Getter
    private static int regularFont = -1;
    @Getter
    private static int boldFont = -1;
    @Getter
    private static int iconFont = -1;
    @Getter
    private static int jetbrainsFont = -1;
    @Getter
    private static int poppinsFont = -1;
    @Getter
    private static int monacoFont = -1;

    private static String cachedFontStyle = null;
    private static int cachedFontId = -1;
    private static long lastFontCheck = 0;
    private static final long FONT_CACHE_TIME = 1000;

    public static void loadFonts() {
        NanoVGContext.assertValid();
        try {
            regularFont = loadFont("inter", "assets/silk/fonts/Inter.ttf");
            boldFont = loadFont("inter-bold", "assets/silk/fonts/Inter.ttf");
            jetbrainsFont = loadFont("jetbrains", "assets/silk/fonts/JetbrainsMono.ttf");
            poppinsFont = loadFont("poppins", "assets/silk/fonts/Poppins-Medium.ttf");
            monacoFont = loadFont("monaco", "assets/silk/fonts/monaco.ttf");

            try {
                iconFont = loadFont("fa-solid", "assets/silk/fonts/font awesome 7 freesolid900.otf");
            } catch (Exception e) {
                iconFont = regularFont;
            }

            if (regularFont == -1) {
                regularFont = 0;
            }
            if (boldFont == -1) {
                boldFont = regularFont;
            }
        } catch (Exception e) {
            regularFont = 0;
            boldFont = 0;
        }
    }

    private static int loadFont(String name, String path) throws Exception {
        InputStream is = NanoVGFontManager.class.getClassLoader().getResourceAsStream(path);
        if (is == null) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        }
        if (is == null) {
            throw new RuntimeException("Font not found: " + path);
        }

        try {
            return createFontFromStream(name, is);
        } finally {
            is.close();
        }
    }

    private static int createFontFromStream(String name, InputStream is) throws Exception {
        ReadableByteChannel rbc = Channels.newChannel(is);
        ByteBuffer buffer = MemoryUtil.memAlloc(8192);

        try {
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
            int font = nvgCreateFontMem(NanoVGContext.getHandle(), name, buffer, false);

            if (font == -1) {
                MemoryUtil.memFree(buffer);
                throw new RuntimeException("Failed to create font: " + name);
            }

            return font;
        } catch (Exception e) {
            MemoryUtil.memFree(buffer);
            throw e;
        }
    }

    public static int getFontId(boolean bold) {
        long currentTime = System.currentTimeMillis();

        if (cachedFontStyle == null || currentTime - lastFontCheck > FONT_CACHE_TIME) {
            try {
                cachedFontStyle = cc.silk.module.modules.client.ClientSettingsModule.getFontStyle();
                lastFontCheck = currentTime;

                if ("JetbrainsMono.ttf".equalsIgnoreCase(cachedFontStyle)) {
                    cachedFontId = jetbrainsFont > 0 ? jetbrainsFont : regularFont;
                } else if ("Poppins-Medium.ttf".equalsIgnoreCase(cachedFontStyle)) {
                    cachedFontId = poppinsFont > 0 ? poppinsFont : regularFont;
                } else if ("monaco.ttf".equalsIgnoreCase(cachedFontStyle)) {
                    cachedFontId = monacoFont > 0 ? monacoFont : regularFont;
                } else if ("Inter.ttf".equals(cachedFontStyle)) {
                    cachedFontId = regularFont > 0 ? regularFont : 0;
                } else {
                    cachedFontId = bold ? boldFont : regularFont;
                }
            } catch (Exception e) {
                cachedFontId = bold ? boldFont : regularFont;
            }
        }

        int fontId = cachedFontId;

        if (fontId <= 0) {
            fontId = regularFont > 0 ? regularFont : boldFont;
        }

        if (fontId <= 0) {
            fontId = 1;
        }

        return fontId;
    }
}

