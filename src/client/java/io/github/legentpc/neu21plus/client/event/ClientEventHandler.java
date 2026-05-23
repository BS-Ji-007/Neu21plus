package io.github.legentpc.neu21plus.client.event;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("neu21plus-events");

    private int tickCount = 0;

    public void onClientTick(MinecraftClient client) {
        tickCount++;
    }

    public void onToggleOverlay() {
        LOGGER.debug("Toggle overlay pressed");
    }

    public void onViewRecipe() {
        LOGGER.debug("View recipe pressed");
    }

    public void onViewUsages() {
        LOGGER.debug("View usages pressed");
    }

    public void onFavourite() {
        LOGGER.debug("Favourite pressed");
    }

    public void onPreviousRecipe() {
        LOGGER.debug("Previous recipe pressed");
    }

    public void onNextRecipe() {
        LOGGER.debug("Next recipe pressed");
    }
}
