/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscgui.itemcustomization;

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.core.ChromaColour;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.item.ItemArmor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour.custom_ench_colour;

public class ItemCustomizationUtils {

	public static List<String> customizeColourGuide = Lists.newArrayList(
		ChatFormatting.AQUA + "Set a custom name for the item",
		ChatFormatting.GREEN + "",
		ChatFormatting.GREEN + "Type \"&&\" for ¶",
		ChatFormatting.GREEN + "Type \"**\" for ✪",
		ChatFormatting.GREEN + "Type \"*1-9\" for ➊-➒",
		ChatFormatting.GREEN + "",
		ChatFormatting.GREEN + "Available colour codes:",
		Utils.chromaString("¶z = Chroma"),
		ChatFormatting.DARK_BLUE + "¶1 = Dark Blue",
		ChatFormatting.DARK_GREEN + "¶2 = Dark Green",
		ChatFormatting.DARK_AQUA + "¶3 = Dark Aqua",
		ChatFormatting.DARK_RED + "¶4 = Dark Red",
		ChatFormatting.DARK_PURPLE + "¶5 = Dark Purple",
		ChatFormatting.GOLD + "¶6 = Gold",
		ChatFormatting.GRAY + "¶7 = Gray",
		ChatFormatting.DARK_GRAY + "¶8 = Dark Gray",
		ChatFormatting.BLUE + "¶9 = Blue",
		ChatFormatting.GREEN + "¶a = Green",
		ChatFormatting.AQUA + "¶b = Aqua",
		ChatFormatting.RED + "¶c = Red",
		ChatFormatting.LIGHT_PURPLE + "¶d = Purple",
		ChatFormatting.YELLOW + "¶e = Yellow",
		ChatFormatting.WHITE + "¶f = White",
		"§Z¶Z = SBA Chroma" + ChatFormatting.RESET + ChatFormatting.GRAY + " (Requires SBA)",
		"",
		ChatFormatting.GREEN + "Available formatting codes:",
		ChatFormatting.GRAY + "¶k = " + ChatFormatting.OBFUSCATED + "Obfuscated",
		ChatFormatting.GRAY + "¶l = " + ChatFormatting.BOLD + "Bold",
		ChatFormatting.GRAY + "¶m = " + ChatFormatting.STRIKETHROUGH + "Strikethrough",
		ChatFormatting.GRAY + "¶n = " + ChatFormatting.UNDERLINE + "Underline",
		ChatFormatting.GRAY + "¶o = " + ChatFormatting.ITALIC + "Italic"
	);

	public static List<String> resetGuide = Lists.newArrayList(
		ChatFormatting.RED + "" + ChatFormatting.BOLD + "This will reset all customisations!!",
		ChatFormatting.GREEN + "",
		ChatFormatting.RED + "Only click if you are sure you want to reset everything for this item"
	);

	public static List<String> speedGuide = Lists.newArrayList(
		ChatFormatting.AQUA + "This is how fast the dyes will cycle, in ticks",
		ChatFormatting.GRAY + "§6Hypixel §7dyes cycle every 2 ticks",
		ChatFormatting.GRAY + "",
		ChatFormatting.GRAY + "In the §dgradient mode §7this decides the amount of intermediary colours",
		ChatFormatting.GRAY + "This means if speed is set to 1 it's the same as §aCycling mode"
	);

	public static List<String> skullGuide = Lists.newArrayList(
		ChatFormatting.GOLD + "How to use custom skulls",
		ChatFormatting.YELLOW + "1. Search for the skull in the item list",
		ChatFormatting.YELLOW + "2. Middle click on the item in the item list, this should give you the item id",
		ChatFormatting.YELLOW + "3. Copy the item id and put it after \"skull:\". Make sure you remove the \"id:\"",
		ChatFormatting.YELLOW + "",
		ChatFormatting.GREEN  + "Examples:",
		ChatFormatting.YELLOW + "skull:YOUNG DRAGON HELMET",
		ChatFormatting.YELLOW + "skull:WITHER_GOGGLES_CELESTIAL"
	);

	public static ItemStack copy(ItemStack stack, GuiItemCustomize instance) {
		ItemStack customStack = stack.copy();
		if (!instance.textFieldCustomItem.getText().isEmpty()) {
			customStack.setItem(ItemCustomizeManager.getCustomItem(stack, instance.textFieldCustomItem.getText().trim()));
			customStack.setItemDamage(ItemCustomizeManager.getCustomItemDamage(stack));
			CompoundTag tagCompound = customStack.getTag();
			if (tagCompound != null) {
				CompoundTag customSkull = ItemCustomizeManager.getCustomSkull(customStack);
				if (customSkull != null) {
					tagCompound.removeTag("SkullOwner");
					tagCompound.setTag("SkullOwner", customSkull);
				}
			}
		}
		return customStack;
	}

	public static int getGlintColour(GuiItemCustomize instance) {
		int col = instance.customGlintColour == null
			? ChromaColour.specialToChromaRGB(ItemCustomizeManager.DEFAULT_GLINT_COLOR)
			: ChromaColour.specialToChromaRGB(instance.customGlintColour);
		return 0xff000000 | col;
	}

	public static int getLeatherColour(GuiItemCustomize instance) {
		if (!instance.supportCustomLeatherColour) return 0xff000000;

		String customLeatherColour = instance.customLeatherColour;
		int col = customLeatherColour == null
			? ((ItemArmor) instance.customItemStack.getItem()).getColor(instance.customItemStack)
			: ChromaColour.specialToChromaRGB(customLeatherColour);
		return 0xff000000 | col;
	}

	public static int getLeatherColour(String colourString) {
		return 0xff000000 | ChromaColour.specialToChromaRGB(colourString);
	}

	public static String getChromaStrFromLeatherColour(GuiItemCustomize instance) {
		ItemStack customItemStack = instance.customItemStack;
		return ChromaColour.special(0, 0xff, ((ItemArmor) customItemStack.getItem()).getColor(customItemStack));
	}

	public static void renderFooter(int xCenter, int yTop, GuiType guiType) {
		int xCentreLeft = xCenter - 90;
		int xCentreRight = xCenter;

		Gui.drawRect(xCentreLeft, yTop, xCenter + 1, yTop + 17, 0xff101016);
		Gui.drawRect(xCentreLeft, yTop, xCenter - 1, yTop + 15, 0xff101016);
		Gui.drawRect(xCentreLeft - 1, yTop + 1, xCenter - 2, yTop + 14, 0xff000000 | 0xff00ffc4);

		Utils.renderShadowedString(getButtons(guiType, 0).getDisplay(),
			xCentreLeft + 44,
			yTop + 4,
			xCenter * 2 - xCentreRight
		);

		xCentreLeft += 90;
		xCentreRight += 90;

		Gui.drawRect(xCentreLeft, yTop, xCentreRight, yTop + 17, 0x70000000);
		Gui.drawRect(xCentreLeft, yTop, xCentreRight, yTop + 15, 0xff101016);
		Gui.drawRect(xCentreLeft - 1, yTop + 1, xCentreRight, yTop + 14, 0xff000000 | 0xff00ffc4 * 2);

		Utils.renderShadowedString(getButtons(guiType, 1).getDisplay(),
			xCentreLeft + 45,
			yTop + 4,
			xCenter * 2 - xCentreRight
		);
	}

	public static GuiType getButtons(GuiType guiType, int button) {
		if (button == 0) {
			if (guiType == GuiType.DEFAULT) {
				return GuiType.ANIMATED;
			} else {
				return GuiType.DEFAULT;
			}
		}
		if (button == 1) {
			if (guiType == GuiType.HYPIXEL) {
				return GuiType.ANIMATED;
			} else {
				return GuiType.HYPIXEL;
			}
		}

		return GuiType.DEFAULT;
	}

	public static GuiType getButtonClicked(int mouseX, int mouseY, GuiType guiType, float offset) {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getInstance());
		int xCenter = scaledResolution.getScaledWidth() / 2;
		int xCentreLeft = xCenter - 90;
		int xCentreRight = xCenter;

		for (int i = 0; i < 2; i++) {
			if (mouseX >= xCentreLeft && mouseX <= xCentreRight &&
				mouseY >= offset - 7 && mouseY <= offset + 12) {
				return getButtons(guiType, i);
			}
			xCentreLeft += 90;
			xCentreRight += 90;
		}

		return null;
	}

	public static int getAnimatedDyeColour(String[] dyeColours, int ticks, DyeMode dyeMode) {
		if (dyeMode == DyeMode.GRADIENT) {
			int i = getTicksForList(ticks, dyeColours.length);
			int dyeColour1 = ChromaColour.specialToChromaRGB(dyeColours[i]);
			if (i == dyeColours.length - 1) {
				i = 0;
			} else {
				i++;
			}
			int dyeColour2 = ChromaColour.specialToChromaRGB(dyeColours[i]);
			return blendColors(dyeColour1, dyeColour2, (float) (Minecraft.getInstance().player.ticksExisted % ticks) / ticks);
		}
		return ChromaColour.specialToChromaRGB(
			dyeColours[getTicksForList(ticks, dyeColours.length)]);
	}

	static final ResourceLocation RESET = new ResourceLocation("notenoughupdates", "notenoughupdates:itemcustomize/reset.png");
	static final ResourceLocation CROSS = new ResourceLocation("notenoughupdates", "notenoughupdates:itemcustomize/cross.png");

	public static void renderColourBlob(int xCenter, int yTop, int colour, String text, boolean renderReset,
																			boolean renderCross) {
		Gui.drawRect(xCenter - 90, yTop, xCenter + 92, yTop + 17, 0x70000000);
		Gui.drawRect(xCenter - 90, yTop, xCenter + 90, yTop + 15, 0xff101016);
		Gui.drawRect(xCenter - 89, yTop + 1, xCenter + 89, yTop + 14, 0xff000000 | colour);

		Utils.renderShadowedString(text, xCenter, yTop + 4, 180);

		if (renderReset) {
			Minecraft.getInstance().getTextureManager().bindTexture(RESET);
			com.mojang.blaze3d.systems.RenderSystem.color(1, 1, 1, 1);
			RenderUtils.drawTexturedRect(xCenter + 90 - 12, yTop + 2, 10, 11, GL11.GL_NEAREST);
		}

		if (renderCross) {
			Minecraft.getInstance().getTextureManager().bindTexture(CROSS);
			com.mojang.blaze3d.systems.RenderSystem.color(1, 1, 1, 1);
			RenderUtils.drawTexturedRect(xCenter + 90 - 12, yTop + 3, 9, 9, GL11.GL_NEAREST);
		}
	}

	public static void renderTextBox(
		GuiElementTextField textField, String text, int xOffset, int yOffset, int maxTextSize
	) {
		if (!textField.getFocus() && textField.getText().isEmpty()) {
			textField.setOptions(GuiElementTextField.SCISSOR_TEXT);
			textField.setPrependText(text);
		} else {
			textField.setOptions(GuiElementTextField.COLOUR | GuiElementTextField.SCISSOR_TEXT);
			textField.setPrependText("");
		}

		if (!textField.getFocus()) {
			textField.setSize(maxTextSize, 20);
		} else {
			int textSize = Minecraft.getInstance().font.getStringWidth(textField.getTextDisplay()) + 10;
			textField.setSize(Math.max(textSize, maxTextSize), 20);
		}

		textField.render(xOffset, yOffset);
	}

	public static Color getColourFromHex(String hex) {
		Color color = null;
		try {
			int decode = Integer.decode(hex);
			color = new Color(decode);
		} catch (NumberFormatException | NullPointerException e) {
		}
		return color;
	}

	public static int rgbToInt(Color color) {
		return (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
	}

	public static void renderPresetButtons(int x, int y, boolean valid, boolean secondValid, String preset) {
		Minecraft.getInstance().getTextureManager().bindTexture(custom_ench_colour);
		com.mojang.blaze3d.systems.RenderSystem.color(1, 1, 1, 1);
		Utils.drawTexturedRect(
			x - 88 + 198,
			y + 2,
			88,
			20,
			64 / 217f,
			152 / 217f,
			48 / 78f,
			68 / 78f,
			GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(
			x - 88 + 198,
			y + 2 + 24,
			88,
			20,
			64 / 217f,
			152 / 217f,
			48 / 78f,
			68 / 78f,
			GL11.GL_NEAREST
		);

		Utils.drawStringCenteredScaledMaxWidth("Load " + preset, x - 44 + 198, y + 8, false, 86, 4210752);
		Utils.drawStringCenteredScaledMaxWidth("from Clipboard", x - 44 + 198, y + 16, false, 86, 4210752);
		Utils.drawStringCenteredScaledMaxWidth("Save " + preset, x - 44 + 198, y + 8 + 24, false, 86, 4210752);
		Utils.drawStringCenteredScaledMaxWidth("to Clipboard", x - 44 + 198, y + 16 + 24, false, 86, 4210752);

		if (!valid) {
			Gui.drawRect(x - 88 + 198, y + 2, x + 198, y + 2 + 20, 0x80000000);
		}
		if (!secondValid) {
			Gui.drawRect(x - 88 + 198, y + 2 + 24, x + 198, y + 2 + 20 + 24, 0x80000000);
		}

		com.mojang.blaze3d.systems.RenderSystem.color(1, 1, 1, 1);
	}

	public static boolean validShareContents(String sharePrefix) {
		String base64 = Utils.getClipboard();
		if (base64 == null) return false;

		if (base64.length() <= sharePrefix.length()) return false;

		base64 = base64.trim();

		try {
			return new String(Base64.getDecoder().decode(base64)).startsWith(sharePrefix);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static void shareContents(String sharePrefix, String jsonObject) {
		String base64String = Base64.getEncoder().encodeToString((sharePrefix +
			jsonObject).getBytes(StandardCharsets.UTF_8));
		Utils.copyToClipboard(base64String);
	}

	public static String getShareFromClipboard(String sharePrefix) {

		String base64 = Utils.getClipboard();
		if (base64 == null) return null;

		if (base64.length() <= sharePrefix.length()) return null;

		base64 = base64.trim();

		String jsonString;
		try {
			jsonString = new String(Base64.getDecoder().decode(base64));
			if (!jsonString.startsWith(sharePrefix)) return null;
			jsonString = jsonString.substring(sharePrefix.length());
		} catch (IllegalArgumentException e) {
			return null;
		}

		return jsonString;
	}

	public static int blendColors(int startColorInt, int endColorInt, float ratio) {
		Color startColour = new Color(startColorInt);
		Color endColour = new Color(endColorInt);
		int redStart = startColour.getRed();
		int greenStart = startColour.getGreen();
		int blueStart = startColour.getBlue();

		int redEnd = endColour.getRed();
		int greenEnd = endColour.getGreen();
		int blueEnd = endColour.getBlue();

		int red = (int) (redStart + (redEnd - redStart) * ratio);
		int green = (int) (greenStart + (greenEnd - greenStart) * ratio);
		int blue = (int) (blueStart + (blueEnd - blueStart) * ratio);

		return (red << 16) | (green << 8) | blue;
	}

	public static int getTicksForList(int speedTicks, int listSize) {
		return getTicksForList(speedTicks, listSize, -1);
	}

	public static int getTicksForList(int speedTicks, int listSize, int presetIndex) {
		int animatedIndex = (Minecraft.getInstance().player.ticksExisted / speedTicks) % listSize;
		if (presetIndex >= 0 && presetIndex < listSize) {
			animatedIndex = presetIndex;
		}
		return animatedIndex;
	}
}
