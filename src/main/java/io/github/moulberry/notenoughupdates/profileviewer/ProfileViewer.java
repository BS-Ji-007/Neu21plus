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

package io.github.moulberry.notenoughupdates.profileviewer;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.UrsaClient;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.Getter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ProfileViewer {

	@Getter
	private static final LinkedHashMap<String, ItemStack> skillToSkillDisplayMap =
		new LinkedHashMap<String, ItemStack>() {
			{
				put("taming", Utils.createItemStack(Items.spawn_egg, ChatFormatting.LIGHT_PURPLE + "Taming"));
				put("mining", Utils.createItemStack(Items.stone_pickaxe, ChatFormatting.GRAY + "Mining"));
				put(
					"foraging",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.sapling), ChatFormatting.DARK_GREEN + "Foraging")
				);
				put(
					"enchanting",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.enchanting_table), ChatFormatting.GREEN + "Enchanting")
				);
				put(
					"carpentry",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.crafting_table), ChatFormatting.DARK_RED + "Carpentry")
				);
				put("farming", Utils.createItemStack(Items.golden_hoe, ChatFormatting.YELLOW + "Farming"));
				put("combat", Utils.createItemStack(Items.stone_sword, ChatFormatting.RED + "Combat"));
				put("fishing", Utils.createItemStack(Items.fishing_rod, ChatFormatting.AQUA + "Fishing"));
				put("alchemy", Utils.createItemStack(Items.brewing_stand, ChatFormatting.BLUE + "Alchemy"));
				put("runecrafting", Utils.createItemStack(Items.magma_cream, ChatFormatting.DARK_PURPLE + "Runecrafting"));
				put("social", Utils.createItemStack(Items.EMERALD, ChatFormatting.DARK_GREEN + "Social"));
				put("zombie", Utils.createItemStack(Items.rotten_flesh, ChatFormatting.GOLD + "Rev Slayer"));
				put("spider", Utils.createItemStack(Items.spider_eye, ChatFormatting.GOLD + "Tara Slayer"));
				put("wolf", Utils.createItemStack(Items.bone, ChatFormatting.GOLD + "Sven Slayer"));
				put("enderman", Utils.createItemStack(Items.ender_pearl, ChatFormatting.GOLD + "Ender Slayer"));
				put("blaze", Utils.createItemStack(Items.blaze_rod, ChatFormatting.GOLD + "Blaze Slayer"));
				put("vampire", Utils.createItemStack(Items.redstone, ChatFormatting.GOLD + "Vampire Slayer"));
			}
		};
	private static final ItemStack CAT_FARMING = Utils.createItemStack(
		Items.golden_hoe,
		ChatFormatting.YELLOW + "Farming"
	);
	private static final ItemStack CAT_MINING = Utils.createItemStack(
		Items.stone_pickaxe,
		ChatFormatting.GRAY + "Mining"
	);
	private static final ItemStack CAT_COMBAT = Utils.createItemStack(
		Items.stone_sword,
		ChatFormatting.RED + "Combat"
	);
	private static final ItemStack CAT_FORAGING = Utils.createItemStack(
		Item.getItemFromBlock(Blocks.sapling),
		ChatFormatting.DARK_GREEN + "Foraging"
	);
	private static final ItemStack CAT_FISHING = Utils.createItemStack(
		Items.fishing_rod,
		ChatFormatting.AQUA + "Fishing"
	);
	private static final ItemStack CAT_RIFT = Utils.createItemStack(
		Blocks.mycelium,
		ChatFormatting.DARK_PURPLE + "Rift"
	);
	@Getter
	private static final LinkedHashMap<ItemStack, List<String>> collectionCatToCollectionMap =
		new LinkedHashMap<ItemStack, List<String>>() {
			{
				put(
					CAT_FARMING,
					Utils.createList(
						"WHEAT",
						"CARROT_ITEM",
						"POTATO_ITEM",
						"PUMPKIN",
						"MELON",
						"SEEDS",
						"MUSHROOM_COLLECTION",
						"INK_SACK:3",
						"CACTUS",
						"SUGAR_CANE",
						"FEATHER",
						"LEATHER",
						"PORK",
						"RAW_CHICKEN",
						"MUTTON",
						"RABBIT",
						"NETHER_STALK"
					)
				);
				put(
					CAT_MINING,
					Utils.createList(
						"COBBLESTONE",
						"COAL",
						"IRON_INGOT",
						"GOLD_INGOT",
						"DIAMOND",
						"INK_SACK:4",
						"EMERALD",
						"REDSTONE",
						"QUARTZ",
						"OBSIDIAN",
						"GLOWSTONE_DUST",
						"GRAVEL",
						"ICE",
						"NETHERRACK",
						"SAND",
						"ENDER_STONE",
						null,
						"MITHRIL_ORE",
						"HARD_STONE",
						"GEMSTONE_COLLECTION",
						"MYCEL",
						"SAND:1",
						"SULPHUR_ORE",
						"GLACITE",
						"TUNGSTEN",
						"UMBER"
					)
				);
				put(
					CAT_COMBAT,
					Utils.createList(
						"ROTTEN_FLESH",
						"BONE",
						"STRING",
						"SPIDER_EYE",
						"SULPHUR",
						"ENDER_PEARL",
						"GHAST_TEAR",
						"SLIME_BALL",
						"BLAZE_ROD",
						"MAGMA_CREAM",
						null,
						null,
						null,
						null,
						"CHILI_PEPPER"
					)
				);
				put(CAT_FORAGING, Utils.createList("LOG", "LOG:1", "LOG:2", "LOG_2:1", "LOG_2", "LOG:3", null));
				put(
					CAT_FISHING,
					Utils.createList(
						"RAW_FISH",
						"RAW_FISH:1",
						"RAW_FISH:2",
						"RAW_FISH:3",
						"PRISMARINE_SHARD",
						"PRISMARINE_CRYSTALS",
						"CLAY_BALL",
						"WATER_LILY",
						"INK_SACK",
						"SPONGE",
						"MAGMA_FISH"
					)
				);
				put(CAT_RIFT,
					Utils.createList(
						"AGARICUS_CAP",
						"CADUCOUS_STEM",
						"HALF_EATEN_CARROT",
						"HEMOVIBE",
						"METAL_HEART",
						"WILTED_BERBERIS",
						"TIMITE"
					)
				);
			}
		};
	@Getter
	private static final LinkedHashMap<ItemStack, List<String>> collectionCatToMinionMap =
		new LinkedHashMap<ItemStack, List<String>>() {
			{
				put(
					CAT_FARMING,
					Utils.createList(
						"WHEAT",
						"CARROT",
						"POTATO",
						"PUMPKIN",
						"MELON",
						null,
						"MUSHROOM",
						"COCOA",
						"CACTUS",
						"SUGAR_CANE",
						"CHICKEN",
						"COW",
						"PIG",
						null,
						"SHEEP",
						"RABBIT",
						"NETHER_WARTS"
					)
				);
				put(
					CAT_MINING,
					Utils.createList(
						"COBBLESTONE",
						"COAL",
						"IRON",
						"GOLD",
						"DIAMOND",
						"LAPIS",
						"EMERALD",
						"REDSTONE",
						"QUARTZ",
						"OBSIDIAN",
						"GLOWSTONE",
						"GRAVEL",
						"ICE",
						null,
						"SAND",
						"ENDER_STONE",
						"SNOW",
						"MITHRIL",
						"HARD_STONE",
						null,
						"MYCELIUM",
						"RED_SAND",
						null,
						null,
						null,
						null
					)
				);
				put(
					CAT_COMBAT,
					Utils.createList(
						"ZOMBIE",
						"SKELETON",
						"SPIDER",
						"CAVESPIDER",
						"CREEPER",
						"ENDERMAN",
						"GHAST",
						"SLIME",
						"BLAZE",
						"MAGMA_CUBE",
						"REVENANT",
						"TARANTULA",
						"VOIDLING",
						"INFERNO"
					)
				);
				put(CAT_FORAGING, Utils.createList("OAK", "SPRUCE", "BIRCH", "DARK_OAK", "ACACIA", "JUNGLE", "FLOWER"));
				put(CAT_FISHING, Utils.createList("FISHING", null, null, null, null, null, "CLAY", null, null, null));
				put(CAT_RIFT, Utils.createList(null, null, null, "VAMPIRE"));
			}
		};
	@Getter
	private static final LinkedHashMap<String, ItemStack> collectionToCollectionDisplayMap =
		new LinkedHashMap<String, ItemStack>() {
			{
				/* FARMING COLLECTIONS */
				put("WHEAT", Utils.createItemStack(Items.wheat, ChatFormatting.YELLOW + "Wheat"));
				put("CARROT_ITEM", Utils.createItemStack(Items.carrot, ChatFormatting.YELLOW + "Carrot"));
				put("POTATO_ITEM", Utils.createItemStack(Items.potato, ChatFormatting.YELLOW + "Potato"));
				put(
					"PUMPKIN",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.pumpkin), ChatFormatting.YELLOW + "Pumpkin")
				);
				put("MELON", Utils.createItemStack(Items.melon, ChatFormatting.YELLOW + "Melon"));
				put("SEEDS", Utils.createItemStack(Items.wheat_seeds, ChatFormatting.YELLOW + "Seeds"));
				put(
					"MUSHROOM_COLLECTION",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.red_mushroom), ChatFormatting.YELLOW + "Mushroom")
				);
				put("INK_SACK:3", Utils.createItemStack(Items.dye, ChatFormatting.YELLOW + "Cocoa Beans", 3));
				put(
					"CACTUS",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.cactus), ChatFormatting.YELLOW + "Cactus")
				);
				put("SUGAR_CANE", Utils.createItemStack(Items.reeds, ChatFormatting.YELLOW + "Sugar Cane"));
				put("FEATHER", Utils.createItemStack(Items.feather, ChatFormatting.YELLOW + "Feather"));
				put("LEATHER", Utils.createItemStack(Items.leather, ChatFormatting.YELLOW + "Leather"));
				put("PORK", Utils.createItemStack(Items.porkchop, ChatFormatting.YELLOW + "Raw Porkchop"));
				put("RAW_CHICKEN", Utils.createItemStack(Items.chicken, ChatFormatting.YELLOW + "Raw Chicken"));
				put("MUTTON", Utils.createItemStack(Items.mutton, ChatFormatting.YELLOW + "Mutton"));
				put("RABBIT", Utils.createItemStack(Items.rabbit, ChatFormatting.YELLOW + "Raw Rabbit"));
				put("NETHER_STALK", Utils.createItemStack(Items.nether_wart, ChatFormatting.YELLOW + "Nether Wart"));

				/* MINING COLLECTIONS */
				put(
					"COBBLESTONE",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.cobblestone), ChatFormatting.GRAY + "Cobblestone")
				);
				put("COAL", Utils.createItemStack(Items.coal, ChatFormatting.GRAY + "Coal"));
				put("IRON_INGOT", Utils.createItemStack(Items.iron_ingot, ChatFormatting.GRAY + "Iron Ingot"));
				put("GOLD_INGOT", Utils.createItemStack(Items.gold_ingot, ChatFormatting.GRAY + "Gold Ingot"));
				put("DIAMOND", Utils.createItemStack(Items.diamond, ChatFormatting.GRAY + "Diamond"));
				put("INK_SACK:4", Utils.createItemStack(Items.dye, ChatFormatting.GRAY + "Lapis Lazuli", 4));
				put("EMERALD", Utils.createItemStack(Items.EMERALD, ChatFormatting.GRAY + "Emerald"));
				put("REDSTONE", Utils.createItemStack(Items.redstone, ChatFormatting.GRAY + "Redstone"));
				put("QUARTZ", Utils.createItemStack(Items.quartz, ChatFormatting.GRAY + "Nether Quartz"));
				put(
					"OBSIDIAN",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.obsidian), ChatFormatting.GRAY + "Obsidian")
				);
				put("GLOWSTONE_DUST", Utils.createItemStack(Items.glowstone_dust, ChatFormatting.GRAY + "Glowstone Dust"));
				put("GRAVEL", Utils.createItemStack(Item.getItemFromBlock(Blocks.gravel), ChatFormatting.GRAY + "Gravel"));
				put("ICE", Utils.createItemStack(Item.getItemFromBlock(Blocks.ice), ChatFormatting.GRAY + "Ice"));
				put(
					"NETHERRACK",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.netherrack), ChatFormatting.GRAY + "Netherrack")
				);
				put("SAND", Utils.createItemStack(Item.getItemFromBlock(Blocks.sand), ChatFormatting.GRAY + "Sand"));
				put(
					"ENDER_STONE",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.END_STONE), ChatFormatting.GRAY + "End Stone")
				);
				put("MITHRIL_ORE", Utils.createItemStack(Items.prismarine_crystals, ChatFormatting.GRAY + "Mithril"));
				put(
					"HARD_STONE",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.stone), ChatFormatting.GRAY + "Hard Stone")
				);
				put(
					"GEMSTONE_COLLECTION",
					Utils.createSkull(
						ChatFormatting.GRAY + "Gemstone",
						"e942eb66-a350-38e5-aafa-0dfc3e17b4ac",
						"ewogICJ0aW1lc3RhbXAiIDogMTYxODA4Mzg4ODc3MSwKICAicHJvZmlsZUlkIiA6ICJjNTBhZmE4YWJlYjk0ZTQ1OTRiZjFiNDI1YTk4MGYwMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUd29FQmFlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FhYzE1ZjZmY2YyY2U5NjNlZjRjYTcxZjFhODY4NWFkYjk3ZWI3NjllMWQxMTE5NGNiYmQyZTk2NGE4ODk3OGMiCiAgICB9CiAgfQp9"
					)
				);
				put(
					"MYCEL",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.mycelium), ChatFormatting.GRAY + "Mycelium")
				);
				put(
					"SAND:1",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.sand), ChatFormatting.GRAY + "Red Sand", 1)
				);
				put("SULPHUR_ORE", Utils.createItemStack(Items.glowstone_dust, ChatFormatting.GRAY + "Sulphur"));
				put("GLACITE", Utils.createItemStack(Blocks.packed_ice, ChatFormatting.GRAY + "Glacite"));
				put(
					"TUNGSTEN",
					Utils.createSkull(
						ChatFormatting.GRAY + "Tungsten",
						"b0ac8270-ac1b-325c-9e2c-e679e0f48684",
						"ewogICJ0aW1lc3RhbXAiIDogMTcwODkxNDY4MjE4MCwKICAicHJvZmlsZUlkIiA6ICJiZDNhNWRmY2ZkZjg0NDczOTViZDJiZmUwNGY0YzAzMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJwcmVja3Jhc25vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q4MTFmM2U3MjNiYmQ0NjM5M2Y4YWFkODU1NmIxZGY4ZWQzM2Y1NTliZTgyN2Y0N2ZlNzM2ZjcwNGMzNTU4NmUiCiAgICB9CiAgfQp9"
					)
				);
				put(
					"UMBER",
					Utils.createSkull(
						ChatFormatting.GRAY + "Umber",
						"bfb54c8e-93b8-3196-841c-5c8892d59f10",
						"ewogICJ0aW1lc3RhbXAiIDogMTcwODkxOTkzMjc2MSwKICAicHJvZmlsZUlkIiA6ICJiMmM5ZTUyZjNhODU0YzQ4YmJjMmMwM2JjM2U0NzdiMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJJU05FRVpFREFUWU9VIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I1NjViNWFhODNkNGFhN2Y3YWYyMmRjMTI3MWIyZjBiMjc0NDFmOWFjMTQ5NWY2YjQ2NTNjZjY4ZGZiMTA1ZWYiCiAgICB9CiAgfQp9"
					)
				);

				/* COMBAT COLLECTIONS */
				put("ROTTEN_FLESH", Utils.createItemStack(Items.rotten_flesh, ChatFormatting.RED + "Rotten Flesh"));
				put("BONE", Utils.createItemStack(Items.bone, ChatFormatting.RED + "Bone"));
				put("STRING", Utils.createItemStack(Items.string, ChatFormatting.RED + "String"));
				put("SPIDER_EYE", Utils.createItemStack(Items.spider_eye, ChatFormatting.RED + "Spider Eye"));
				put("SULPHUR", Utils.createItemStack(Items.gunpowder, ChatFormatting.RED + "Gunpowder"));
				put("ENDER_PEARL", Utils.createItemStack(Items.ender_pearl, ChatFormatting.RED + "Ender Pearl"));
				put("GHAST_TEAR", Utils.createItemStack(Items.ghast_tear, ChatFormatting.RED + "Ghast Tear"));
				put("SLIME_BALL", Utils.createItemStack(Items.slime_ball, ChatFormatting.RED + "Slimeball"));
				put("BLAZE_ROD", Utils.createItemStack(Items.blaze_rod, ChatFormatting.RED + "Blaze Rod"));
				put("MAGMA_CREAM", Utils.createItemStack(Items.magma_cream, ChatFormatting.RED + "Magma Cream"));
				put(
					"CHILI_PEPPER",
					Utils.createSkull(
						ChatFormatting.RED + "Chili Pepper",
						"3d47abaa-b40b-3826-b20c-d83a7f053bd9",
						"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg1OWM4ZGYxMTA5YzA4YTc1NjI3NWYxZDI4ODdjMjc0ODA0OWZlMzM4Nzc3NjlhN2I0MTVkNTZlZGE0NjlkOCJ9fX0"
					)
				);

				/* FORAGING COLLECTIONS */
				put(
					"LOG",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log), ChatFormatting.DARK_GREEN + "Oak Wood")
				);
				put(
					"LOG:1",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log), ChatFormatting.DARK_GREEN + "Spruce Wood", 1)
				);
				put(
					"LOG:2",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log), ChatFormatting.DARK_GREEN + "Birch Wood", 2)
				);
				put(
					"LOG_2:1",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log2), ChatFormatting.DARK_GREEN + "Dark Oak Wood", 1)
				);
				put(
					"LOG_2",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log2), ChatFormatting.DARK_GREEN + "Acacia Wood")
				);
				put(
					"LOG:3",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log), ChatFormatting.DARK_GREEN + "Jungle Wood", 3)
				);

				/* FISHING COLLECTIONS */
				put("RAW_FISH", Utils.createItemStack(Items.fish, ChatFormatting.AQUA + "Raw Fish"));
				put("RAW_FISH:1", Utils.createItemStack(Items.fish, ChatFormatting.AQUA + "Raw Salmon", 1));
				put("RAW_FISH:2", Utils.createItemStack(Items.fish, ChatFormatting.AQUA + "Clownfish", 2));
				put("RAW_FISH:3", Utils.createItemStack(Items.fish, ChatFormatting.AQUA + "Pufferfish", 3));
				put(
					"PRISMARINE_SHARD",
					Utils.createItemStack(Items.prismarine_shard, ChatFormatting.AQUA + "Prismarine Shard")
				);
				put(
					"PRISMARINE_CRYSTALS",
					Utils.createItemStack(Items.prismarine_crystals, ChatFormatting.AQUA + "Prismarine Crystals")
				);
				put("CLAY_BALL", Utils.createItemStack(Items.clay_ball, ChatFormatting.AQUA + "Clay"));
				put(
					"WATER_LILY",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.waterlily), ChatFormatting.AQUA + "Lily Pad")
				);
				put("INK_SACK", Utils.createItemStack(Items.dye, ChatFormatting.AQUA + "Ink Sac"));
				put("SPONGE", Utils.createItemStack(Item.getItemFromBlock(Blocks.sponge), ChatFormatting.AQUA + "Sponge"));
				put(
					"MAGMA_FISH",
					Utils.createSkull(
						ChatFormatting.AQUA + "Magmafish",
						"5c53195c-5b98-3476-9731-c32647b22723",
						"ewogICJ0aW1lc3RhbXAiIDogMTY0MjQ4ODA3MDY2NiwKICAicHJvZmlsZUlkIiA6ICIzNDkxZjJiOTdjMDE0MWE2OTM2YjFjMjJhMmEwMGZiNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZXNzc3N1aGgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU2YjU5NTViMjk1NTIyYzk2ODk0ODE5NjBjMDFhOTkyY2ExYzc3NTRjZjRlZTMxM2M4ZGQwYzM1NmQzMzVmIgogICAgfQogIH0KfQ"
					)
				);
				/* RIFT COLLECTIONS */
				put("AGARICUS_CAP",
					Utils.createSkull(
						ChatFormatting.DARK_PURPLE + "Agaricus Cap",
						"43e884b2-633e-3c87-b601-62d18c11683f",
						"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlMGEyMzBhY2Q2NDM2YWJjODZmMTNiZTcyZTliYTk0NTM3ZWU1NGYwMzI1YmI4NjI1NzdhMWUwNjJmMzcifX19"
					));
				put("CADUCOUS_STEM", Utils.createItemStack(Item.getItemFromBlock(Blocks.double_plant),
					ChatFormatting.DARK_PURPLE + "Caducous Stem", 4));
				put("HALF_EATEN_CARROT", Utils.createItemStack(Items.carrot, ChatFormatting.DARK_PURPLE + "Half-Eaten Carrot"));
				put("HEMOVIBE", Utils.createItemStack(Item.getItemFromBlock(Blocks.redstone_ore), ChatFormatting.DARK_PURPLE + "Hemovibe"));
				put("METAL_HEART",
					Utils.createSkull(
						ChatFormatting.DARK_PURPLE + "Living Metal Heart",
						"c678b8cc-c130-31d1-baf3-14660f8ef742",
						"ewogICJ0aW1lc3RhbXAiIDogMTY3NjQ3NjQ1NjcyNywKICAicHJvZmlsZUlkIiA6ICI5MGQ1NDY0OGEzNWE0YmExYTI2Yjg1YTg4NTU4OGJlOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJFdW4wbWlhIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2YwMjc4ZWU1M2E1M2I3NzMzYzdiODQ1MmZjZjc5NGRmYmZiYzNiMDMyZTc1MGE2OTkzNTczYjViZDAyOTkxMzUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ"
					));
				put("WILTED_BERBERIS", Utils.createItemStack(Item.getItemFromBlock(Blocks.deadbush), ChatFormatting.DARK_PURPLE + "Wilted Berberis"));
				put("TIMITE",
					Utils.createSkull(
						ChatFormatting.DARK_PURPLE + "Timite",
						"268a3d23-43c7-3f4e-9c25-14c314e7126b",
						"ewogICJ0aW1lc3RhbXAiIDogMTcyNTMyNTgwNDE0MywKICAicHJvZmlsZUlkIiA6ICIxMTM1Njg1ZTk3ZGE0ZjYyYTliNDQ3MzA0NGFiZjQ0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXJpb1dsZXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDIxNzdhYzk5N2ZmOTcwOTAwY2QxNzkyN2ZiMzFjNGUyMGZhYzI2MzY0OWIxYjU1ODU1NWJjYzZmMGQ1YjVkZCIKICAgIH0KICB9Cn0="
					));
			}
		};
	private static final AtomicBoolean updatingResourceCollection = new AtomicBoolean(false);
	private static JsonObject resourceCollection = null;
	@Getter
	private final HashMap<String, JsonObject> uuidToHypixelProfile = new HashMap<>();
	@Getter
	private final NEUManager manager;
	private final HashMap<String, SkyblockProfiles> uuidToSkyblockProfiles = new HashMap<>();
	final HashMap<String, String> nameToUuid = new HashMap<>();

	public ProfileViewer(NEUManager manager) {
		this.manager = manager;
	}

	public static JsonObject getOrLoadCollectionsResource() {
		if (resourceCollection != null) return resourceCollection;
		if (updatingResourceCollection.get()) return null;

		updatingResourceCollection.set(true);

		NotEnoughUpdates.INSTANCE.manager.apiUtils
			.newAnonymousHypixelApiRequest("resources/skyblock/collections")
			.requestJson()
			.thenAccept(jsonObject -> {
				updatingResourceCollection.set(false);
				if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
					resourceCollection = jsonObject.get("collections").getAsJsonObject();
				}
			});
		return null;
	}

	public void putNameUuid(String name, String uuid) {
		nameToUuid.put(name, uuid);
	}

	public void getPlayerUUID(String name, Consumer<String> uuidCallback) {
		String nameLower = name.toLowerCase(Locale.ROOT);
		if (nameToUuid.containsKey(nameLower)) {
			uuidCallback.accept(nameToUuid.get(nameLower));
			return;
		}

		manager.apiUtils
			.request()
			.url("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + nameLower)
			.requestJson()
			.thenAccept(jsonObject -> {
				if (jsonObject.has("id") && jsonObject.get("id").isJsonPrimitive() &&
					jsonObject.get("id").getAsJsonPrimitive().isString()) {
					String uuid = jsonObject.get("id").getAsString();
					nameToUuid.put(nameLower, uuid);
					uuidCallback.accept(uuid);
					return;
				}
				uuidCallback.accept(null);
			});
	}

	public void loadPlayerByName(String name, Consumer<SkyblockProfiles> callback) {
		String nameLower = name.toLowerCase(Locale.ROOT);

		if (nameToUuid.containsKey(nameLower) && nameToUuid.get(nameLower) == null) {
			callback.accept(null);
			return;
		}

		getPlayerUUID(
			nameLower,
			uuid -> {
				nameToUuid.put(nameLower, uuid);
				if (uuid == null) {
					callback.accept(null);
				} else {
					if (!uuidToHypixelProfile.containsKey(uuid)) {
						manager.ursaClient
							.get(UrsaClient.player(Utils.parseDashlessUUID(uuid)))
							.thenAccept(playerJson -> {
									if (
										playerJson != null &&
											playerJson.has("success") &&
											playerJson.get("success").getAsBoolean() &&
											playerJson.get("player").isJsonObject()
									) {
										uuidToHypixelProfile.put(uuid, playerJson.get("player").getAsJsonObject());
									}
								}
							);
					}

					callback.accept(getOrLoadSkyblockProfiles(uuid, ignored -> {}));
				}
			}
		);
	}

	public SkyblockProfiles getOrLoadSkyblockProfiles(String uuid, Consumer<SkyblockProfiles> callback) {
		if (uuidToSkyblockProfiles.containsKey(uuid)) {
			uuidToSkyblockProfiles.get(uuid).resetCache();
		}

		SkyblockProfiles profile = uuidToSkyblockProfiles.computeIfAbsent(uuid, key -> new SkyblockProfiles(this, uuid));
		if (profile.getNameToProfile() != null) {
			callback.accept(profile);
		} else {
			profile.getOrLoadSkyblockProfiles(() -> callback.accept(profile));
		}
		return profile;
	}

	public static class Level {

		public float level = 0;
		public float maxXpForLevel = 0;
		public boolean maxed = false;
		public int maxLevel;
		public float totalXp;
	}
}
