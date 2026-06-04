/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.listener;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin;
import io.github.moulberry.notenoughupdates.miscfeatures.CookieWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalMetalDetectorSolver;
import io.github.moulberry.notenoughupdates.miscfeatures.EnderNodes;
import io.github.moulberry.notenoughupdates.miscfeatures.StreamerMode;
import io.github.moulberry.notenoughupdates.miscfeatures.world.EnderNodeHighlighter;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.SlayerOverlay;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringUtil;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.moulberry.notenoughupdates.overlays.SlayerOverlay.RNGMeter;
import static io.github.moulberry.notenoughupdates.overlays.SlayerOverlay.slayerXp;
import static io.github.moulberry.notenoughupdates.overlays.SlayerOverlay.timeSinceLastBoss;
import static io.github.moulberry.notenoughupdates.overlays.SlayerOverlay.timeSinceLastBoss2;

public class ChatListener {

	private final NotEnoughUpdates neu;

	private static final Pattern SLAYER_EXP_PATTERN = Pattern.compile(
		"   (Spider|Zombie|Wolf|Enderman|Blaze) Slayer LVL (\\d) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)");
	private static final Pattern SKY_BLOCK_LEVEL_PATTERN = Pattern.compile("\\[(\\d{1,4})\\] .*");
	private final Pattern PARTY_FINDER_PATTERN = Pattern.compile("§dParty Finder §r§f> (.*)§ejoined the (dungeon )?group!");

	private AtomicBoolean missingRecipe = new AtomicBoolean(false);

	public ChatListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

    public void registerEvents() {
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            onChatMessage(message, false);
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            onChatMessage(message, overlay);
        });
    }

	private String processText(String text) {
		if (SBInfo.getInstance().getLocation() == null) return text;
		if (!SBInfo.getInstance().getLocation().startsWith("mining_") && !SBInfo.getInstance().getLocation().equals(
			"crystal_hollows"))
			return text;

		if (Minecraft.getInstance().player == null) return text;
		if (!NotEnoughUpdates.INSTANCE.config.mining.drillFuelBar) return text;

		return Utils.trimIgnoreColour(text.replaceAll(ChatFormatting.DARK_GREEN + "\\S+ Drill Fuel", ""));
	}

	private Component processChatComponent(Component chatComponent) {
        // Simplified for modern Component architecture
        String unformatted = chatComponent.getString();
        return Component.literal(processText(unformatted)).withStyle(chatComponent.getStyle());
	}

	private void onChatMessage(Component message, boolean isOverlay) {
		String unformatted = Utils.cleanColour(message.getString());
		Matcher matcher = SLAYER_EXP_PATTERN.matcher(unformatted);
		
        if (unformatted.startsWith("You are playing on profile: ")) {
			SBInfo.getInstance().setCurrentProfile(unformatted
				.substring("You are playing on profile: ".length())
				.split(" ")[0].trim());
		} else if (unformatted.startsWith("Your profile was changed to: ")) {
			SBInfo.getInstance().setCurrentProfile(unformatted
				.substring("Your profile was changed to: ".length())
				.split(" ")[0].trim());
		}

		if (unformatted.equals("  SLAYER QUEST FAILED!")) {
			SlayerOverlay.isSlain = false;
			timeSinceLastBoss = 0;
		} else if (unformatted.equals("  NICE! SLAYER BOSS SLAIN!")) {
			SlayerOverlay.isSlain = true;
		} else if (unformatted.equals("  SLAYER QUEST STARTED!")) {
			SlayerOverlay.isSlain = false;
			if (timeSinceLastBoss != 0) {
				timeSinceLastBoss2 = timeSinceLastBoss;
			}
			timeSinceLastBoss = System.currentTimeMillis();
		} else if (unformatted.startsWith("   RNG Meter")) {
			RNGMeter = unformatted.substring("   RNG Meter - ".length());
		} else if (matcher.matches()) {
			SlayerOverlay.slayerLVL = matcher.group(2);
			if (!SlayerOverlay.slayerLVL.equals("9")) {
				SlayerOverlay.slayerXp = matcher.group(3);
			} else {
				slayerXp = "maxed";
			}
		} else if (unformatted.startsWith("Sending to server") || (unformatted.startsWith(
			"Your Slayer Quest has been cancelled!"))) {
			SlayerOverlay.slayerQuest = false;
			SlayerOverlay.unloadOverlayTimer = System.currentTimeMillis();
		} else if (unformatted.startsWith("You consumed a Booster Cookie!")) {
			CookieWarning.resetNotification();
		}

		OverlayManager.powderGrindingOverlay.onMessage(unformatted);

		if (unformatted.startsWith("ENDER NODE!"))
			EnderNodeHighlighter.getInstance().highlightedBlocks.clear();

		if (unformatted.equals("ENDER NODE! You found Endermite Nest!"))
			EnderNodes.displayEndermiteNotif();
	}
}
