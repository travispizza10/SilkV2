package cc.silk.module.modules.render;

import cc.silk.event.impl.render.Render2DEvent;
import cc.silk.mixin.WorldRendererAccessor;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.BooleanSetting;
import cc.silk.module.setting.ColorSetting;
import cc.silk.module.setting.ModeSetting;
import cc.silk.module.setting.NumberSetting;
import cc.silk.utils.render.W2SUtil;
import cc.silk.utils.render.nanovg.NanoVGRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;

import java.awt.*;

public class ESP2D extends Module {

        private final ModeSetting targets = new ModeSetting("Targets", "Players", "Players", "Passives", "Hostiles",
                        "All");
        private final BooleanSetting showSelf = new BooleanSetting("Show Self", false);
        private final NumberSetting range = new NumberSetting("Range", 10, 200, 64, 5);
        private final ModeSetting boxMode = new ModeSetting("Box Mode", "Full", "Full", "Corners", "Rounded");
        private final BooleanSetting boxSetting = new BooleanSetting("Box", true);
        private final NumberSetting roundRadius = new NumberSetting("Round Radius", 0, 10, 3, 0.5);
        private final BooleanSetting boxFill = new BooleanSetting("Box Fill", false);
        private final NumberSetting fillOpacity = new NumberSetting("Fill Opacity", 0, 255, 80, 5);
        private final BooleanSetting healthBar = new BooleanSetting("Health Bar", true);
        private final ColorSetting boxColor = new ColorSetting("Box Color", new Color(255, 255, 255));
        private final ColorSetting playerColor = new ColorSetting("Player Color", new Color(255, 255, 255));
        private final ColorSetting passiveColor = new ColorSetting("Passive Color", new Color(0, 255, 0));
        private final ColorSetting hostileColor = new ColorSetting("Hostile Color", new Color(255, 0, 0));

        public ESP2D() {
                super("2D ESP", "Draws 2D boxes around entities", -1, Category.RENDER);
                addSettings(targets, showSelf, range, boxMode, boxSetting, roundRadius, boxFill, fillOpacity, healthBar,
                                boxColor, playerColor, passiveColor, hostileColor);
        }

        @EventHandler
        private void onRender2D(Render2DEvent event) {
                if (isNull() || mc.world == null || mc.player == null)
                        return;

                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

                NanoVGRenderer.beginFrame();

                for (Entity entity : mc.world.getEntities()) {
                        if (!(entity instanceof LivingEntity))
                                continue;

                        if (!shouldRender(entity))
                                continue;

                        Box box = entity.getBoundingBox();

                        if (!((WorldRendererAccessor) mc.worldRenderer).getFrustum().isVisible(box))
                                continue;

                        double x = entity.prevX + (entity.getX() - entity.prevX)
                                        * mc.getRenderTickCounter().getTickDelta(false);
                        double y = entity.prevY + (entity.getY() - entity.prevY)
                                        * mc.getRenderTickCounter().getTickDelta(false);
                        double z = entity.prevZ + (entity.getZ() - entity.prevZ)
                                        * mc.getRenderTickCounter().getTickDelta(false);

                        Box expandedBox = new Box(
                                        box.minX - entity.getX() + x - 0.05,
                                        box.minY - entity.getY() + y,
                                        box.minZ - entity.getZ() + z - 0.05,
                                        box.maxX - entity.getX() + x + 0.05,
                                        box.maxY - entity.getY() + y + 0.1,
                                        box.maxZ - entity.getZ() + z + 0.05);

                        Vec3d[] vectors = new Vec3d[] {
                                        new Vec3d(expandedBox.minX, expandedBox.minY, expandedBox.minZ),
                                        new Vec3d(expandedBox.minX, expandedBox.maxY, expandedBox.minZ),
                                        new Vec3d(expandedBox.maxX, expandedBox.minY, expandedBox.minZ),
                                        new Vec3d(expandedBox.maxX, expandedBox.maxY, expandedBox.minZ),
                                        new Vec3d(expandedBox.minX, expandedBox.minY, expandedBox.maxZ),
                                        new Vec3d(expandedBox.minX, expandedBox.maxY, expandedBox.maxZ),
                                        new Vec3d(expandedBox.maxX, expandedBox.minY, expandedBox.maxZ),
                                        new Vec3d(expandedBox.maxX, expandedBox.maxY, expandedBox.maxZ),
                        };

                        Vector4d position = null;

                        for (Vec3d vector : vectors) {
                                Vec3d vectorToScreen = W2SUtil.getCoords(vector);

                                if (vectorToScreen.z > 0 && vectorToScreen.z < 1) {
                                        if (position == null) {
                                                position = new Vector4d(vectorToScreen.x, vectorToScreen.y,
                                                                vectorToScreen.z, 0);
                                        }

                                        position.x = Math.min(vectorToScreen.x, position.x);
                                        position.y = Math.min(vectorToScreen.y, position.y);
                                        position.z = Math.max(vectorToScreen.x, position.z);
                                        position.w = Math.max(vectorToScreen.y, position.w);
                                }
                        }

                        if (position != null) {
                                float posX = (float) position.x;
                                float posY = (float) position.y;
                                float endPosX = (float) position.z;
                                float endPosY = (float) position.w;

                                Color accentColor = boxColor.getValue();

                                if (boxFill.getValue()) {
                                        int opacity = fillOpacity.getValueInt();
                                        Color fillColor = new Color(0, 0, 0, opacity);
                                        NanoVGRenderer.drawRect(posX, posY, endPosX - posX,
                                                        endPosY - posY, fillColor);
                                }

                                if (boxSetting.getValue()) {
                                        float shadowOffset = 1f;
                                        float lineWidth = 0.5f;

                                        if (boxMode.getMode().equals("Full")) {
                                                NanoVGRenderer.drawRect(posX - shadowOffset, posY,
                                                                lineWidth + shadowOffset, endPosY - posY + shadowOffset,
                                                                Color.BLACK);
                                                NanoVGRenderer.drawRect(posX - shadowOffset,
                                                                posY - shadowOffset, endPosX - posX + shadowOffset,
                                                                lineWidth + shadowOffset, Color.BLACK);
                                                NanoVGRenderer.drawRect(endPosX - lineWidth, posY,
                                                                lineWidth + shadowOffset, endPosY - posY + shadowOffset,
                                                                Color.BLACK);
                                                NanoVGRenderer.drawRect(posX - shadowOffset,
                                                                endPosY - lineWidth, endPosX - posX + shadowOffset,
                                                                lineWidth + shadowOffset, Color.BLACK);

                                                NanoVGRenderer.drawRect(posX - lineWidth, posY,
                                                                lineWidth, endPosY - posY, accentColor);
                                                NanoVGRenderer.drawRect(posX, endPosY - lineWidth,
                                                                endPosX - posX, lineWidth, accentColor);
                                                NanoVGRenderer.drawRect(posX, posY - lineWidth,
                                                                endPosX - posX, lineWidth, accentColor);
                                                NanoVGRenderer.drawRect(endPosX - lineWidth, posY,
                                                                lineWidth, endPosY - posY, accentColor);
                                        } else if (boxMode.getMode().equals("Rounded")) {
                                                float radius = (float) roundRadius.getValue();
                                                float boxWidth = endPosX - posX;
                                                float boxHeight = endPosY - posY;
                                                
                                                NanoVGRenderer.drawRoundedRectOutline(
                                                                posX - 1, posY - 1, boxWidth + 2, boxHeight + 2, radius, 1.5f, Color.BLACK);
                                                NanoVGRenderer.drawRoundedRectOutline(
                                                                posX, posY, boxWidth, boxHeight, radius, 1f, accentColor);
                                        } else {
                                                float boxWidth = endPosX - posX;
                                                float cornerLength = Math.min(boxWidth * 0.25f, 15f);

                                                NanoVGRenderer.drawRect(posX - shadowOffset, posY,
                                                                lineWidth + shadowOffset, cornerLength + shadowOffset,
                                                                Color.BLACK);
                                                NanoVGRenderer.drawRect(posX - shadowOffset,
                                                                posY - shadowOffset, cornerLength + shadowOffset,
                                                                lineWidth + shadowOffset, Color.BLACK);

                                                NanoVGRenderer.drawRect(endPosX - lineWidth, posY,
                                                                lineWidth + shadowOffset, cornerLength + shadowOffset,
                                                                Color.BLACK);
                                                NanoVGRenderer.drawRect(
                                                                endPosX - cornerLength - shadowOffset,
                                                                posY - shadowOffset, cornerLength + shadowOffset,
                                                                lineWidth + shadowOffset, Color.BLACK);

                                                NanoVGRenderer.drawRect(posX - shadowOffset,
                                                                endPosY - cornerLength, lineWidth + shadowOffset,
                                                                cornerLength + shadowOffset, Color.BLACK);
                                                NanoVGRenderer.drawRect(posX - shadowOffset,
                                                                endPosY - lineWidth, cornerLength + shadowOffset,
                                                                lineWidth + shadowOffset, Color.BLACK);

                                                NanoVGRenderer.drawRect(endPosX - lineWidth,
                                                                endPosY - cornerLength, lineWidth + shadowOffset,
                                                                cornerLength + shadowOffset, Color.BLACK);
                                                NanoVGRenderer.drawRect(
                                                                endPosX - cornerLength - shadowOffset,
                                                                endPosY - lineWidth, cornerLength + shadowOffset,
                                                                lineWidth + shadowOffset, Color.BLACK);

                                                NanoVGRenderer.drawRect(posX - lineWidth, posY,
                                                                lineWidth, cornerLength, accentColor);
                                                NanoVGRenderer.drawRect(posX, posY - lineWidth,
                                                                cornerLength, lineWidth, accentColor);

                                                NanoVGRenderer.drawRect(endPosX - lineWidth, posY,
                                                                lineWidth, cornerLength, accentColor);
                                                NanoVGRenderer.drawRect(endPosX - cornerLength,
                                                                posY - lineWidth, cornerLength, lineWidth, accentColor);

                                                NanoVGRenderer.drawRect(posX - lineWidth,
                                                                endPosY - cornerLength, lineWidth, cornerLength,
                                                                accentColor);
                                                NanoVGRenderer.drawRect(posX, endPosY - lineWidth,
                                                                cornerLength, lineWidth, accentColor);

                                                NanoVGRenderer.drawRect(endPosX - lineWidth,
                                                                endPosY - cornerLength, lineWidth, cornerLength,
                                                                accentColor);
                                                NanoVGRenderer.drawRect(endPosX - cornerLength,
                                                                endPosY - lineWidth, cornerLength, lineWidth,
                                                                accentColor);
                                        }
                                }

                                if (healthBar.getValue() && entity instanceof LivingEntity livingEntity) {
                                        float health = livingEntity.getHealth();
                                        float maxHealth = livingEntity.getMaxHealth();
                                        float healthPercent = Math.min(health / maxHealth, 1f);

                                        float boxWidth = endPosX - posX;
                                        float maxBarWidth = 3.5f;
                                        float barWidth = Math.min(2.5f, boxWidth * 0.08f);
                                        barWidth = Math.min(barWidth, maxBarWidth);

                                        float barHeight = endPosY - posY;
                                        float barX = posX - barWidth - 3f;
                                        float barY = posY;
                                        float yOffset = 0.3f;

                                        NanoVGRenderer.drawRect(barX, barY, barWidth + 0.5f,
                                                        barHeight, Color.BLACK);
                                        NanoVGRenderer.drawRect(barX, barY - yOffset,
                                                        barWidth, yOffset, Color.BLACK);
                                        NanoVGRenderer.drawRect(barX, barY + barHeight,
                                                        barWidth, yOffset, Color.BLACK);

                                        float healthBarHeight = barHeight * healthPercent;
                                        float healthBarY = barY + (barHeight - healthBarHeight);

                                        Color accentDark = new Color(
                                                        Math.max(0, accentColor.getRed() - 60),
                                                        Math.max(0, accentColor.getGreen() - 60),
                                                        Math.max(0, accentColor.getBlue() - 60));

                                        NanoVGRenderer.drawRoundedRectGradient(barX, healthBarY,
                                                        barWidth, healthBarHeight, 0f, accentColor, accentDark);
                                }
                        }
                }

                NanoVGRenderer.endFrame();

                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        private Color getColorForEntity(Entity entity) {
                if (entity instanceof PlayerEntity)
                        return playerColor.getValue();
                if (entity instanceof PassiveEntity)
                        return passiveColor.getValue();
                if (entity instanceof HostileEntity)
                        return hostileColor.getValue();
                return Color.WHITE;
        }

        private boolean shouldRender(Entity entity) {
                if (entity == mc.player && !showSelf.getValue())
                        return false;

                if (mc.player.distanceTo(entity) > range.getValue())
                        return false;

                return switch (targets.getMode()) {
                        case "Players" -> entity instanceof PlayerEntity;
                        case "Passives" -> entity instanceof PassiveEntity;
                        case "Hostiles" -> entity instanceof HostileEntity;
                        case "All" ->
                                entity instanceof PlayerEntity || entity instanceof PassiveEntity
                                                || entity instanceof HostileEntity;
                        default -> false;
                };
        }
}
