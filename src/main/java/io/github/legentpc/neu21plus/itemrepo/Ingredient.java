package io.github.legentpc.neu21plus.itemrepo;

import org.jetbrains.annotations.NotNull;

public class Ingredient {

    public static final String SKYBLOCK_COIN = "SKYBLOCK_COIN";

    private final String internalItemId;
    private final int count;

    public Ingredient(@NotNull String internalItemId, int count) {
        this.internalItemId = internalItemId;
        this.count = count;
    }

    @NotNull
    public static Ingredient parse(String input) {
        if (input == null || input.isEmpty()) {
            return new Ingredient("", 0);
        }
        String trimmed = input.trim();
        int colonIndex = trimmed.lastIndexOf(':');
        if (colonIndex > 0) {
            String itemId = trimmed.substring(0, colonIndex);
            String countStr = trimmed.substring(colonIndex + 1);
            try {
                int parsedCount = Integer.parseInt(countStr);
                return new Ingredient(itemId, parsedCount);
            } catch (NumberFormatException e) {
                return new Ingredient(trimmed, 1);
            }
        }
        return new Ingredient(trimmed, 1);
    }

    @NotNull
    public String getInternalItemId() {
        return internalItemId;
    }

    public int getCount() {
        return count;
    }

    public boolean isCoin() {
        return SKYBLOCK_COIN.equals(internalItemId);
    }

    public boolean isEmpty() {
        return internalItemId.isEmpty();
    }

    @Override
    public String toString() {
        if (count <= 1) {
            return internalItemId;
        }
        return internalItemId + ":" + count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return count == that.count && internalItemId.equals(that.internalItemId);
    }

    @Override
    public int hashCode() {
        int result = internalItemId.hashCode();
        result = 31 * result + count;
        return result;
    }
}
