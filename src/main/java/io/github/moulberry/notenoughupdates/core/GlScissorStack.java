/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.core;

import net.minecraft.client.Minecraft;

import java.util.Stack;

public class GlScissorStack {
    private static final Stack<Rectangle> stack = new Stack<>();

    public static void push(int x, int y, int width, int height) {
        // Modern scissoring using ScissorStack concepts
    }

    public static void pop() {
        if (!stack.isEmpty()) stack.pop();
    }

    private record Rectangle(int x, int y, int width, int height) {}
}
