/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.auction.APIManager;
import io.github.moulberry.notenoughupdates.core.config.KeybindHelper;
import io.github.moulberry.notenoughupdates.miscfeatures.inventory.MuseumTooltipManager;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.hypixelapi.HypixelItemAPI;
import lombok.val;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.platform.InputConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ItemPriceInformation {
	private static File file;
	private static HashSet<String> auctionableItems;
	private static Gson gson;
	private static final NumberFormat format = new DecimalFormat("#,##0.#", new DecimalFormatSymbols(Locale.US));
	public static String STACKSIZE_OVERRIDE = "NEU_STACKSIZE_OVERRIDE";

	private static final Pattern SACK_STORED_AMOUNT = Pattern.compile(
		".*Stored: §.(?<amount>[\\d,]+)§.\\/.*");

	private static final Pattern GEMSTONE_STORED_AMOUNT = Pattern.compile(
		".*Amount: §.(?<amount>[\\d,]+)");

	private static final Pattern COMPOSTER_STORED_AMOUNT = Pattern.compile(
		".*Compost Available: §.(?<amount>[\\d,]+)");

	private static final Pattern BAZAAR_STORED_AMOUNT = Pattern.compile(
		".*(?:Offer|Order) amount: §.(?<amount>[\\d,]+)§.x");

	public static void addToTooltip(List<String> tooltip, String internalName, ItemStack stack) {
		addToTooltip(tooltip, internalName, stack, true);
	}

	public static void init(File saveLocation, Gson neuGson) {
		file = saveLocation;
		gson = neuGson;
		if (file.exists()) {
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file),
					StandardCharsets.UTF_8
				))
			) {
				auctionableItems = gson.fromJson(reader, HashSet.class);
			} catch (Exception ignored) {
			}
		}
	}

	public static void updateAuctionableItemsList() {
		Set<String> items = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfoKeySet();
		if (!items.isEmpty()) {
			auctionableItems = (HashSet<String>) items;
			try (
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file),
					StandardCharsets.UTF_8
				))
			) {
				//noinspection ResultOfMethodCallIgnored
				file.createNewFile();
				writer.write(gson.toJson(items));
			} catch (IOException ignored) {
			}
		}
	}

	public static void addToTooltip(List<String> tooltip, String internalname, ItemStack stack, boolean useStackSize) {
		if (stack.getTag().hasKey("disableNeuTooltip") && stack.getTag().getBoolean("disableNeuTooltip")) {
			return;
		}
		if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.disablePriceKey &&
			!KeybindHelper.isKeyDown(NotEnoughUpdates.INSTANCE.config.tooltipTweaks.disablePriceKeyKeybind)) {
			return;
		}
		if (internalname.equals("SKYBLOCK_MENU")) {
			return;
		}
		JsonObject auctionInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfo(internalname);
		JsonObject bazaarInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(internalname);
		double lowestBinAvg = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAvgBin(internalname);

		double lowestBin = NotEnoughUpdates.INSTANCE.manager.auctionManager.getLowestBin(internalname);
		APIManager.CraftInfo craftCost = NotEnoughUpdates.INSTANCE.manager.auctionManager.getCraftCost(internalname);
		boolean bazaarItem = bazaarInfo != null;

		boolean auctionItem = !bazaarItem;
		boolean auctionInfoErrored = auctionInfo == null && lowestBin < 0;
		if (auctionItem) {
			long currentTime = System.currentTimeMillis();
			long lastUpdate = NotEnoughUpdates.INSTANCE.manager.auctionManager.getLastLowestBinUpdateTime();
			//check if info is older than 10 minutes
			if (currentTime - lastUpdate > 600 * 1000 && NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
				tooltip.add(ChatFormatting.RED + "[NEU] Price info is outdated.");
				tooltip.add(ChatFormatting.RED + "It will be updated again as soon as possible.");
			}
		}

		int shiftStackMultiplier = useStackSize && stack.getCount() > 1 ? stack.getCount() : stack.getItem().getItemStackLimit(stack);
		if (stack.getTag() != null && stack.getTag().hasKey(STACKSIZE_OVERRIDE)) {
			shiftStackMultiplier = stack.getTag().getInteger(STACKSIZE_OVERRIDE);
		}
		int stackMultiplier = 1;
		boolean foundMulti = false;
		for (int i = 1; i < tooltip.size(); i++) {
			if (foundMulti) break;
			for (Pattern pattern : new Pattern[]{SACK_STORED_AMOUNT, GEMSTONE_STORED_AMOUNT, COMPOSTER_STORED_AMOUNT, BAZAAR_STORED_AMOUNT}) {
				val matcher = pattern.matcher(tooltip.get(i));
				if (matcher.matches()) {
					String amountString = matcher.group("amount").replace(",", "");
					try {
						int parsedValue = Integer.parseInt(amountString);
						if (parsedValue != 0) {
							shiftStackMultiplier = parsedValue;
							foundMulti = true;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		}
		boolean shiftPressed = InputConstants.isKeyDown(Keyboard.KEY_LSHIFT);
		if (shiftPressed) {
			stackMultiplier = shiftStackMultiplier;
		}
		boolean added = false;
		if (bazaarItem) {
			List<Integer> lines = NotEnoughUpdates.INSTANCE.config.tooltipTweaks.priceInfoBaz;
			//values = {"", "Buy", "Sell", "Buy (Insta)", "Sell (Insta)", "Raw Craft Cost", "Instabuys (Hourly)", "Instasells (Hourly)", "Instabuys (Daily)", "Instasells (Daily)", "Instabuys (Weekly)", "Instasells (Weekly)"}
			for (int lineId : lines) {
				switch (lineId) {
					case 0:
						if (bazaarInfo.has("avg_buy")) {
							if (!added) {
								tooltip.add("");
								if (!shiftPressed)
									tooltip.add(ChatFormatting.DARK_GRAY + "[SHIFT show x" + shiftStackMultiplier + "]");
								added = true;
							}
							double bazaarBuyPrice = bazaarInfo.get("avg_buy").getAsFloat() * stackMultiplier;
							tooltip.add(formatPrice("Bazaar Buy: ", bazaarBuyPrice));
						}
						break;
					case 1:
						if (bazaarInfo.has("avg_sell")) {
							if (!added) {
								tooltip.add("");
								if (!shiftPressed)
									tooltip.add(ChatFormatting.DARK_GRAY + "[SHIFT show x" + shiftStackMultiplier + "]");
								added = true;
							}
							double bazaarSellPrice = bazaarInfo.get("avg_sell").getAsDouble() * stackMultiplier;
							tooltip.add(formatPrice("Bazaar Sell: ", bazaarSellPrice));
						}
						break;
					case 2:
						if (bazaarInfo.has("curr_buy")) {
							if (!added) {
								tooltip.add("");
								if (!shiftPressed)
									tooltip.add(ChatFormatting.DARK_GRAY + "[SHIFT show x" + shiftStackMultiplier + "]");
								added = true;
							}
							double bazaarInstantBuyPrice = bazaarInfo.get("curr_buy").getAsFloat() * stackMultiplier;
							tooltip.add(formatPrice("Bazaar Insta-Buy: ", bazaarInstantBuyPrice));
						}
						break;
					case 3:
						if (bazaarInfo.has("curr_sell")) {
							if (!added) {
								tooltip.add("");
								if (!shiftPressed)
									tooltip.add(ChatFormatting.DARK_GRAY + "[SHIFT show x" + shiftStackMultiplier + "]");
								added = true;
							}
							double bazaarInstantSellPrice = bazaarInfo.get("curr_sell").getAsFloat() * stackMultiplier;
							tooltip.add(formatPrice("Bazaar Insta-Sell: ", bazaarInstantSellPrice));
						}
						break;
					case 4:
						if (craftCost != null && craftCost.fromRecipe) {
							if (craftCost.craftCost == 0) {
								continue;
							}
							if (!added) {
								tooltip.add("");
								added = true;
							}
							double cost = craftCost.craftCost;
							if (shiftPressed) cost = cost * shiftStackMultiplier;
							tooltip.add(formatPrice("Raw Craft Cost: ", cost));
						}
						break;
					case 5:
						if (bazaarInfo.has("instabuys_hourly")) {
							if (!added) {
								tooltip.add("");
								added = true;
							}

							tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD +
								"Insta-Buys (Hourly): " +
								ChatFormatting.GOLD + ChatFormatting.BOLD +
								format.format(bazaarInfo.get("instabuys_hourly").getAsFloat()));
						}
						break;
					case 6:
						if (bazaarInfo.has("instasells_hourly")) {
							if (!added) {
								tooltip.add("");
								added = true;
							}

							tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD +
								"Insta-Sells (Hourly): " +
								ChatFormatting.GOLD + ChatFormatting.BOLD +
								format.format(bazaarInfo.get("instasells_hourly").getAsFloat()));
						}
						break;
					case 7:
						if (bazaarInfo.has("instabuys_daily")) {
							if (!added) {
								tooltip.add("");
								added = true;
							}

							tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD +
								"Insta-Buys (Daily): " +
								ChatFormatting.GOLD + ChatFormatting.BOLD +
								format.format(bazaarInfo.get("instabuys_daily").getAsFloat()));
						}
						break;
					case 8:
						if (bazaarInfo.has("instasells_daily")) {
							if (!added) {
								tooltip.add("");
								added = true;
							}

							tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD +
								"Insta-Sells (Daily): " +
								ChatFormatting.GOLD + ChatFormatting.BOLD +
								format.format(bazaarInfo.get("instasells_daily").getAsFloat()));
						}
						break;
					case 9:
						if (bazaarInfo.has("instabuys_weekly")) {
							if (!added) {
								tooltip.add("");
								added = true;
							}

							tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD +
								"Insta-Buys (Weekly): " +
								ChatFormatting.GOLD + ChatFormatting.BOLD +
								format.format(bazaarInfo.get("instabuys_weekly").getAsFloat()));
						}
						break;
					case 10:
						if (bazaarInfo.has("instasells_weekly")) {
							if (!added) {
								tooltip.add("");
								added = true;
							}

							tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD +
								"Insta-Sells (Weekly): " +
								ChatFormatting.GOLD + ChatFormatting.BOLD +
								format.format(bazaarInfo.get("instasells_weekly").getAsFloat()));
						}
						break;
				}
			}

		} else if (auctionItem && !auctionInfoErrored) {
			List<Integer> lines = NotEnoughUpdates.INSTANCE.config.tooltipTweaks.priceInfoAuc;


			for (int lineId : lines) {
				switch (lineId) {
					case 0:
						if (lowestBin > 0) {
							if (!added) {
								tooltip.add("");
								added = true;
							}
							tooltip.add(formatPrice("Lowest BIN: ", lowestBin));
						}
						break;
					case 1:
						if (auctionInfo != null) {
							if (!added) {
								tooltip.add("");
								added = true;
							}

							if (auctionInfo.has("clean_price")) {
								double cleanPrice = auctionInfo.get("clean_price").getAsDouble();
								tooltip.add(formatPrice("AH Price (Clean): ", cleanPrice));
							} else {
								double auctionPrice = auctionInfo.get("price").getAsDouble() / auctionInfo.get("count").getAsFloat();
								tooltip.add(formatPrice("AH Price: ", auctionPrice));
							}

						}
						break;
					case 2:
						if (auctionInfo != null) {
							if (!added) {
								tooltip.add("");
								added = true;
							}
							if (auctionInfo.has("clean_price")) {
								tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD + "AH Sales (Clean): " +
									ChatFormatting.GOLD + ChatFormatting.BOLD +
									(auctionInfo.get("clean_sales").getAsFloat() < 2 ?
										format.format(auctionInfo.get("clean_sales").getAsFloat()) + " sale/day"
										:
											format.format(auctionInfo.get("clean_sales").getAsFloat()) + " sales/day"));
							} else {
								tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD + "AH Sales: " +
									ChatFormatting.GOLD + ChatFormatting.BOLD +
									(auctionInfo.get("sales").getAsFloat() < 2 ? format.format(auctionInfo.get("sales").getAsFloat()) +
										" sale/day"
										: format.format(auctionInfo.get("sales").getAsFloat()) +
											" sales/day"));
							}
						}
						break;
					case 3:
						if (craftCost != null && craftCost.fromRecipe) {
							if (craftCost.craftCost == 0) {
								continue;
							}
							if (!added) {
								tooltip.add("");
								added = true;
							}
							tooltip.add(formatPrice("Raw Craft Cost: ", craftCost.craftCost));
						}
						break;
					case 4:
						if (lowestBinAvg > 0) {
							if (!added) {
								tooltip.add("");
								added = true;
							}
							tooltip.add(formatPrice("AVG Lowest BIN: ", lowestBinAvg));
						}
						break;
					case 5:
						if (Constants.ESSENCECOSTS == null) break;
						JsonObject essenceCosts = Constants.ESSENCECOSTS;
						if (!essenceCosts.has(internalname)) {
							break;
						}
						JsonObject itemCosts = essenceCosts.get(internalname).getAsJsonObject();
						String essenceType = itemCosts.get("type").getAsString();
						boolean requiresItems = false;
						JsonObject itemsObject = null;
						if (itemCosts.has("items")) {
							requiresItems = true;
							itemsObject = itemCosts.get("items").getAsJsonObject();
						}

						int dungeonItemLevel = Utils.getNumberOfStars(stack);

						if (dungeonItemLevel == -1) {
							int dungeonizeCost = 0;
							if (itemCosts.has("dungeonize")) {
								dungeonizeCost = itemCosts.get("dungeonize").getAsInt();
							}

							if (dungeonizeCost > 0) {
								tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD + "Dungeonize Cost: " +
									ChatFormatting.GOLD + ChatFormatting.BOLD + dungeonizeCost + " " + essenceType);
							}
						} else if (dungeonItemLevel >= 0) {
							String nextStarLevelString = (dungeonItemLevel + 1) + "";
							int nextStarLevelInt = Integer.parseInt(nextStarLevelString);

							if (itemCosts.has(nextStarLevelString)) {
								int upgradeCost = itemCosts.get(nextStarLevelString).getAsInt();
								String starString = Utils.getStarsString(nextStarLevelInt);
								tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD + "Upgrade to " +
									starString + ChatFormatting.YELLOW + ChatFormatting.BOLD + ": " +
									ChatFormatting.GOLD + ChatFormatting.BOLD + upgradeCost + " " + essenceType);
								if (requiresItems && itemsObject.has(nextStarLevelString)) {
									boolean shouldShow = InputConstants.isKeyDown(Keyboard.KEY_LCONTROL) ||
										NotEnoughUpdates.INSTANCE.config.tooltipTweaks.alwaysShowRequiredItems;

									if (shouldShow) {
										tooltip.add(ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD + "Required Items:");
										for (JsonElement item : itemsObject.get(nextStarLevelString).getAsJsonArray()) {
											if (item.getAsString().contains("§")) {
												//TODO show outdated repo notification when 2.1.1 releases
												tooltip.add("  - " + item.getAsString());
												continue;
											}
											String itemString = item.getAsString();
											int colon = itemString.indexOf(':');
											if (colon != -1) {
												String amount = itemString.substring(colon + 1);
												String requiredItem = itemString.substring(0, colon);
												if (requiredItem.equals("SKYBLOCK_COIN")) {
													tooltip.add("  - " + ChatFormatting.GOLD + amount + " Coins");
												}

												if (NotEnoughUpdates.INSTANCE.manager.isValidInternalName(requiredItem)) {
													JsonObject itemObject = NotEnoughUpdates.INSTANCE.manager.
														createItemResolutionQuery().
														withKnownInternalName(requiredItem).
														resolveToItemListJson();

													if (itemObject != null && itemObject.has("displayname")) {
														String displayName = itemObject.get("displayname").getAsString();
														tooltip.add("  - " + displayName + ChatFormatting.DARK_GRAY + " x" + amount);
													}
												}
											}
										}
									} else {
										tooltip.add(ChatFormatting.DARK_GRAY + "[CTRL to show required items]");
									}
								}
							}
						}
						break;
				}
			}

		} else if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.rawCraft && craftCost != null && craftCost.fromRecipe) {
			if (craftCost.craftCost != 0) {
				double cost = craftCost.craftCost;
				cost = cost * stackMultiplier;
				added = true;
				tooltip.add("");
				tooltip.add(formatPrice("Raw Craft Cost: ", cost));
			}
		} else if (auctionInfoErrored && NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			String message = ChatFormatting.RED.toString() + ChatFormatting.BOLD + "[NEU] API is down";
			if (auctionableItems != null && !auctionableItems.isEmpty()) {
				if (auctionableItems.contains(internalname)) {
					tooltip.add(message);
				}
			} else {
				tooltip.add(message + " and no item data is cached");
			}
		}
		Double npcSellPrice = HypixelItemAPI.getNPCSellPrice(internalname);
		if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.npcSellPrice && npcSellPrice != null && npcSellPrice != 0) {
			if (!added)
				tooltip.add("");
			tooltip.add(formatPrice("NPC Sell Price: ", npcSellPrice * stackMultiplier));
		}

		if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.museumDonationStatus) {
			if (!MuseumTooltipManager.INSTANCE.hasPlayerVisitedMuseum()) {
				tooltip.add(ChatFormatting.RED + ChatFormatting.BOLD.toString() +
					"[NEU] Visit your Museum to display donation status");
			}
			if (MuseumTooltipManager.INSTANCE.isItemDonated(internalname)) {
				tooltip.add(
					ChatFormatting.YELLOW + "Item already donated to museum");
			} else if (MuseumTooltipManager.INSTANCE.canItemBeDonated(internalname)) {
				tooltip.add(
					ChatFormatting.RED + "Item not donated to museum");
			}
		}
	}

	private static String formatPrice(String label, double price) {
		boolean shortNumber = NotEnoughUpdates.INSTANCE.config.tooltipTweaks.shortNumberFormatPrices;
		String number = (shortNumber && price > 1000
			? Utils.shortNumberFormat(price, 0)
			: price > 5 && Integer.MAX_VALUE > price ? format.format((int) price) : format.format(price));
		return "§e§l" + label + "§6§l" + number + " coins";
	}

}
