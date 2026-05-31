package io.github.legentpc.neu21plus.client.storage;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.notification.NotificationSystem;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StorageViewer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageViewer.class);

    private static final StorageViewer INSTANCE = new StorageViewer();

    private static final Pattern STORAGE_PATTERN = Pattern.compile(
            ".*Storage Slot (\\d+).*"
    );
    private static final Pattern BACKPACK_PATTERN = Pattern.compile(
            ".*Backpack Slot (\\d+).*"
    );

    public static StorageViewer getInstance() {
        return INSTANCE;
    }

    private final List<StorageSlot> storageSlots = new ArrayList<>();
    private boolean viewingStorage = false;
    private int tickCount = 0;

    private StorageViewer() {
        for (int i = 0; i < 18; i++) {
            storageSlots.add(new StorageSlot(i + 1));
        }
    }

    public void tick() {
        tickCount++;

        if (tickCount % 200 == 0) {
            updateStorageData();
        }
    }

    private void updateStorageData() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        String openContainer = sbInfo.getOpenChestName();
        if (openContainer != null) {
            Matcher storageMatcher = STORAGE_PATTERN.matcher(openContainer);
            if (storageMatcher.find()) {
                viewingStorage = true;
                return;
            }

            Matcher backpackMatcher = BACKPACK_PATTERN.matcher(openContainer);
            if (backpackMatcher.find()) {
                viewingStorage = true;
                return;
            }
        }

        viewingStorage = false;
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        String cleaned = text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();

        if (cleaned.contains("Storage") || cleaned.contains("Backpack")) {
            updateStorageData();
        }
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.storage.enabled || !config.storage.showOverlay) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        Minecraft client = Minecraft.getInstance();
        float scale = config.storage.storageScale;

        int x = screenWidth - (int) (140 * scale);
        int y = 10;
        int lineH = client.font.lineHeight + 2;

        context.fill(x - 4, y - 4, x + (int) (138 * scale), y + (int) (storageSlots.size() * (lineH + 1) * scale / 2 + 20), 0x80000000);
        context.outline(x - 4, y - 4, (int) (142 * scale), (int) (storageSlots.size() * (lineH + 1) * scale / 2 + 20), 0xFF555555);

        context.text(client.font, "\u00a76Storage", x, y, 0xFFFFAA00, true);
        y += lineH + 4;

        int col = 0;
        for (StorageSlot slot : storageSlots) {
            String status = slot.hasContent ? "\u00a7a\u2714" : "\u00a7c\u2716";
            String text = status + " \u00a77#" + slot.slotNumber;

            int drawX = x + col * 70;
            context.text(client.font, text, drawX, y, 0xFFAAAAAA, false);

            col++;
            if (col >= 2) {
                col = 0;
                y += lineH;
            }
        }
    }

    public boolean isViewingStorage() {
        return viewingStorage;
    }

    public void openStorageMenu() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.storage.enabled) {
            NotificationSystem.getInstance().notify("\u00a7cStorage viewer is disabled in config", NotificationSystem.NotificationType.ERROR);
            return;
        }

        client.player.connection.sendCommand("storage");
    }

    public void reset() {
        viewingStorage = false;
        for (StorageSlot slot : storageSlots) {
            slot.hasContent = false;
        }
    }

    private static class StorageSlot {
        final int slotNumber;
        boolean hasContent = false;

        StorageSlot(int slotNumber) {
            this.slotNumber = slotNumber;
        }
    }
}
