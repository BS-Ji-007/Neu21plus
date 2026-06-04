/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscgui;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.mixins.AccessorContainerScreen;
import io.github.moulberry.notenoughupdates.util.PetLeveling;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.ChestScreen;
import net.minecraft.client.renderer.com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.init.Blocks;
import net.minecraft.world.inventory.AbstractContainerMenuChest;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class KatSitterOverlay {
	public KatSitterOverlay() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onGuiDrawn(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!(event.gui instanceof ChestScreen)) return;
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.showKatSitting) return;
		ChestScreen gui = (ChestScreen) event.gui;
		ContainerChest container = (ContainerChest) gui.inventorySlots;
		if (!"Pet Sitter".equals(container.getLowerChestInventory().getName().getString().getUnformattedText())) return;
		Slot slot = container.getSlot(13);
		if (slot == null || !slot.getHasStack() || slot.getStack() == null) return;
		ItemStack item = slot.getStack();
		CompoundTag tagCompound = item.getTag();
		if (tagCompound == null || !tagCompound.hasKey("ExtraAttributes", 10)) return;
		CompoundTag extra = tagCompound.getCompoundTag("ExtraAttributes");
		if (extra == null || !extra.hasKey("id", 8) ||
			!"PET".equals(extra.getString("id")) || !extra.hasKey("petInfo", 8))
			return;
		JsonObject petInfo = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(extra.getString("petInfo"), JsonObject.class);
		if (petInfo == null || !petInfo.has("exp") || !petInfo.has("tier") || !petInfo.has("type")) return;
		String petId = petInfo.get("type").getAsString();
		double xp = petInfo.get("exp").getAsDouble();
		PetInfoOverlay.Rarity rarity = PetInfoOverlay.Rarity.valueOf(petInfo.get("tier").getAsString());
		Slot katSlot = container.getSlot(22);
		PetInfoOverlay.Rarity upgradedRarity = rarity.nextRarity();
		boolean nextRarityPresent = katSlot.getStack() != null && katSlot.getStack().getItem() != Item.getItemFromBlock(
			Blocks.barrier) && upgradedRarity != null;
		renderPetInformation(
			PetLeveling
				.getPetLevelingForPet(petId, rarity)
				.getPetLevel(xp)
				.getCurrentLevel(),
			nextRarityPresent ?
				PetLeveling
					.getPetLevelingForPet(petId, rarity)
					.getPetLevel(xp)
					.getCurrentLevel() : null,
			gui
		);
	}

	public void renderPetInformation(int currentLevel, Integer upgradedLevel, ChestScreen gui) {
		FontRenderer font = Minecraft.getInstance().font;
		String currentText = "Current pet level: " + currentLevel;
		int currentWidth = font.getStringWidth(currentText);
		String upgradedText = "Upgraded pet level: " + upgradedLevel;
		int upgradedWidth = font.getStringWidth(upgradedText);
		int left = ((AccessorContainerScreen) gui).getGuiLeft() - 30 - (upgradedLevel == null ? Math.max(
			upgradedWidth,
			currentWidth
		) : currentWidth);
		com.mojang.blaze3d.systems.RenderSystem.disableLighting();
		com.mojang.blaze3d.systems.RenderSystem.color(1F, 1F, 1F, 1F);
		Utils.drawStringScaled(currentText, left, ((AccessorContainerScreen) gui).getGuiTop() + 25, false, 0xFFD700, 1F);
		if (upgradedLevel != null)
			Utils.drawStringScaled(
				upgradedText,
				left,
				((AccessorContainerScreen) gui).getGuiTop() + 45,
				false,
				0xFFD700,
				1F
			);
	}



}
