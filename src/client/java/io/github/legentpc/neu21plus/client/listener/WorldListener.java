package io.github.legentpc.neu21plus.client.listener;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldListener.class);

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        SBInfo sbInfo = SBInfo.getInstance();
        sbInfo.tick();

        if (client.currentScreen instanceof GenericContainerScreen containerScreen) {
            ScreenHandler handler = containerScreen.getScreenHandler();
            if (handler.slots.size() > 0) {
                Slot firstSlot = handler.getSlot(0);
                Text inventoryName = firstSlot.inventory.getName();
                if (inventoryName != null) {
                    String chestName = inventoryName.getString();
                    if (!chestName.equals(sbInfo.currentlyOpenChestName)) {
                        sbInfo.lastOpenChestName = sbInfo.currentlyOpenChestName;
                        sbInfo.currentlyOpenChestName = chestName;
                    }
                }
            }
        } else {
            sbInfo.currentlyOpenChestName = "";
        }
    }
}
