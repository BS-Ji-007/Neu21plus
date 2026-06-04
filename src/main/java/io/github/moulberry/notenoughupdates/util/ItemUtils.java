/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.listener.ItemTooltipListener;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

public class ItemUtils {
	private static final Gson smallPrintingGson = new Gson();

	public static ItemStack createSkullItemStack(String displayName, String uuid, String skinAbsoluteUrl) {
		JsonObject object = new JsonObject();
		JsonObject textures = new JsonObject();
		JsonObject skin = new JsonObject();
		skin.addProperty("url", skinAbsoluteUrl);
		textures.add("SKIN", skin);
		object.add("textures", textures);
		String json = smallPrintingGson.toJson(object);
		return Utils.createSkull(
			displayName,
			uuid,
			Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8))
		);
	}

	public static ItemStack getCoinItemStack(double coinAmount) {
		String uuid = "2070f6cb-f5db-367a-acd0-64d39a7e5d1b";
		String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0=";
		ItemStack skull = Utils.createSkull("§r§6" + StringUtils.formatNumber(coinAmount) + " Coins", uuid, texture);
		CompoundTag extraAttributes = getExtraAttributes(skull);
		extraAttributes.putString("id", "SKYBLOCK_COIN");
		return skull;
	}

	public static CompoundTag getOrCreateTag(ItemStack is) {
		return is.getOrCreateTag();
	}

	public static void appendLore(ItemStack is, List<String> moreLore) {
		CompoundTag display = is.getOrCreateTagElement("display");
		ListTag lore = display.getList("Lore", 8);
		for (String s : moreLore) {
			lore.add(StringTag.valueOf(s));
		}
		display.put("Lore", lore);
	}

	public static void setLore(ItemStack is, List<String> newLore) {
		CompoundTag display = is.getOrCreateTagElement("display");
		ListTag lore = new ListTag();
		for (String s : newLore) {
			lore.add(StringTag.valueOf(s));
		}
		display.put("Lore", lore);
	}

	public static @NotNull List<@NotNull String> getLore(@Nullable ItemStack is) {
		if (is == null || !is.hasTag()) return new ArrayList<>();
		return getLore(is.getTag());
	}

	public static @NotNull List<@NotNull String> getLore(@Nullable CompoundTag tagCompound) {
		if (tagCompound == null || !tagCompound.contains("display", 10)) {
			return Collections.emptyList();
		}
		ListTag tagList = tagCompound.getCompound("display").getList("Lore", 8);
		List<String> list = new ArrayList<>();
		for (int i = 0; i < tagList.size(); i++) {
			list.add(tagList.getString(i));
		}
		return list;
	}

	public static @Nullable String getDisplayName(@Nullable ItemStack itemStack) {
		if (itemStack == null) return null;
		return itemStack.getHoverName().getString();
	}

	public static @NotNull CompoundTag getExtraAttributes(ItemStack itemStack) {
		return itemStack.getOrCreateTagElement("ExtraAttributes");
	}

	public static boolean isSoulbound(ItemStack item) {
		return getLore(item).stream()
				.anyMatch(line -> line.contains("Soulbound"));
	}

	public static ItemStack createItemStackFromId(String id, String displayname) {
		// Modern identifier approach
		return ItemStack.EMPTY; 
	}
}
