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

package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.ChromaColour;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCooldowns;
import io.github.moulberry.notenoughupdates.miscgui.itemcustomization.ItemCustomizationUtils;
import io.github.moulberry.notenoughupdates.miscgui.itemcustomization.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin({RenderItem.class})
public abstract class MixinRenderItem {
	private static void func_181565_a(
		WorldRenderer w, int x, int y, float width, int height,
		int r, int g, int b, int a
	) {
		w.begin(7, DefaultVertexFormats.POSITION_COLOR);
		w.pos(x, y, 0.0D)
		 .color(r, g, b, a).endVertex();
		w.pos(x, y + height, 0.0D)
		 .color(r, g, b, a).endVertex();
		w.pos(x + width, y + height, 0.0D)
		 .color(r, g, b, a).endVertex();
		w.pos(x + width, y, 0.0D)
		 .color(r, g, b, a).endVertex();
		Tessellator.getInstance().draw();
	}

	private static String customEnchGlint = null;

	@Redirect(method = "renderItem(Lnet.minecraft.world.item.ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet.minecraft.world.item.ItemStack;hasEffect()Z"
		)
	)
	public boolean renderItem_hasEffect(ItemStack stack) {
		ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
		if (data != null) {
			customEnchGlint = data.customGlintColour;
			if (data.overrideEnchantGlint) {
				return data.enchantGlintValue;
			}
		} else {
			customEnchGlint = null;
		}

		return stack.hasEffect();
	}

	@Redirect(method = "renderItem(Lnet.minecraft.world.item.ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/tileentity/TileEntityItemStackRenderer;renderByItem(Lnet.minecraft.world.item.ItemStack;)V"
		)
	)
	public void renderItem_renderByItem(TileEntityItemStackRenderer tileEntityItemStackRenderer, ItemStack stack) {
		GL11.glPushMatrix();
		tileEntityItemStackRenderer.renderByItem(stack);
		GL11.glPopMatrix();

		ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
		if (data != null) {
			if (data.overrideEnchantGlint && data.enchantGlintValue) {
				ItemCustomizeManager.renderEffectHook(data.customGlintColour, (color) -> {
					float red = ((color >> 16) & 0xFF) / 255f;
					float green = ((color >> 8) & 0xFF) / 255f;
					float blue = (color & 0xFF) / 255f;
					float alpha = ((color >> 24) & 0xFF) / 255f;

					com.mojang.blaze3d.systems.RenderSystem.color(red, green, blue, alpha);

					com.mojang.blaze3d.systems.RenderSystem.scale(1 / 8f, 1 / 8f, 1 / 8f);
					com.mojang.blaze3d.systems.RenderSystem.matrixMode(GL11.GL_MODELVIEW);
					GL11.glPushMatrix();
					ItemCustomizeManager.disableTextureBinding = true;
					tileEntityItemStackRenderer.renderByItem(stack);
					ItemCustomizeManager.disableTextureBinding = false;
					GL11.glPopMatrix();

				});
			}
		}
	}

	@Redirect(method = "renderQuads",
		at = @At(
			value = "INVOKE",
			target = "Lnet.minecraft.world.item.Item;getColorFromItemStack(Lnet.minecraft.world.item.ItemStack;I)I"
		)
	)
	public int renderItem_renderByItem(Item item, ItemStack stack, int renderPass) {
		if (renderPass == 0) {
			ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
			if (data != null && data.animatedLeatherColours != null && data.animatedDyeTicks > 0 && ItemCustomizeManager.shouldRenderLeatherColour(stack)) {
				return ItemCustomizationUtils.getAnimatedDyeColour(data.animatedLeatherColours, data.animatedDyeTicks, data.dyeMode);
			} else if (data != null && data.customLeatherColour != null && ItemCustomizeManager.shouldRenderLeatherColour(stack)) {
				return ChromaColour.specialToChromaRGB(data.customLeatherColour);
			}
		}

		return item.getColorFromItemStack(stack, renderPass);
	}

	@Inject(method = "renderEffect", at = @At("HEAD"), cancellable = true)
	public void renderEffect(IBakedModel model, CallbackInfo ci) {
		if (ItemCustomizeManager.renderEffectHook(customEnchGlint, (color) -> renderModel(model, color))) {
			ci.cancel();
		}
	}

	@Shadow
	abstract void renderModel(IBakedModel model, int color);

	@Inject(method = "renderItemIntoGUI", at = @At("HEAD"))
	public void renderItemHead(ItemStack stack, int x, int y, CallbackInfo ci) {
		if (NotEnoughUpdates.INSTANCE.overlay.searchMode && RenderListener.drawingGuiScreen && NotEnoughUpdates.INSTANCE.isOnSkyblock() && !(Minecraft.getInstance().currentScreen instanceof GuiProfileViewer)) {
			boolean matches = false;

			GuiTextField textField = NEUOverlay.getTextField();

			if (textField.getText().trim().isEmpty()) {
				matches = true;
			} else if (stack != null) {
				for (String search : textField.getText().split("\\|")) {
					matches |= NotEnoughUpdates.INSTANCE.manager.doesStackMatchSearch(stack, search.trim());
				}
			}
			if (matches) {
				com.mojang.blaze3d.systems.RenderSystem.pushMatrix();
				com.mojang.blaze3d.systems.RenderSystem.translate(0, 0, 100 + Minecraft.getInstance().getRenderItem().zLevel);
				com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
				Gui.drawRect(x, y, x + 16, y + 16, NEUOverlay.overlayColourLight);
				com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
				com.mojang.blaze3d.systems.RenderSystem.popMatrix();
			}
		}
	}

	@Inject(method = "renderItemIntoGUI", at = @At("RETURN"))
	public void renderItemReturn(ItemStack stack, int x, int y, CallbackInfo ci) {
		if (stack != null && stack.getCount() != 1) return;
		if (NotEnoughUpdates.INSTANCE.overlay.searchMode && RenderListener.drawingGuiScreen && NotEnoughUpdates.INSTANCE.isOnSkyblock() && !(Minecraft.getInstance().currentScreen instanceof GuiProfileViewer)) {
			boolean matches = false;

			GuiTextField textField = NEUOverlay.getTextField();

			if (textField.getText().trim().isEmpty()) {
				matches = true;
			} else if (stack != null) {
				for (String search : textField.getText().split("\\|")) {
					matches |= NotEnoughUpdates.INSTANCE.manager.doesStackMatchSearch(stack, search.trim());
				}
			}
			if (!matches) {
				com.mojang.blaze3d.systems.RenderSystem.pushMatrix();
				com.mojang.blaze3d.systems.RenderSystem.translate(0, 0, 110 + Minecraft.getInstance().getRenderItem().zLevel);
				Gui.drawRect(x, y, x + 16, y + 16, NEUOverlay.overlayColourDark);
				com.mojang.blaze3d.systems.RenderSystem.popMatrix();
			}
		}
	}

	@Inject(method = "renderItemOverlayIntoGUI", at = @At("RETURN"))
	public void renderItemOverlayIntoGUI(
		FontRenderer fr,
		ItemStack stack,
		int xPosition,
		int yPosition,
		String text,
		CallbackInfo ci
	) {
		if (stack != null && stack.getCount() != 1) {
			if (NotEnoughUpdates.INSTANCE.overlay.searchMode && RenderListener.drawingGuiScreen && NotEnoughUpdates.INSTANCE.isOnSkyblock() && !(Minecraft.getInstance().currentScreen instanceof GuiProfileViewer)) {
				boolean matches = false;

				GuiTextField textField = NEUOverlay.getTextField();

				if (textField.getText().trim().isEmpty()) {
					matches = true;
				} else {
					for (String search : textField.getText().split("\\|")) {
						matches |= NotEnoughUpdates.INSTANCE.manager.doesStackMatchSearch(stack, search.trim());
					}
				}
				if (!matches) {
					com.mojang.blaze3d.systems.RenderSystem.pushMatrix();
					com.mojang.blaze3d.systems.RenderSystem.translate(0, 0, 110 + Minecraft.getInstance().getRenderItem().zLevel);
					com.mojang.blaze3d.systems.RenderSystem.disableDepth();
					Gui.drawRect(xPosition, yPosition, xPosition + 16, yPosition + 16, NEUOverlay.overlayColourDark);
					com.mojang.blaze3d.systems.RenderSystem.enableDepth();
					com.mojang.blaze3d.systems.RenderSystem.popMatrix();
				}
			}
		}

		if (stack == null) return;

		float damageOverride = ItemCooldowns.getDurabilityOverride(stack);

		if (damageOverride >= 0) {
			com.mojang.blaze3d.systems.RenderSystem.disableLighting();
			com.mojang.blaze3d.systems.RenderSystem.disableDepth();
			if (NotEnoughUpdates.INSTANCE.config.itemOverlays.oldCooldowns) {
				float barX = 13.0f - damageOverride * 13.0f;
				int col = (int) Math.round(255.0D - damageOverride * 255.0D);
				com.mojang.blaze3d.systems.RenderSystem.disableTexture2D();
				com.mojang.blaze3d.systems.RenderSystem.disableAlpha();
				com.mojang.blaze3d.systems.RenderSystem.disableBlend();
				Tessellator tessellator = Tessellator.getInstance();
				WorldRenderer worldrenderer = tessellator.getWorldRenderer();
				func_181565_a(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
				func_181565_a(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - col) / 4, 64, 0, 255);
				func_181565_a(worldrenderer, xPosition + 2, yPosition + 13, barX, 1, 255 - col, col, 0, 255);
				com.mojang.blaze3d.systems.RenderSystem.enableAlpha();
				com.mojang.blaze3d.systems.RenderSystem.enableTexture2D();
			} else {
				com.mojang.blaze3d.systems.RenderSystem.enableAlpha();

				Utils.drawRect(xPosition, yPosition + 16.0f * (1.0f - damageOverride), xPosition + 16, yPosition + 16, Integer.MAX_VALUE);
			}
			com.mojang.blaze3d.systems.RenderSystem.enableLighting();
			com.mojang.blaze3d.systems.RenderSystem.enableDepth();
		}
	}

	@Redirect(method = "renderItemOverlayIntoGUI", at = @At(value = "INVOKE", target = "Lnet.minecraft.world.item.Item;showDurabilityBar(Lnet.minecraft.world.item.ItemStack;)Z"))
	public boolean renderItemOverlayIntoGUI_showDurabilityBar(
		Item instance, ItemStack stack
	) {
		if (ItemCustomizeManager.hasCustomItem(stack)) return false;
		return stack.getItem().showDurabilityBar(stack);
	}
}
