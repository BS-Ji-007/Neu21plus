package io.github.legentpc.neu21plus.client;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.event.ClientEventHandler;
import io.github.legentpc.neu21plus.client.listener.ChatListener;
import io.github.legentpc.neu21plus.client.listener.WorldListener;
import io.github.legentpc.neu21plus.client.overlay.OverlayRenderer;
import io.github.legentpc.neu21plus.client.overlay.TooltipModifier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neu21PlusClientMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("neu21plus-client");

    private static KeyBinding keybindToggleOverlay;
    private static KeyBinding keybindViewRecipe;
    private static KeyBinding keybindViewUsages;
    private static KeyBinding keybindFavourite;
    private static KeyBinding keybindPreviousRecipe;
    private static KeyBinding keybindNextRecipe;

    private final ClientEventHandler eventHandler = new ClientEventHandler();
    private final ChatListener chatListener = new ChatListener();
    private final WorldListener worldListener = new WorldListener();

    private boolean repoLoadTriggered = false;

    @Override
    public void onInitializeClient() {
        registerKeyBindings();
        registerEventListeners();
        registerFabricListeners();

        LOGGER.info("Neu21+ client initialized on Minecraft 26.1 Fabric");
    }

    private void registerKeyBindings() {
        keybindToggleOverlay = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neu21plus.toggle_overlay",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.neu21plus"
        ));

        keybindViewRecipe = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neu21plus.view_recipe",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.neu21plus"
        ));

        keybindViewUsages = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neu21plus.view_usages",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.neu21plus"
        ));

        keybindFavourite = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neu21plus.favourite",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                "category.neu21plus"
        ));

        keybindPreviousRecipe = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neu21plus.previous_recipe",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                "category.neu21plus"
        ));

        keybindNextRecipe = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neu21plus.next_recipe",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                "category.neu21plus"
        ));
    }

    private void registerEventListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!repoLoadTriggered && client.player != null) {
                repoLoadTriggered = true;
                Neu21PlusMod.getInstance().onClientReady();
            }

            eventHandler.onClientTick(client);

            if (keybindToggleOverlay.wasPressed()) {
                eventHandler.onToggleOverlay();
            }
            if (keybindViewRecipe.wasPressed()) {
                eventHandler.onViewRecipe();
            }
            if (keybindViewUsages.wasPressed()) {
                eventHandler.onViewUsages();
            }
            if (keybindFavourite.wasPressed()) {
                eventHandler.onFavourite();
            }
            if (keybindPreviousRecipe.wasPressed()) {
                eventHandler.onPreviousRecipe();
            }
            if (keybindNextRecipe.wasPressed()) {
                eventHandler.onNextRecipe();
            }
        });
    }

    private void registerFabricListeners() {
        chatListener.register();
        worldListener.register();

        OverlayRenderer.getInstance().register();
        TooltipModifier.getInstance().register();
    }

    public static KeyBinding getKeybindToggleOverlay() {
        return keybindToggleOverlay;
    }

    public static KeyBinding getKeybindViewRecipe() {
        return keybindViewRecipe;
    }

    public static KeyBinding getKeybindViewUsages() {
        return keybindViewUsages;
    }

    public static KeyBinding getKeybindFavourite() {
        return keybindFavourite;
    }

    public static KeyBinding getKeybindPreviousRecipe() {
        return keybindPreviousRecipe;
    }

    public static KeyBinding getKeybindNextRecipe() {
        return keybindNextRecipe;
    }
}
