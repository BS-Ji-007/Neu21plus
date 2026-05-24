package io.github.legentpc.neu21plus.client;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.dungeon.DungeonFeatures;
import io.github.legentpc.neu21plus.client.mining.MiningFeatures;
import io.github.legentpc.neu21plus.client.event.ClientEventHandler;
import io.github.legentpc.neu21plus.client.listener.ChatListener;
import io.github.legentpc.neu21plus.client.listener.WorldListener;
import io.github.legentpc.neu21plus.client.notification.NotificationSystem;
import io.github.legentpc.neu21plus.client.overlay.NEUOverlay;
import io.github.legentpc.neu21plus.client.overlay.OverlayRenderer;
import io.github.legentpc.neu21plus.client.overlay.TooltipModifier;
import io.github.legentpc.neu21plus.command.NeuCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neu21PlusClientMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("neu21plus-client");

    private static KeyMapping keybindToggleOverlay;
    private static KeyMapping keybindViewRecipe;
    private static KeyMapping keybindViewUsages;
    private static KeyMapping keybindFavourite;
    private static KeyMapping keybindPreviousRecipe;
    private static KeyMapping keybindNextRecipe;

    private final ClientEventHandler eventHandler = new ClientEventHandler();
    private final ChatListener chatListener = new ChatListener();
    private final WorldListener worldListener = new WorldListener();

    private boolean repoLoadTriggered = false;

    @Override
    public void onInitializeClient() {
        registerKeyBindings();
        registerEventListeners();
        registerFabricListeners();
        registerCommands();

        LOGGER.info("Neu21+ client initialized on Minecraft 26.1 Fabric");
    }

    private void registerKeyBindings() {
        keybindToggleOverlay = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.neu21plus.toggle_overlay",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                new KeyMapping.Category(Identifier.fromNamespaceAndPath("neu21plus", "neu21plus"))
        ));

        keybindViewRecipe = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.neu21plus.view_recipe",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                new KeyMapping.Category(Identifier.fromNamespaceAndPath("neu21plus", "neu21plus"))
        ));

        keybindViewUsages = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.neu21plus.view_usages",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                new KeyMapping.Category(Identifier.fromNamespaceAndPath("neu21plus", "neu21plus"))
        ));

        keybindFavourite = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.neu21plus.favourite",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                new KeyMapping.Category(Identifier.fromNamespaceAndPath("neu21plus", "neu21plus"))
        ));

        keybindPreviousRecipe = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.neu21plus.previous_recipe",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                new KeyMapping.Category(Identifier.fromNamespaceAndPath("neu21plus", "neu21plus"))
        ));

        keybindNextRecipe = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.neu21plus.next_recipe",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                new KeyMapping.Category(Identifier.fromNamespaceAndPath("neu21plus", "neu21plus"))
        ));
    }

    private void registerEventListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!repoLoadTriggered && client.player != null) {
                repoLoadTriggered = true;
                Neu21PlusMod.getInstance().onClientReady();
            }

            eventHandler.onClientTick(client);
            NotificationSystem.getInstance().tick();
            DungeonFeatures.getInstance().tick();
            MiningFeatures.getInstance().tick();

            if (keybindToggleOverlay.consumeClick()) {
                eventHandler.onToggleOverlay();
            }
            if (keybindViewRecipe.consumeClick()) {
                eventHandler.onViewRecipe();
            }
            if (keybindViewUsages.consumeClick()) {
                eventHandler.onViewUsages();
            }
            if (keybindFavourite.consumeClick()) {
                eventHandler.onFavourite();
            }
            if (keybindPreviousRecipe.consumeClick()) {
                eventHandler.onPreviousRecipe();
            }
            if (keybindNextRecipe.consumeClick()) {
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

    private void registerCommands() {
        NeuCommand.register();
    }

    public static KeyMapping getKeybindToggleOverlay() {
        return keybindToggleOverlay;
    }

    public static KeyMapping getKeybindViewRecipe() {
        return keybindViewRecipe;
    }

    public static KeyMapping getKeybindViewUsages() {
        return keybindViewUsages;
    }

    public static KeyMapping getKeybindFavourite() {
        return keybindFavourite;
    }

    public static KeyMapping getKeybindPreviousRecipe() {
        return keybindPreviousRecipe;
    }

    public static KeyMapping getKeybindNextRecipe() {
        return keybindNextRecipe;
    }
}
