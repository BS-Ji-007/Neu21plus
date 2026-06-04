/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.core.config;

import com.google.gson.annotations.Expose;

public class Position {
    @Expose public float x;
    @Expose public float y;
    @Expose public Anchor anchor = Anchor.TOP_LEFT;

    public enum Anchor {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    }
}
