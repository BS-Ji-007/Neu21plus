package io.github.legentpc.neu21plus.client.overlay;

import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.recipe.NeuRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeHistory {

    private static final int MAX_HISTORY = 50;

    private final List<Entry> history = new ArrayList<>();
    private int currentIndex = -1;

    public void push(@Nullable String itemId, boolean isRecipe) {
        if (itemId == null || itemId.isEmpty()) return;

        if (currentIndex >= 0 && currentIndex < history.size()) {
            Entry current = history.get(currentIndex);
            if (current.itemId.equals(itemId) && current.isRecipe == isRecipe) {
                return;
            }
        }

        if (currentIndex < history.size() - 1) {
            history.subList(currentIndex + 1, history.size()).clear();
        }

        history.add(new Entry(itemId, isRecipe));
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
        currentIndex = history.size() - 1;
    }

    @Nullable
    public Entry goBack() {
        if (currentIndex > 0) {
            currentIndex--;
            return history.get(currentIndex);
        }
        return null;
    }

    @Nullable
    public Entry goForward() {
        if (currentIndex < history.size() - 1) {
            currentIndex++;
            return history.get(currentIndex);
        }
        return null;
    }

    @Nullable
    public Entry getCurrent() {
        if (currentIndex >= 0 && currentIndex < history.size()) {
            return history.get(currentIndex);
        }
        return null;
    }

    public boolean canGoBack() {
        return currentIndex > 0;
    }

    public boolean canGoForward() {
        return currentIndex < history.size() - 1;
    }

    public void clear() {
        history.clear();
        currentIndex = -1;
    }

    public int size() {
        return history.size();
    }

    public static class Entry {
        public final String itemId;
        public final boolean isRecipe;

        public Entry(String itemId, boolean isRecipe) {
            this.itemId = itemId;
            this.isRecipe = isRecipe;
        }
    }
}
