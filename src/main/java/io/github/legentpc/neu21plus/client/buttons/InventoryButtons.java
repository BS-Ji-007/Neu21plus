package io.github.legentpc.neu21plus.client.buttons;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class InventoryButtons {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryButtons.class);

    private static final InventoryButtons INSTANCE = new InventoryButtons();

    public static InventoryButtons getInstance() {
        return INSTANCE;
    }

    private final List<InventoryButton> buttons = new ArrayList<>();

    private InventoryButtons() {
        initDefaultButtons();
    }

    private void initDefaultButtons() {
        buttons.add(new InventoryButton("Storage", "storage", 0xFF5555FF, 0));
        buttons.add(new InventoryButton("Craft", "craft", 0xFF55FF55, 1));
        buttons.add(new InventoryButton("Wardrbe", "wardrobe", 0xFF5555FF, 2));
        buttons.add(new InventoryButton("Pets", "pets", 0xFFFFAA00, 3));
        buttons.add(new InventoryButton("SBMenu", "sbmenu", 0xFF55FFFF, 4));
        buttons.add(new InventoryButton("AH", "ah", 0xFFFF5555, 5));
        buttons.add(new InventoryButton("BZ", "bz", 0xFF55FF55, 6));
        buttons.add(new InventoryButton("Collect", "collection", 0xFFAA55FF, 7));
    }

    public void render(GuiGraphicsExtractor context, AbstractContainerScreen<?> screen) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.inventory.inventoryButtons) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        Minecraft client = Minecraft.getInstance();

        int startX = 2;
        int startY = 2;
        int buttonSize = 20;
        int spacing = 2;

        for (InventoryButton button : buttons) {
            int y = startY + button.index * (buttonSize + spacing);
            if (y + buttonSize > client.getWindow().getGuiScaledHeight()) {
                continue;
            }

            boolean hovered = isHovered(client, startX, y, buttonSize, buttonSize);
            int bgColor = hovered ? 0xA0222222 : 0x80000000;
            int borderColor = button.color | 0xFF000000;

            context.fill(startX, y, startX + buttonSize, y + buttonSize, bgColor);
            context.outline(startX, y, buttonSize, buttonSize, borderColor);

            String label = button.label;
            if (label.length() > 4) {
                label = label.substring(0, 4);
            }
            context.text(client.font, label, startX + buttonSize / 2 - client.font.width(label) / 2, y + (buttonSize - client.font.lineHeight) / 2, button.color, true);
        }
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.inventory.inventoryButtons) return false;

        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof AbstractContainerScreen<?>)) return false;

        int startX = 2;
        int startY = 2;
        int buttonSize = 20;
        int spacing = 2;

        for (InventoryButton btn : buttons) {
            int y = startY + btn.index * (buttonSize + spacing);
            if (mouseX >= startX && mouseX < startX + buttonSize && mouseY >= y && mouseY < y + buttonSize) {
                if (client.player != null) {
                    client.player.connection.sendCommand(btn.command);
                }
                return true;
            }
        }

        return false;
    }

    private boolean isHovered(Minecraft client, int x, int y, int width, int height) {
        double mouseX = client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth() / client.getWindow().getScreenWidth();
        double mouseY = client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight() / client.getWindow().getScreenHeight();
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public List<InventoryButton> getButtons() {
        return buttons;
    }

    public static class InventoryButton {
        public final String label;
        public final String command;
        public final int color;
        public final int index;

        public InventoryButton(String label, String command, int color, int index) {
            this.label = label;
            this.command = command;
            this.color = color;
            this.index = index;
        }
    }
}
