package io.github.legentpc.neu21plus.itemrepo;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ItemResolutionQuery {

    private ItemStack itemStack;
    private String internalName;

    public ItemResolutionQuery() {
    }

    @NotNull
    public ItemResolutionQuery withItemStack(@Nullable ItemStack stack) {
        this.itemStack = stack;
        return this;
    }

    @NotNull
    public ItemResolutionQuery withInternalName(@Nullable String name) {
        this.internalName = name;
        return this;
    }

    @Nullable
    public String resolve() {
        if (internalName != null && !internalName.isEmpty()) {
            return internalName;
        }

        if (itemStack == null || itemStack.isEmpty()) {
            return null;
        }

        String fromNbt = resolveFromNbt(itemStack);
        if (fromNbt != null) {
            return fromNbt;
        }

        String fromDisplayName = resolveFromDisplayName(itemStack);
        if (fromDisplayName != null) {
            return fromDisplayName;
        }

        return null;
    }

    @Nullable
    private String resolveFromNbt(@NotNull ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;

        CompoundTag nbt = customData.copyTag();
        CompoundTag extraAttributes = nbt.getCompoundOrEmpty("ExtraAttributes");
        if (extraAttributes.isEmpty()) return null;

        String skyblockId = extraAttributes.getStringOr("id", "");
        if (skyblockId != null && !skyblockId.isEmpty()) {
            ItemRepo repo = ItemRepo.getInstance();
            if (repo.hasItem(skyblockId)) {
                return skyblockId;
            }
        }

        return null;
    }

    @Nullable
    private String resolveFromDisplayName(@NotNull ItemStack stack) {
        String displayName = stack.getHoverName().getString();
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }

        String cleaned = displayName.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();
        if (cleaned.isEmpty()) return null;

        ItemRepo repo = ItemRepo.getInstance();
        for (Map.Entry<String, String> entry : repo.getDisplayNames()) {
            String itemName = entry.getValue().replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();
            if (itemName.equalsIgnoreCase(cleaned)) {
                return entry.getKey();
            }
        }

        return null;
    }
}
