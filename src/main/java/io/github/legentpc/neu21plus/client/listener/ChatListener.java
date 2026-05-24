package io.github.legentpc.neu21plus.client.listener;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.actionbar.ActionBarDisplay;
import io.github.legentpc.neu21plus.client.bazaar.BazaarHelper;
import io.github.legentpc.neu21plus.client.collection.CollectionDisplay;
import io.github.legentpc.neu21plus.client.dungeon.DungeonFeatures;
import io.github.legentpc.neu21plus.client.fairysoul.FairySouls;
import io.github.legentpc.neu21plus.client.mining.MiningFeatures;
import io.github.legentpc.neu21plus.client.misc.MiscFeatures;
import io.github.legentpc.neu21plus.client.storage.StorageViewer;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatListener.class);

    private static final Pattern SLAYER_EXP_PATTERN = Pattern.compile(
            "   (Spider|Zombie|Wolf|Enderman|Blaze) Slayer LVL (\\d) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)"
    );

    private static final Pattern SKY_BLOCK_LEVEL_PATTERN = Pattern.compile("\\[(\\d{1,4})\\] .*");

    public void register() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::onReceiveGameMessage);
        ClientReceiveMessageEvents.MODIFY_GAME.register(this::modifyGameMessage);
        ClientSendMessageEvents.ALLOW_CHAT.register(this::onSendChat);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onWorldJoin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onWorldLeave());
    }

    private boolean onReceiveGameMessage(Component message, boolean overlay) {
        if (overlay) {
            ActionBarDisplay.getInstance().onActionBar(message);
            return true;
        }

        SBInfo sbInfo = SBInfo.getInstance();
        sbInfo.onChatMessage(message);

        String text = message.getString();

        Matcher slayerMatcher = SLAYER_EXP_PATTERN.matcher(text);
        if (slayerMatcher.find()) {
            String slayerType = slayerMatcher.group(1);
            int slayerLevel = Integer.parseInt(slayerMatcher.group(2));
            LOGGER.debug("Slayer detected: {} LVL {}", slayerType, slayerLevel);
        }

        DungeonFeatures.getInstance().onChatMessage(message);
        MiningFeatures.getInstance().onChatMessage(message);
        MiscFeatures.getInstance().onChatMessage(message);
        StorageViewer.getInstance().onChatMessage(message);
        BazaarHelper.getInstance().onChatMessage(message);
        CollectionDisplay.getInstance().onChatMessage(message);
        FairySouls.getInstance().onChatMessage(message);

        return true;
    }

    private Component modifyGameMessage(Component message, boolean overlay) {
        if (overlay) return message;

        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null) return message;

        return message;
    }

    private boolean onSendChat(String message) {
        SBInfo.getInstance().onSendChatMessage(message);
        return true;
    }

    private void onWorldJoin() {
        LOGGER.info("World joined");
        SBInfo.getInstance().onWorldLoad();
    }

    private void onWorldLeave() {
        LOGGER.info("World left");
        DungeonFeatures.getInstance().reset();
        MiningFeatures.getInstance().reset();
        MiscFeatures.getInstance().reset();
        StorageViewer.getInstance().reset();
        BazaarHelper.getInstance().reset();
        CollectionDisplay.getInstance().reset();
        FairySouls.getInstance().reset();
        ActionBarDisplay.getInstance().reset();
    }
}
