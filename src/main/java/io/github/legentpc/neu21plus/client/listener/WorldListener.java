package io.github.legentpc.neu21plus.client.listener;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.dungeon.DungeonFeatures;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldListener.class);

    private String lastChestName = "";

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft client) {
        SBInfo sbInfo = SBInfo.getInstance();
        sbInfo.tick();

        if (client.screen instanceof AbstractContainerScreen<?> containerScreen) {
            Component title = containerScreen.getTitle();
            if (title != null) {
                String chestName = title.getString();
                if (!chestName.equals(sbInfo.currentlyOpenChestName)) {
                    sbInfo.lastOpenChestName = sbInfo.currentlyOpenChestName;
                    sbInfo.currentlyOpenChestName = chestName;
                }
                if (!chestName.equals(lastChestName)) {
                    lastChestName = chestName;
                    DungeonFeatures.getInstance().onChestTitleChanged(chestName);
                }
            }
        } else {
            sbInfo.currentlyOpenChestName = "";
            lastChestName = "";
        }
    }
}
