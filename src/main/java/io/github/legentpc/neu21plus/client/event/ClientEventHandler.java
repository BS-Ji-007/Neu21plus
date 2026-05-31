package io.github.legentpc.neu21plus.client.event;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.overlay.CraftingOverlay;
import io.github.legentpc.neu21plus.client.overlay.NEUOverlay;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientEventHandler.class);

    private int tickCount = 0;

    public void onClientTick(Minecraft client) {
        tickCount++;

        NEUOverlay overlay = NEUOverlay.getInstance();
        overlay.tick();
    }

    public void onToggleOverlay() {
        NEUOverlay.getInstance().toggleOverlay();
    }

    public void onViewRecipe() {
        NEUOverlay.getInstance().viewRecipe();
    }

    public void onViewUsages() {
        NEUOverlay.getInstance().viewUsages();
    }

    public void onFavourite() {
        NEUOverlay.getInstance().toggleFavourite();
    }

    public void onPreviousRecipe() {
        NEUOverlay.getInstance().navigatePrevious();
    }

    public void onNextRecipe() {
        NEUOverlay.getInstance().navigateNext();
    }
}
