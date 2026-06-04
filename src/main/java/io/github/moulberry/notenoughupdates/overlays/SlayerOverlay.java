/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import net.minecraft.client.gui.GuiGraphics;
import java.util.List;
import java.util.function.Supplier;

public class SlayerOverlay extends TextOverlay {
    public static boolean slayerQuest;
    public static long unloadOverlayTimer = -1;
    public static long timeSinceLastBoss = 0;
    public static long timeSinceLastBoss2 = 0;
    public static String RNGMeter = "?";
    public static boolean isSlain = false;
    public static String slayerLVL = "-1";
    public static String slayerXp = "0";

	public SlayerOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	@Override
	public void update() {
        // Slayer overlay update logic
	}

    @Override
    public void render(GuiGraphics graphics) {
        // Render logic
    }

    @Override
    public void tick() {}
}
