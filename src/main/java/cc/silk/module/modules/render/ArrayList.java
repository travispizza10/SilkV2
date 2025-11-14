package cc.silk.module.modules.render;

import cc.silk.SilkClient;
import cc.silk.event.impl.render.Render2DEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.*;
import cc.silk.utils.render.nanovg.NanoVGRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class ArrayList extends Module {
    private final ModeSetting position = new ModeSetting("Position", "Top Right", "Top Left", "Top Right",
            "Bottom Left", "Bottom Right");
    private final ModeSetting sortMode = new ModeSetting("Sort", "Length", "Length", "Alphabetical");
    private final ModeSetting font = new ModeSetting("Font", "Inter", "Inter", "JetBrains Mono", "Poppins", "Monaco");
    private final NumberSetting fontSize = new NumberSetting("Font Size", 8, 24, 14, 1);
    private final NumberSetting padding = new NumberSetting("Padding", 2, 10, 4, 1);
    private final NumberSetting spacing = new NumberSetting("Spacing", 0, 5, 2, 1);
    private final NumberSetting xOffset = new NumberSetting("X Offset", 0, 50, 5, 1);
    private final NumberSetting yOffset = new NumberSetting("Y Offset", 0, 50, 5, 1);
    private final ModeSetting bgColorMode = new ModeSetting("BG Color", "Dark Gray", "Dark Gray", "Black",
            "Matt Black");
    private final NumberSetting bgAlpha = new NumberSetting("BG Alpha", 0, 255, 150, 1);
    private final NumberSetting cornerRadius = new NumberSetting("Corner Radius", 0, 10, 3, 0.5);
    private final BooleanSetting showBackground = new BooleanSetting("Background", true);
    private final BooleanSetting showBar = new BooleanSetting("Side Bar", true);
    private final NumberSetting barWidth = new NumberSetting("Bar Width", 1, 5, 2, 0.5);
    private final BooleanSetting showOutline = new BooleanSetting("Outline", true);
    private final NumberSetting outlineWidth = new NumberSetting("Outline Width", 0.5, 3, 1, 0.5);
    private final BooleanSetting showSuffix = new BooleanSetting("Show Suffix", true);
    private final BooleanSetting animations = new BooleanSetting("Animations", true);
    private final NumberSetting animationSpeed = new NumberSetting("Animation Speed", 0.1, 10.0, 5.0, 0.1);
    private final ModeSetting colorMode = new ModeSetting("Color Mode", "Theme", "Theme", "Gradient", "Custom");
    private final ColorSetting customColor = new ColorSetting("Custom Color", new Color(255, 100, 100));
    private final ColorSetting gradientColor = new ColorSetting("Gradient Color", new Color(100, 150, 255));
    private final NumberSetting gradientSpeed = new NumberSetting("Gradient Speed", 0.1, 5.0, 1.5, 0.1);
    private final NumberSetting gradientSpread = new NumberSetting("Gradient Spread", 0.1, 5.0, 0.6, 0.1);

    private final java.util.Map<String, ModuleAnimation> moduleAnimations = new java.util.HashMap<>();

    public ArrayList() {
        super("ArrayList", "Displays enabled modules", -1, Category.RENDER);
        addSettings(position, sortMode, font, fontSize, padding, spacing, xOffset, yOffset,
                showBackground, bgColorMode, bgAlpha, cornerRadius, showBar, barWidth,
                showOutline, outlineWidth, showSuffix, animations, animationSpeed,
                colorMode, customColor, gradientColor, gradientSpeed, gradientSpread);
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.world == null)
            return;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen))
            return;

        List<Module> enabledModules = SilkClient.INSTANCE.getModuleManager().getEnabledModules();

        if (animations.getValue()) {
            updateAnimations(enabledModules);
        }

        if (enabledModules.isEmpty() && moduleAnimations.isEmpty())
            return;

        List<Module> sortedModules = new java.util.ArrayList<>(enabledModules);
        int fontId = getFontId();
        if (sortMode.getMode().equals("Length")) {
            sortedModules.sort(Comparator
                    .comparingDouble(
                            m -> -NanoVGRenderer.getTextWidthWithFont(getModuleDisplayText(m),
                                    (float) fontSize.getValue(), fontId)));
        } else {
            sortedModules.sort(Comparator.comparing(Module::getName));
        }

        NanoVGRenderer.beginFrame();
        renderModuleList(sortedModules);
        NanoVGRenderer.endFrame();
    }

    private void renderModuleList(List<Module> modules) {
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        float size = (float) fontSize.getValue();
        float pad = (float) padding.getValue();
        float gap = (float) spacing.getValue();
        float xOff = (float) xOffset.getValue();
        float yOff = (float) yOffset.getValue();
        float radius = (float) cornerRadius.getValue();
        float barW = (float) barWidth.getValue();

        String pos = position.getMode();
        boolean isRight = pos.contains("Right");
        boolean isBottom = pos.contains("Bottom");

        float currentY = isBottom ? screenHeight - yOff : yOff;
        float previousWidth = 0;

        List<String> modulesToRender = new java.util.ArrayList<>();
        for (Module m : modules) {
            modulesToRender.add(m.getName());
        }
        if (animations.getValue()) {
            for (String name : moduleAnimations.keySet()) {
                if (!modulesToRender.contains(name)) {
                    modulesToRender.add(name);
                }
            }
        }

        for (int i = 0; i < modulesToRender.size(); i++) {
            String moduleName = modulesToRender.get(i);
            Module module = modules.stream().filter(m -> m.getName().equals(moduleName)).findFirst().orElse(null);

            if (module == null && !animations.getValue())
                continue;

            String displayText = module != null ? getModuleDisplayText(module) : moduleName;

            float animProgress = 1.0f;
            if (animations.getValue()) {
                ModuleAnimation anim = moduleAnimations.get(moduleName);
                if (anim != null) {
                    animProgress = anim.progress;
                    if (animProgress <= 0)
                        continue;
                }
            }

            int fontId = getFontId();
            float textWidth = NanoVGRenderer.getTextWidthWithFont(displayText, size, fontId);
            float textHeight = NanoVGRenderer.getTextHeight(size);
            float bgWidth = textWidth + pad * 2;
            float bgHeight = textHeight + pad * 2;

            float animatedWidth = bgWidth * animProgress;

            float x = isRight ? screenWidth - animatedWidth - xOff : xOff;
            float y = isBottom ? currentY - bgHeight : currentY;

            if (showBackground.getValue()) {
                int alpha = (int) (bgAlpha.getValue() * animProgress);
                Color bgColor = switch (bgColorMode.getMode()) {
                    case "Black" -> new Color(0, 0, 0, alpha);
                    case "Matt Black" -> new Color(10, 10, 10, alpha);
                    default -> new Color(20, 20, 25, alpha);
                };

                NanoVGRenderer.drawRoundedRect(x, y, animatedWidth, bgHeight, radius, bgColor);
            }

            if (showOutline.getValue() && animProgress > 0.1f) {
                Color outlineColor = getModuleColor(i, modules.size());
                outlineColor = new Color(outlineColor.getRed(), outlineColor.getGreen(),
                        outlineColor.getBlue(), (int) (outlineColor.getAlpha() * animProgress));
                float outlineW = (float) outlineWidth.getValue();

                if (radius > 0) {
                    NanoVGRenderer.drawRoundedRectOutline(x, y, animatedWidth, bgHeight, radius, outlineW,
                            outlineColor);
                } else {
                    if (isRight) {
                        if (i == 0) {
                            NanoVGRenderer.drawLine(x, y, x + animatedWidth, y, outlineW, outlineColor);
                        }

                        NanoVGRenderer.drawLine(x, y, x, y + bgHeight, outlineW, outlineColor);

                        if (i > 0 && previousWidth != animatedWidth) {
                            float prevX = screenWidth - previousWidth - xOff;
                            NanoVGRenderer.drawLine(x, y, prevX, y, outlineW, outlineColor);
                        }

                        if (i == modules.size() - 1) {
                            NanoVGRenderer.drawLine(x, y + bgHeight, x + animatedWidth, y + bgHeight, outlineW,
                                    outlineColor);
                        }

                        NanoVGRenderer.drawLine(x + animatedWidth, y, x + animatedWidth, y + bgHeight, outlineW,
                                outlineColor);
                    } else {
                        if (i == 0) {
                            NanoVGRenderer.drawLine(x, y, x + animatedWidth, y, outlineW, outlineColor);
                        }

                        NanoVGRenderer.drawLine(x, y, x, y + bgHeight, outlineW, outlineColor);

                        NanoVGRenderer.drawLine(x + animatedWidth, y, x + animatedWidth, y + bgHeight, outlineW,
                                outlineColor);

                        if (i > 0 && previousWidth != animatedWidth) {
                            float prevX = xOff + previousWidth;
                            NanoVGRenderer.drawLine(prevX, y, x + animatedWidth, y, outlineW, outlineColor);
                        }

                        if (i == modules.size() - 1) {
                            NanoVGRenderer.drawLine(x, y + bgHeight, x + animatedWidth, y + bgHeight, outlineW,
                                    outlineColor);
                        }
                    }
                }
            }

            if (showBar.getValue()) {
                Color barColor = getModuleColor(i, modules.size());
                barColor = new Color(barColor.getRed(), barColor.getGreen(),
                        barColor.getBlue(), (int) (barColor.getAlpha() * animProgress));
                if (isRight) {
                    NanoVGRenderer.drawRoundedRect(x + animatedWidth - barW, y, barW, bgHeight,
                            radius, barColor);
                } else {
                    NanoVGRenderer.drawRoundedRect(x, y, barW, bgHeight, radius, barColor);
                }
            }

            NanoVGRenderer.scissor(x, y, animatedWidth, bgHeight);

            if (colorMode.getMode().equals("Gradient")) {
                drawGradientText(displayText, x + pad + (isRight ? 0 : (showBar.getValue() ? barW : 0)),
                        y + pad, size, animProgress, fontId);
            } else {
                Color textColor = getModuleColor(i, modules.size());
                textColor = new Color(textColor.getRed(), textColor.getGreen(),
                        textColor.getBlue(), (int) (textColor.getAlpha() * animProgress));
                float textX = x + pad + (isRight ? 0 : (showBar.getValue() ? barW : 0));
                float textY = y + pad;
                NanoVGRenderer.drawTextWithFont(displayText, textX, textY, size, textColor, fontId);
            }

            NanoVGRenderer.resetScissor();

            previousWidth = animatedWidth;
            if (isBottom) {
                currentY -= bgHeight + gap;
            } else {
                currentY += bgHeight + gap;
            }
        }
    }

    private Color getModuleColor(int index, int total) {
        return switch (colorMode.getMode()) {
            case "Custom" -> customColor.getValue();
            case "Gradient" -> getGradientColor(index);
            default -> cc.silk.module.modules.client.NewClickGUIModule.getAccentColor();
        };
    }

    private Color getGradientColor(int index) {
        double time = (System.currentTimeMillis() / 1000.0) * gradientSpeed.getValue();
        double phase = time + index * gradientSpread.getValue();
        double waveValue = (Math.sin(phase) + 1.0) * 0.5;
        double wave = Math.sin(waveValue * Math.PI * 0.5);
        return new Color(applyWaveColor(gradientColor.getValue().getRGB(), wave, 1.0f), true);
    }

    private String getModuleDisplayText(Module module) {
        String name = module.getName();
        if (!showSuffix.getValue()) {
            return name;
        }

        NumberSetting numberSetting = null;
        for (Setting setting : module.getSettings()) {
            if (setting instanceof NumberSetting) {
                numberSetting = (NumberSetting) setting;
                break;
            }
        }

        if (numberSetting == null) {
            return name;
        }

        double value = numberSetting.getValue();
        String formattedValue;
        if (value == (long) value) {
            formattedValue = String.format("%d", (long) value);
        } else {
            formattedValue = String.format("%.1f", value);
        }

        return name + " [" + formattedValue + "]";
    }

    private void updateAnimations(List<Module> enabledModules) {
        float delta = (float) (animationSpeed.getValue() * 0.016);

        for (Module module : enabledModules) {
            moduleAnimations.putIfAbsent(module.getName(), new ModuleAnimation());
        }

        moduleAnimations.entrySet().removeIf(entry -> {
            ModuleAnimation anim = entry.getValue();
            boolean isEnabled = enabledModules.stream().anyMatch(m -> m.getName().equals(entry.getKey()));

            if (isEnabled) {
                anim.progress = Math.min(1.0f, anim.progress + delta);
            } else {
                anim.progress = Math.max(0.0f, anim.progress - delta);
            }

            return anim.progress <= 0 && !isEnabled;
        });
    }

    private void drawGradientText(String text, float x, float y, float size, float alpha, int fontId) {
        double time = (System.currentTimeMillis() / 1000.0) * gradientSpeed.getValue();
        float cursorX = x;

        Color baseColor = gradientColor.getValue();

        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            double phase = time + i * gradientSpread.getValue();
            double waveValue = (Math.sin(phase) + 1.0) * 0.5;
            double wave = Math.sin(waveValue * Math.PI * 0.5);

            int waved = applyWaveColor(baseColor.getRGB(), wave, alpha);
            NanoVGRenderer.drawTextWithFont(ch, cursorX, y, size, new Color(waved, true), fontId);
            cursorX += NanoVGRenderer.getTextWidthWithFont(ch, size, fontId);
        }
    }

    private int getFontId() {
        return switch (font.getMode()) {
            case "JetBrains Mono" -> NanoVGRenderer.getJetBrainsFontId();
            case "Poppins" -> NanoVGRenderer.getPoppinsFontId();
            case "Monaco" -> NanoVGRenderer.getMonacoFontId();
            default -> NanoVGRenderer.getRegularFontId();
        };
    }

    private int applyWaveColor(int baseColor, double waveValue, float alpha) {
        Color base = new Color(baseColor, true);
        double wave = Math.pow(waveValue, 0.5);
        int r = (int) (base.getRed() + (255 - base.getRed()) * wave);
        int g = (int) (base.getGreen() + (255 - base.getGreen()) * wave);
        int b = (int) (base.getBlue() + (255 - base.getBlue()) * wave);
        int a = (int) (255 * alpha);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static class ModuleAnimation {
        float progress = 0.0f;
    }
}
