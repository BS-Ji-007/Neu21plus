/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.profileviewer.level;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.profileviewer.BasicPage;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewerPage;
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.CoreTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.DungeonTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.EssenceTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.EventTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.GuiTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.MiscTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.SkillRelatedTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.SlayingTaskLevel;
import io.github.moulberry.notenoughupdates.profileviewer.level.task.StoryTaskLevel;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelPage extends GuiProfileViewerPage {

	private static final ResourceLocation pv_levels = new ResourceLocation("notenoughupdates", "notenoughupdates:pv_levels.png");
	private final BasicPage basicPage;
	private final JsonObject constant;
	private final List<GuiTaskLevel> tasks = new ArrayList<>();

	public LevelPage(GuiProfileViewer instance, BasicPage basicPage) {
		super(instance);
		this.basicPage = basicPage;
		constant = Constants.SBLEVELS;

		tasks.add(new CoreTaskLevel(this));
		tasks.add(new DungeonTaskLevel(this));
		tasks.add(new EssenceTaskLevel(this));
		tasks.add(new MiscTaskLevel(this));
		tasks.add(new SkillRelatedTaskLevel(this));
		tasks.add(new SlayingTaskLevel(this));
		tasks.add(new StoryTaskLevel(this));
		tasks.add(new EventTaskLevel(this));
	}

	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		basicPage.drawSideButtons(mouseX, mouseY);

		if (constant == null) {
			Utils.showOutdatedRepoNotification("sblevels.json");
			return;
		}

		Minecraft.getInstance().getTextureManager().bindTexture(pv_levels);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		double skyblockLevel = selectedProfile.getSkyblockLevel();
		JsonObject profileInfo = selectedProfile.getProfileJson();

		drawMainBar(skyblockLevel, mouseX, mouseY, guiLeft, guiTop);
		tasks.forEach(task -> task.drawTask(profileInfo, mouseX, mouseY, guiLeft, guiTop));
	}

	public void renderLevelBar(
		String name,
		ItemStack stack,
		int x,
		int y,
		int xSize,
		double level,
		double xp,
		double max,
		int mouseX,
		int mouseY,
		boolean percentage,
		List<String> tooltip
	) {

		if (xp < 0) xp = 0;
		double experienceRequired = (xp / max);

		String second = ChatFormatting.WHITE.toString() + (int) level;
		if (percentage) {
			second = ChatFormatting.WHITE.toString() + (int) (experienceRequired * 100) + "%";
		}
		Utils.renderAlignedString(
			ChatFormatting.RED + name,
			second,
			x + 14,
			y - 4,
			xSize - 20
		);

		if (xp >= max) {
			getInstance().renderGoldBar(x, y + 6, xSize);
		} else {
			getInstance().renderBar(x, y + 6, xSize, (float) experienceRequired);
		}
		String levelStr;
		if (mouseX > x && mouseX < x + 120) {
			if (mouseY > y - 4 && mouseY < y + 13) {
				String xpFormatted = StringUtils.formatNumber((int) xp);
				String maxFormatted = StringUtils.formatNumber((int) max);

				levelStr =
					ChatFormatting.GRAY + "Progress: " + ChatFormatting.DARK_PURPLE + (int) (experienceRequired * 100) +
						"%" +
						" §8(" + xpFormatted + "/" + maxFormatted + " XP)";
				if (tooltip != null && !tooltip.isEmpty()) {
					tooltip.add("");
					tooltip.add(levelStr);
					getInstance().tooltipToDisplay = tooltip;
				} else {
					getInstance().tooltipToDisplay = Utils.createList(levelStr);
				}
			}
		}

		com.mojang.blaze3d.systems.RenderSystem.enableDepth();
		GL11.glTranslatef((x), (y - 6f), 0);
		GL11.glScalef(0.7f, 0.7f, 1);
		Utils.drawItemStackLinear(stack, 0, 0);
		GL11.glScalef(1 / 0.7f, 1 / 0.7f, 1);
		GL11.glTranslatef(-(x), -(y - 6f), 0);
		com.mojang.blaze3d.systems.RenderSystem.disableDepth();
	}

	private void drawMainBar(double skyblockLevel, int mouseX, int mouseY, int guiLeft, int guiTop) {
		renderLevelBar(
			"Level",
			BasicPage.skull,
			guiLeft + 163, guiTop + 30,
			110,
			skyblockLevel,
			Math.round((skyblockLevel - (long) skyblockLevel) * 100),
			100,
			mouseX, mouseY,
			false,
			Collections.emptyList()
		);
	}

	public String buildLore(String name, double xpGotten, double xpGainful, boolean hasNoLimit) {
		String xpGottenFormatted = StringUtils.formatNumber((int) xpGotten);
		String xpGainfulFormatted = StringUtils.formatNumber((int) xpGainful);

		if (xpGainful == 0 && xpGotten == 0 && !hasNoLimit) {
			return ChatFormatting.GOLD + name + ": §c§lNOT DETECTABLE!";
		}
		if (hasNoLimit) {
			return ChatFormatting.GOLD + name + ": " + ChatFormatting.YELLOW + xpGottenFormatted + " XP";
		}
		int percentage = (int) ((xpGotten / xpGainful) * 100);
		if (xpGotten >= xpGainful) {
			return ChatFormatting.GOLD + name + ": " + ChatFormatting.GREEN
				+ percentage + "%" + " §8(" + xpGottenFormatted + "/" + xpGainfulFormatted + " XP)";
		} else if (xpGotten == -1) {
			return ChatFormatting.GOLD + name + ": §c§lCOLLECTION DISABLED!";
		} else {

			return ChatFormatting.GOLD + name + ": " + ChatFormatting.YELLOW
				+ percentage + "%" + " §8(" + xpGottenFormatted + "/" + xpGainfulFormatted + " XP)";
		}
	}

	public JsonObject getConstant() {
		return constant;
	}
}
