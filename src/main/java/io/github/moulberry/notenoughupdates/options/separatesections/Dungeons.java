/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.options.separatesections;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.*;

public class Dungeons {
	@ConfigOption(name = "Dungeon Map", desc = "")
	@ConfigEditorAccordion(id = 0)
	public boolean dungeonMapAccordion = false;

	@Expose
	@ConfigOption(name = "Edit Dungeon Map", desc = "The NEU dungeon map has its own editor (/neumap)")
	@ConfigEditorButton(runnableId = 0, buttonText = "Edit")
	@ConfigAccordionId(id = 0)
	public int editDungeonMap = 0;

	@Expose
	@ConfigOption(name = "Profit Type", desc = "Set the price dataset used for calculating profit")
	@ConfigEditorDropdown(values = {"Lowest BIN", "24 AVG Lowest Bin", "Auction AVG"})
	@ConfigAccordionId(id = 1)
	public int profitType = 0;
}
