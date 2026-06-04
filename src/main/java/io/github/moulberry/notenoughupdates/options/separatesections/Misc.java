/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.options.separatesections;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.*;
import com.mojang.blaze3d.platform.InputConstants;

public class Misc {
	@Expose
	@ConfigOption(name = "Only Show on SkyBlock", desc = "The item list and some other GUI elements will only show on SkyBlock")
	@ConfigEditorBoolean
	public boolean onlyShowOnSkyblock = true;

	@Expose
	@ConfigOption(name = "Hide Potion Effects", desc = "Hide the potion effects inside your inventory while on SkyBlock")
	@ConfigEditorBoolean
	public boolean hidePotionEffect = true;

	@Expose
	@ConfigOption(name = "Streamer Mode", desc = "Randomize lobby names in the scoreboard and chat messages to help prevent stream sniping")
	@ConfigEditorBoolean
	public boolean streamerMode = false;

	@Expose
	@ConfigOption(name = "Waypoint Keybind", desc = "Press this keybind to show waypoints to various NPCs")
	@ConfigEditorKeybind(defaultKey = -1)
	public int keybindWaypoint = -1;

	@Expose
	@ConfigOption(name = "Search AH/BZ for current item", desc = "Search AH/BZ for the item you are hovering over")
	@ConfigEditorKeybind(defaultKey = 77) // 'M' key
	public int openAHKeybind = 77;
}
