/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.Container;

import java.util.List;
import java.util.function.Supplier;

public class MiningOverlay extends TextTabOverlay {
	public MiningOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	@Override
	public void update() {
        // Mining overlay update logic
	}

    @Override
    public void render(GuiGraphics graphics) {
        // Render logic
    }
}
