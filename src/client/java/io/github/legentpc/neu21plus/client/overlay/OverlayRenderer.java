package io.github.legentpc.neu21plus.client.overlay;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.itemrepo.ItemResolutionQuery;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverlayRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverlayRenderer.class);

    private static OverlayRenderer instance;

    public static OverlayRenderer getInstance() {
        if (instance == null) {
            instance = new OverlayRenderer();
        }
        return instance;
    }

    private boolean registered = false;

    private OverlayRenderer() {
    }

    public void register() {
        if (registered) return;

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof HandledScreen) {
                registerForScreen(screen, client);
            }
        });

        registered = true;
        LOGGER.info("Overlay renderer registered");
    }

    private void registerForScreen(net.minecraft.client.gui.screen.Screen screen, MinecraftClient client) {
        NEUOverlay overlay = NEUOverlay.getInstance();

        ScreenEvents.afterRender(screen).register((s, drawContext, mouseX, mouseY, tickDelta) -> {
            overlay.render(drawContext, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        });

        ScreenMouseEvents.beforeMouseClick(screen).register((s, mouseX, mouseY, button) -> {
            if (overlay.onMouseClick(mouseX, mouseY, button)) {
                return;
            }
        });

        ScreenMouseEvents.beforeMouseScroll(screen).register((s, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            if (overlay.onMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return;
            }
        });

        ScreenKeyboardEvents.beforeKeyPress(screen).register((s, key, scancode, modifiers) -> {
            overlay.onKeyPress(key, scancode, modifiers);
        });
    }
}
