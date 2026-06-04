/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.core.util.lerp;

public class LerpUtils {
    public static float lerp(float start, float end, float delta) {
        return start + delta * (end - start);
    }
}
