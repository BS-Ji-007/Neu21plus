/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import java.util.ArrayList;
import java.util.List;

public class OverlayManager {
    public static final List<TextOverlay> textOverlays = new ArrayList<>();
    public static final List<Class<? extends TextOverlay>> dontRenderOverlay = new ArrayList<>();

    public static void init() {
        // Register all overlays
    }
}
