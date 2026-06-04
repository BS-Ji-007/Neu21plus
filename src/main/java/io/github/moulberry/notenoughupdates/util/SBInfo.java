/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.util;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.CookieWarning;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.SlayerOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SBInfo {
	private static final SBInfo INSTANCE = new SBInfo();

	public static SBInfo getInstance() {
		return INSTANCE;
	}

	private static final Pattern timePattern = Pattern.compile(".+(am|pm)");

	private String location = "";
	private String lastLocation = "";
	public String date = "";
	public String time = "";
	public String objective = "";
	public String slayer = "";
	public boolean stranded = false;
	public boolean bingo = false;

	public String mode = null;
	public Date currentTimeDate = null;
	private JsonObject mayorJson = new JsonObject();

	public String currentlyOpenChestName = "";
	public String lastOpenChestName = "";

	private long lastManualLocRaw = -1;
	private long lastLocRaw = -1;
	public long joinedWorld = -1;
	private long lastMayorUpdate;
	public long unloadedWorld = -1;
	private JsonObject locraw = null;
	public boolean isInDungeon = false;

	public String currentProfile = null;

    public void registerEvents() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> onChatMessage(message));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onWorldLoad());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onWorldUnload());
    }

	public void onWorldLoad() {
		lastLocRaw = -1;
		locraw = null;
		this.mode = null;
		joinedWorld = System.currentTimeMillis();
		currentlyOpenChestName = "";
		lastOpenChestName = "";
	}

	public void onWorldUnload() {
		unloadedWorld = System.currentTimeMillis();
	}

	private static final Pattern JSON_BRACKET_PATTERN = Pattern.compile("^\\{.+}");

	public void onChatMessage(Component message) {
		String text = message.getString();
		Matcher matcher = JSON_BRACKET_PATTERN.matcher(text);
		if (matcher.find()) {
			try {
				JsonObject obj = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(matcher.group(), JsonObject.class);
				if (obj.has("server")) {
					if (obj.has("gametype") && obj.has("mode") && obj.has("map")) {
						locraw = obj;
						mode = locraw.get("mode").getAsString();
					}
				}
			} catch (Exception e) {}
		}
	}

	public String getLocation() {
		return mode;
	}

	public void tick() {
		long currentTime = System.currentTimeMillis();

		if (Minecraft.getInstance().player != null &&
			Minecraft.getInstance().level != null &&
			locraw == null &&
			(currentTime - joinedWorld) > 1000 &&
			(currentTime - lastLocRaw) > 15000) {
			lastLocRaw = System.currentTimeMillis();
			NotEnoughUpdates.INSTANCE.sendChatMessage("/locraw");
		}
        
        // Sidebar/Scoreboard parsing logic needs porting to modern Scoreboard API
	}

	public JsonObject getMayorJson() {
		return mayorJson;
	}

	public void setCurrentProfile(String newProfile) {
		if (!newProfile.equals(currentProfile)) {
			currentProfile = newProfile;
            if (NotEnoughUpdates.INSTANCE.config != null) {
                MinionHelperManager.getInstance().onProfileSwitch();
                CookieWarning.onProfileSwitch();
            }
		}
	}
}
