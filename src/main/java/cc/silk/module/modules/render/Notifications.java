package cc.silk.module.modules.render;

import cc.silk.event.impl.render.Render2DEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.utils.notification.Notification;
import cc.silk.utils.notification.NotificationManager;
import cc.silk.utils.render.nanovg.NanoVGRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class Notifications extends Module {

    private static final int NOTIFICATION_WIDTH = 160;
    private static final int NOTIFICATION_HEIGHT = 35;
    private static final int NOTIFICATION_SPACING = 5;
    private static final int MARGIN_RIGHT = 8;
    private static final int MARGIN_BOTTOM = 8;
    private static final int ICON_SIZE = 20;
    private static final int CORNER_RADIUS = 6;

    private static final Color ENABLED_COLOR = new Color(0, 255, 0);
    private static final Color DISABLED_COLOR = new Color(255, 0, 0);
    private static final Color BUFF_EXPIRED_COLOR = new Color(255, 165, 0);

    public Notifications() {
        super("Notifications", "Toggle notification display on/off", Category.RENDER);
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        List<Notification> notifications = NotificationManager.getInstance().getNotifications();
        if (notifications.isEmpty()) {
            return;
        }

        updateNotifications(notifications);
        NanoVGRenderer.beginFrame();
        renderNotifications(event.getWidth(), event.getHeight(), notifications);
        NanoVGRenderer.endFrame();
    }

    private void updateNotifications(List<Notification> notifications) {
        Iterator<Notification> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            Notification notification = iterator.next();

            if (notification.isExpired() && !notification.isRemoving()) {
                notification.setRemoving(true);
            }

            float targetProgress = notification.isRemoving() ? 0f : 1f;
            notification
                    .setAnimationProgress(MathHelper.lerp(0.15f, notification.getAnimationProgress(), targetProgress));

            if (notification.isRemoving() && notification.getAnimationProgress() < 0.05f) {
                iterator.remove();
            }
        }
    }

    private void renderNotifications(int screenWidth, int screenHeight, List<Notification> notifications) {
        float yOffset = 0;

        for (int i = notifications.size() - 1; i >= 0; i--) {
            Notification notification = notifications.get(i);

            float x = screenWidth - NOTIFICATION_WIDTH - MARGIN_RIGHT;
            float y = screenHeight - MARGIN_BOTTOM - NOTIFICATION_HEIGHT - yOffset;

            float slideProgress = notification.getAnimationProgress();
            float slideOffset = (1f - slideProgress) * (NOTIFICATION_WIDTH + 20);
            x += slideOffset;

            renderNotification(notification, x, y);

            yOffset += (NOTIFICATION_HEIGHT + NOTIFICATION_SPACING) * slideProgress;
        }
    }

    private void renderNotification(Notification notification, float x, float y) {
        float alpha = notification.getAnimationProgress();

        Color statusColor = switch (notification.getType()) {
            case MODULE_ENABLED -> ENABLED_COLOR;
            case MODULE_DISABLED -> DISABLED_COLOR;
            case BUFF_EXPIRED -> BUFF_EXPIRED_COLOR;
        };

        Color bgColor = new Color(25, 25, 30, (int) (200 * alpha));
        Color shadowColor = new Color(0, 0, 0, (int) (80 * alpha));
        NanoVGRenderer.drawRoundedRectWithShadow(x, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT, CORNER_RADIUS, bgColor, shadowColor, 12f, 2f);

        float iconCenterX = x + 18;
        float iconCenterY = y + NOTIFICATION_HEIGHT / 2f;
        float iconRadius = ICON_SIZE / 2f;

        Color iconBgColor = new Color(35, 35, 40, (int) (255 * alpha));
        NanoVGRenderer.drawCircle(iconCenterX, iconCenterY, iconRadius, iconBgColor);

        Color iconOutlineColor = new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(),
                (int) (255 * alpha));
        NanoVGRenderer.drawCircle(iconCenterX, iconCenterY, iconRadius + 1, iconOutlineColor);
        NanoVGRenderer.drawCircle(iconCenterX, iconCenterY, iconRadius - 1, iconBgColor);

        int fontId = NanoVGRenderer.getRegularFontId();
        Color iconTextColor = new Color(255, 255, 255, (int) (255 * alpha));
        float iconTextSize = 11f;
        String iconSymbol = "i";
        float iconTextWidth = NanoVGRenderer.getTextWidthWithFont(iconSymbol, iconTextSize, fontId);
        NanoVGRenderer.drawTextWithFont(iconSymbol, iconCenterX - iconTextWidth / 2, iconCenterY - iconTextSize / 2 + 1,
                iconTextSize, iconTextColor, fontId);

        float textX = x + 35;

        Color titleColor = new Color(255, 255, 255, (int) (255 * alpha));
        NanoVGRenderer.drawTextWithFont(notification.getTitle(), textX, y + 8, 11f, titleColor, fontId);

        Color messageColor = new Color(180, 180, 180, (int) (255 * alpha));
        NanoVGRenderer.drawTextWithFont(notification.getMessage(), textX, y + 21, 9f, messageColor, fontId);

        float progressWidth = NOTIFICATION_WIDTH * (1f - notification.getLifetimeProgress());
        if (progressWidth > 0) {
            Color progressColor = new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(),
                    (int) (100 * alpha));

            float progressInset = 2f;
            float progressX = x + progressInset;
            float progressY = y + NOTIFICATION_HEIGHT - 4;
            float progressHeight = 2;
            float adjustedProgressWidth = Math.min(progressWidth - progressInset,
                    NOTIFICATION_WIDTH - (progressInset * 2));

            float progressRadius = 1f;
            NanoVGRenderer.drawRoundedRect(progressX, progressY, adjustedProgressWidth, progressHeight, progressRadius,
                    progressColor);
        }
    }
}