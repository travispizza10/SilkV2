package cc.silk.module.modules.render;

import cc.silk.event.impl.render.Render2DEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.*;
import cc.silk.utils.render.DraggableComponent;
import cc.silk.utils.render.nanovg.NanoVGRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;

import java.awt.*;

public class Watermark extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Simple", "Simple", "Gamesense");
    private final StringSetting text = new StringSetting("Text", "Silk");
    private final NumberSetting fontSize = new NumberSetting("Font Size", 8, 32, 16, 1);
    private final NumberSetting transparency = new NumberSetting("Transparency", 0, 255, 200, 1);
    private final ModeSetting colorMode = new ModeSetting("Color Mode", "Theme", "Theme", "Custom");
    private final ColorSetting customColor = new ColorSetting("Custom Color", new Color(255, 255, 255));
    private final BooleanSetting waveAnimation = new BooleanSetting("Wave Animation", false);
    private final NumberSetting waveSpeed = new NumberSetting("Wave Speed", 0.1, 5.0, 1.5, 0.1);
    private final NumberSetting waveSpread = new NumberSetting("Wave Spread", 0.1, 5.0, 0.6, 0.1);

    private DraggableComponent draggable;
    private boolean needsInitialCenter = true;

    private int fpsIcon = -1;
    private int pingIcon = -1;

    public Watermark() {
        super("Watermark", "Displays client watermark", -1, Category.RENDER);
        addSettings(mode, text, fontSize, transparency, colorMode, customColor, waveAnimation, waveSpeed, waveSpread);
    }

    @Override
    public void onEnable() {
        loadIcons();
    }

    private void loadIcons() {
        if (fpsIcon == -1) {
            fpsIcon = NanoVGRenderer.loadImage("assets/silk/imgs/fps.png");
        }
        if (pingIcon == -1) {
            pingIcon = NanoVGRenderer.loadImage("assets/silk/imgs/ping.png");
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.world == null)
            return;
        if (text.getValue().isEmpty())
            return;

        if (draggable == null) {
            int screenWidth = mc.getWindow().getScaledWidth();
            draggable = new DraggableComponent(screenWidth / 2f, 10, 200, 30);
            needsInitialCenter = true;
        }

        boolean isInChat = mc.currentScreen instanceof ChatScreen;
        if (mc.currentScreen != null && !isInChat)
            return;

        NanoVGRenderer.beginFrame();

        switch (mode.getMode()) {
            case "Simple" -> renderSimple(isInChat);
            case "Gamesense" -> renderGamesense(isInChat);
        }

        NanoVGRenderer.endFrame();
    }

    private void renderSimple(boolean isInChat) {
        float padding = 8;
        float titleSize = 14;
        float infoSize = 11;
        float spacing = 8;
        float iconSize = 12;
        float iconSpacing = 4;

        String title = text.getValue();
        int fps = mc.getCurrentFps();

        int ping = 0;
        if (mc.getNetworkHandler() != null && mc.player != null) {
            var playerEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (playerEntry != null) {
                ping = playerEntry.getLatency();
            }
        }

        String fpsText = String.valueOf(fps);
        String pingText = ping + "ms";

        float titleWidth = NanoVGRenderer.getTextWidth(title, titleSize);
        float fpsWidth = iconSize + iconSpacing + NanoVGRenderer.getTextWidth(fpsText, infoSize);
        float pingWidth = iconSize + iconSpacing + NanoVGRenderer.getTextWidth(pingText, infoSize);

        float titleHeight = NanoVGRenderer.getTextHeight(titleSize);
        float infoHeight = NanoVGRenderer.getTextHeight(infoSize);
        float maxHeight = Math.max(titleHeight, Math.max(infoHeight, iconSize));

        float totalWidth = titleWidth + spacing + fpsWidth + spacing + pingWidth;
        float bgWidth = totalWidth + padding * 2;
        float bgHeight = maxHeight + padding * 2;

        draggable.setWidth(bgWidth);
        draggable.setHeight(bgHeight);

        if (needsInitialCenter) {
            int screenWidth = mc.getWindow().getScaledWidth();
            draggable.setX(screenWidth / 2f - bgWidth / 2f);
            needsInitialCenter = false;
        }

        if (isInChat) {
            draggable.update();
            snapToCenter(bgWidth);
        }

        float x = draggable.getX();
        float y = draggable.getY();

        int alpha = (int) transparency.getValue();
        Color bgColor = new Color(20, 20, 25, alpha);
        Color borderColor = new Color(40, 40, 46, alpha);
        NanoVGRenderer.drawRoundedRect(x, y, bgWidth, bgHeight, 4f, bgColor);
        NanoVGRenderer.drawRoundedRectOutline(x, y, bgWidth, bgHeight, 4f, 1f, borderColor);

        float centerY = y + bgHeight / 2f;

        float textX = x + padding;
        float titleY = centerY - titleHeight / 2f;

        Color accentColor = getColor(0);
        if (waveAnimation.getValue()) {
            drawWaveText(title, textX, titleY, titleSize, accentColor);
        } else {
            NanoVGRenderer.drawText(title, textX, titleY, titleSize, accentColor);
        }

        textX += titleWidth + spacing;

        float iconY = centerY - iconSize / 2f;
        if (fpsIcon != -1) {
            if (waveAnimation.getValue()) {
                drawWaveIcon(fpsIcon, textX, iconY, iconSize, 0, accentColor);
            } else {
                NanoVGRenderer.drawImage(fpsIcon, textX, iconY, iconSize, iconSize, new Color(255, 255, 255, 255));
            }
        }
        textX += iconSize + iconSpacing;

        float infoY = centerY - infoHeight / 2f;
        Color infoColor = new Color(200, 200, 200);
        if (waveAnimation.getValue()) {
            drawWaveText(fpsText, textX, infoY, infoSize, accentColor, 1);
        } else {
            NanoVGRenderer.drawText(fpsText, textX, infoY, infoSize, infoColor);
        }

        textX += NanoVGRenderer.getTextWidth(fpsText, infoSize) + spacing;

        iconY = centerY - iconSize / 2f;
        if (pingIcon != -1) {
            if (waveAnimation.getValue()) {
                int startIndex = 1 + fpsText.length();
                drawWaveIcon(pingIcon, textX, iconY, iconSize, startIndex, accentColor);
            } else {
                NanoVGRenderer.drawImage(pingIcon, textX, iconY, iconSize, iconSize, new Color(255, 255, 255, 255));
            }
        }
        textX += iconSize + iconSpacing;
        if (waveAnimation.getValue()) {
            int startIndex = 1 + fpsText.length() + 1;
            drawWaveText(pingText, textX, infoY, infoSize, accentColor, startIndex);
        } else {
            NanoVGRenderer.drawText(pingText, textX, infoY, infoSize, infoColor);
        }
    }

    private void renderGamesense(boolean isInChat) {
        String fullText = text.getValue() + "sense | release | " + getIP();

        float padding = 6;
        float size = 12;

        float textWidth = NanoVGRenderer.getTextWidth(fullText, size);
        float textHeight = NanoVGRenderer.getTextHeight(size);
        float bgWidth = textWidth + padding * 2;
        float bgHeight = textHeight + padding * 2;

        draggable.setWidth(bgWidth);
        draggable.setHeight(bgHeight);

        if (needsInitialCenter) {
            int screenWidth = mc.getWindow().getScaledWidth();
            draggable.setX(screenWidth / 2f - bgWidth / 2f);
            needsInitialCenter = false;
        }

        if (isInChat) {
            draggable.update();
            snapToCenter(bgWidth);
        }

        float x = draggable.getX();
        float y = draggable.getY();

        int alpha = (int) transparency.getValue();
        NanoVGRenderer.drawRoundedRect(x - 1, y - 1, bgWidth + 2, bgHeight + 2, 2f,
                new Color(96, 96, 96, alpha));
        NanoVGRenderer.drawRoundedRect(x, y, bgWidth, bgHeight, 2f, new Color(25, 25, 25, alpha));

        Color accentColor = getColor(0);
        if (waveAnimation.getValue()) {
            drawWaveText(fullText, x + padding, y + padding, size, accentColor);
        } else {
            NanoVGRenderer.drawText(fullText, x + padding, y + padding, size, accentColor);
        }
    }

    private void snapToCenter(float width) {
        int screenWidth = mc.getWindow().getScaledWidth();
        float centerX = screenWidth / 2f;
        float componentCenterX = draggable.getX() + width / 2f;

        float snapDistance = 5f;
        if (Math.abs(componentCenterX - centerX) < snapDistance) {
            draggable.setX(centerX - width / 2f);
        }
    }

    private Color getColor(int offset) {
        return switch (colorMode.getMode()) {
            case "Custom" -> customColor.getValue();
            default -> cc.silk.module.modules.client.NewClickGUIModule.getAccentColor();
        };
    }

    private String getIP() {
        if (mc.world == null)
            return "NULL";
        if (mc.isInSingleplayer())
            return "Singleplayer";
        return mc.getCurrentServerEntry().address;
    }

    private void drawWaveText(String s, float x, float y, float size, Color baseColor) {
        drawWaveText(s, x, y, size, baseColor, 0);
    }

    private void drawWaveText(String s, float x, float y, float size, Color baseColor, int startIndex) {
        double time = (System.currentTimeMillis() / 1000.0) * waveSpeed.getValue();
        float cursorX = x;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            double phase = time + (startIndex + i) * waveSpread.getValue();
            double waveValue = (Math.sin(phase) + 1.0) * 0.5;
            double wave = Math.sin(waveValue * Math.PI * 0.5);
            int waved = applyWaveColor(baseColor.getRGB(), wave);
            NanoVGRenderer.drawText(ch, cursorX, y, size, new Color(waved, true));
            cursorX += NanoVGRenderer.getTextWidth(ch, size);
        }
    }

    // this aint fucking working properly nigger nigger
    private void drawWaveIcon(int imageId, float x, float y, float size, int index, Color baseColor) {
        double time = (System.currentTimeMillis() / 1000.0) * waveSpeed.getValue();
        double phase = time + index * waveSpread.getValue();
        double waveValue = (Math.sin(phase) + 1.0) * 0.5;
        double wave = Math.sin(waveValue * Math.PI * 0.5);
        int waved = applyWaveColor(baseColor.getRGB(), wave);
        NanoVGRenderer.drawImage(imageId, x, y, size, size, new Color(waved, true));
    }

    private int applyWaveColor(int baseColor, double waveValue) {
        Color base = new Color(baseColor, true);
        double wave = Math.pow(waveValue, 0.5);
        int r = (int) (base.getRed() + (255 - base.getRed()) * wave);
        int g = (int) (base.getGreen() + (255 - base.getGreen()) * wave);
        int b = (int) (base.getBlue() + (255 - base.getBlue()) * wave);
        return (base.getAlpha() << 24) | (r << 16) | (g << 8) | b;
    }
}
