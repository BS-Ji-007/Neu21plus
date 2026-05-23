package io.github.legentpc.neu21plus.client.overlay;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.itemrepo.ItemResolutionQuery;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TooltipModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(TooltipModifier.class);

    private static TooltipModifier instance;

    public static TooltipModifier getInstance() {
        if (instance == null) {
            instance = new TooltipModifier();
        }
        return instance;
    }

    private boolean registered = false;

    private TooltipModifier() {
    }

    public void register() {
        if (registered) return;

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
            modifyTooltip(stack, lines);
        });

        registered = true;
        LOGGER.info("Tooltip modifier registered");
    }

    private void modifyTooltip(ItemStack stack, List<Component> lines) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null) return;

        ItemRepo repo = ItemRepo.getInstance();
        if (!repo.isLoaded()) return;

        String internalName = new ItemResolutionQuery().withItemStack(stack).resolve();
        if (internalName == null) return;

        var itemJson = repo.getItemJson(internalName);
        if (itemJson == null) return;

        if (config.general.showItemRarity) {
            addRarityTooltip(internalName, lines);
        }

        Minecraft client = Minecraft.getInstance();
        boolean shiftHeld = client.options.keyShift.isDown();

        ItemPriceInformation.getInstance().addToTooltip(internalName, lines, shiftHeld);
    }

    private void addRarityTooltip(String internalName, List<Component> lines) {
        ItemRepo repo = ItemRepo.getInstance();
        var itemJson = repo.getItemJson(internalName);
        if (itemJson == null || !itemJson.has("lore")) return;

        var loreArray = itemJson.getAsJsonArray("lore");
        if (loreArray == null || loreArray.isEmpty()) return;

        for (int i = loreArray.size() - 1; i >= 0; i--) {
            String loreLine = loreArray.get(i).getAsString();
            String cleaned = loreLine.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();
            String rarity = extractRarity(cleaned);
            if (rarity != null) {
                return;
            }
        }
    }

    private String extractRarity(String text) {
        String upper = text.toUpperCase();
        if (upper.contains("COMMON")) return "COMMON";
        if (upper.contains("UNCOMMON")) return "UNCOMMON";
        if (upper.contains("RARE")) return "RARE";
        if (upper.contains("EPIC")) return "EPIC";
        if (upper.contains("LEGENDARY")) return "LEGENDARY";
        if (upper.contains("MYTHIC")) return "MYTHIC";
        if (upper.contains("DIVINE")) return "DIVINE";
        if (upper.contains("SPECIAL")) return "SPECIAL";
        if (upper.contains("VERY SPECIAL")) return "VERY SPECIAL";
        return null;
    }
}
