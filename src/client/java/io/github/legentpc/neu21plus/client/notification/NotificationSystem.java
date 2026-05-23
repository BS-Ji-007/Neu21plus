package io.github.legentpc.neu21plus.client.notification;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NotificationSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationSystem.class);

    private static final NotificationSystem INSTANCE = new NotificationSystem();

    private static final int NOTIFICATION_DURATION = 5000;
    private static final int NOTIFICATION_FADE = 500;
    private static final int NOTIFICATION_HEIGHT = 24;
    private static final int NOTIFICATION_PADDING = 4;
    private static final int MAX_NOTIFICATIONS = 5;

    public static NotificationSystem getInstance() {
        return INSTANCE;
    }

    private final List<Notification> activeNotifications = new ArrayList<>();

    private NotificationSystem() {
    }

    public void notify(@NotNull String message) {
        notify(message, null);
    }

    public void notify(@NotNull String message, @Nullable NotificationType type) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config != null && config.misc.notificationSound) {
            playNotificationSound(type);
        }

        Notification notification = new Notification(message, type, System.currentTimeMillis());
        activeNotifications.add(notification);

        while (activeNotifications.size() > MAX_NOTIFICATIONS) {
            activeNotifications.remove(0);
        }
    }

    public void tick() {
        long now = System.currentTimeMillis();
        activeNotifications.removeIf(n -> now - n.timestamp > NOTIFICATION_DURATION);
    }

    public void render(GuiGraphicsExtractor context, int screenWidth) {
        if (activeNotifications.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        long now = System.currentTimeMillis();

        int y = 4;

        for (int i = activeNotifications.size() - 1; i >= 0; i--) {
            Notification notification = activeNotifications.get(i);
            long elapsed = now - notification.timestamp;

            float alpha = 1.0f;
            if (elapsed < NOTIFICATION_FADE) {
                alpha = (float) elapsed / NOTIFICATION_FADE;
            } else if (elapsed > NOTIFICATION_DURATION - NOTIFICATION_FADE) {
                alpha = (float) (NOTIFICATION_DURATION - elapsed) / NOTIFICATION_FADE;
            }

            if (alpha <= 0) continue;

            String text = notification.message;
            int textWidth = client.font.width(text);
            int boxWidth = textWidth + 16;
            int boxX = screenWidth - boxWidth - 4;

            int bgColor = getColorForType(notification.type, alpha);
            int borderColor = getBorderColorForType(notification.type, alpha);

            context.fill(boxX - 2, y - 2, boxX + boxWidth + 2, y + NOTIFICATION_HEIGHT + 2, bgColor);
            context.outline(boxX - 2, y - 2, boxWidth + 4, NOTIFICATION_HEIGHT + 4, borderColor);

            int textColor = (int) (alpha * 255) << 24 | 0xFFFFFF;
            context.text(client.font, text, boxX + 6, y + 8 - client.font.lineHeight / 2, textColor, true);

            y += NOTIFICATION_HEIGHT + NOTIFICATION_PADDING;
        }
    }

    private int getColorForType(@Nullable NotificationType type, float alpha) {
        int baseColor = switch (type != null ? type : NotificationType.INFO) {
            case INFO -> 0xFF005577;
            case SUCCESS -> 0xFF007700;
            case WARNING -> 0xFF776600;
            case ERROR -> 0xFF770000;
        };
        int a = (int) (alpha * (baseColor >> 24 & 0xFF));
        return (a << 24) | (baseColor & 0x00FFFFFF);
    }

    private int getBorderColorForType(@Nullable NotificationType type, float alpha) {
        int baseColor = switch (type != null ? type : NotificationType.INFO) {
            case INFO -> 0xFF00AAFF;
            case SUCCESS -> 0xFF00FF00;
            case WARNING -> 0xFFFFFF00;
            case ERROR -> 0xFFFF0000;
        };
        int a = (int) (alpha * (baseColor >> 24 & 0xFF));
        return (a << 24) | (baseColor & 0x00FFFFFF);
    }

    private void playNotificationSound(@Nullable NotificationType type) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        SoundEvent sound = switch (type != null ? type : NotificationType.INFO) {
            case INFO -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case SUCCESS -> SoundEvents.PLAYER_LEVELUP;
            case WARNING -> SoundEvents.NOTE_BLOCK_PLING.value();
            case ERROR -> SoundEvents.VILLAGER_NO;
        };

        client.player.playSound(sound, 0.5f, 1.0f);
    }

    public void clearAll() {
        activeNotifications.clear();
    }

    public int getActiveCount() {
        return activeNotifications.size();
    }

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    private static class Notification {
        final String message;
        final NotificationType type;
        final long timestamp;

        Notification(String message, NotificationType type, long timestamp) {
            this.message = message;
            this.type = type != null ? type : NotificationType.INFO;
            this.timestamp = timestamp;
        }
    }
}
