package io.github.legentpc.neu21plus.client.profileviewer;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.util.TextUtils;
import io.github.legentpc.neu21plus.client.notification.NotificationSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiProfileViewer extends Screen {

    private static final int BG_COLOR = 0xFF1a1a2e;
    private static final int HEADER_COLOR = 0xFF16213e;
    private static final int ACCENT_COLOR = 0xFF0f3460;
    private static final int TEXT_COLOR = 0xFFe0e0e0;
    private static final int HIGHLIGHT_COLOR = 0xFFe94560;
    private static final int GREEN_COLOR = 0xFF55ff55;
    private static final int YELLOW_COLOR = 0xFFffff55;
    private static final int GOLD_COLOR = 0xFFffaa00;
    private static final int GRAY_COLOR = 0xFFaaaaaa;

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.0");

    private final ProfileData profile;
    private int currentTab = 0;
    private int scrollOffset = 0;
    private int guiLeft;
    private int guiTop;
    private int guiWidth = 380;
    private int guiHeight = 280;

    private static final String[] TAB_NAMES = {"Stats", "Skills", "Inventory", "Pets", "Collections"};

    protected GuiProfileViewer(ProfileData profile) {
        super(Component.literal("Profile Viewer - " + profile.getPlayerName()));
        this.profile = profile;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - guiWidth) / 2;
        this.guiTop = (this.height - guiHeight) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        this.extractBackground(context, mouseX, mouseY, deltaTicks);
        renderHeader(context);
        renderTabs(context, mouseX, mouseY);
        renderContent(context);
    }

    private void renderHeader(GuiGraphicsExtractor context) {
        context.fill(guiLeft, guiTop, guiLeft + guiWidth, guiTop + 30, HEADER_COLOR);

        String name = "\u00a7b" + profile.getPlayerName();
        context.text(font, name, guiLeft + 8, guiTop + 8, 0xFF55ffff, true);

        if (profile.getProfileName() != null) {
            String profileInfo = "\u00a77Profile: \u00a7f" + profile.getProfileName();
            context.text(font, profileInfo, guiLeft + 8 + font.width(name) + 10, guiTop + 8, GRAY_COLOR, false);
        }

        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config != null && config.profileViewer.showNetworth) {
            String nw = "\u00a76Networth: " + TextUtils.formatNumber(profile.getNetworth());
            context.text(font, nw, guiLeft + guiWidth - font.width(nw) - 8, guiTop + 8, GOLD_COLOR, true);
        }

        String level = "\u00a7aSB Level: " + profile.getSkyblockLevel();
        context.text(font, level, guiLeft + guiWidth - font.width(level) - 8, guiTop + 18, GREEN_COLOR, true);
    }

    private void renderTabs(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int tabWidth = guiWidth / TAB_NAMES.length;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int x = guiLeft + i * tabWidth;
            int y = guiTop + 30;
            boolean active = i == currentTab;
            boolean hovered = mouseX >= x && mouseX < x + tabWidth && mouseY >= y && mouseY < y + 20;

            int color = active ? ACCENT_COLOR : (hovered ? 0xFF2a2a4e : BG_COLOR);
            context.fill(x, y, x + tabWidth, y + 20, color);

            if (active) {
                context.outline(x, y + 19, tabWidth, 1, HIGHLIGHT_COLOR);
            }

            String tabName = active ? "\u00a7f" + TAB_NAMES[i] : "\u00a77" + TAB_NAMES[i];
            int textX = x + (tabWidth - font.width(tabName)) / 2;
            context.text(font, tabName, textX, y + 6, active ? TEXT_COLOR : GRAY_COLOR, false);
        }
    }

    private void renderContent(GuiGraphicsExtractor context) {
        int contentX = guiLeft + 8;
        int contentY = guiTop + 54;

        context.fill(guiLeft, guiTop + 50, guiLeft + guiWidth, guiTop + guiHeight, BG_COLOR);

        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null) return;

        switch (currentTab) {
            case 0 -> renderStatsTab(context, contentX, contentY);
            case 1 -> renderSkillsTab(context, contentX, contentY);
            case 2 -> renderInventoryTab(context, contentX, contentY);
            case 3 -> renderPetsTab(context, contentX, contentY);
            case 4 -> renderCollectionsTab(context, contentX, contentY);
        }
    }

    private void renderStatsTab(GuiGraphicsExtractor context, int x, int y) {
        Map<String, Double> stats = profile.getStats();
        if (stats.isEmpty()) {
            context.text(font, "\u00a77No stats available (requires API key)", x, y, GRAY_COLOR, false);
            return;
        }

        int col1X = x;
        int col2X = x + (guiWidth - 16) / 2;
        int rowY = y;
        int index = 0;

        String[] statOrder = {"Health", "Defense", "Speed", "Strength", "Intelligence",
                "Crit Chance", "Crit Damage", "Bonus Attack Speed", "Sea Creature Chance",
                "Magic Find", "Pet Luck", "True Defense", "Ferocity", "Ability Damage",
                "Mining Speed", "Mining Fortune", "Farming Fortune", "Foraging Fortune"};

        for (String statName : statOrder) {
            Double value = stats.get(statName);
            if (value == null) continue;

            int drawX = index % 2 == 0 ? col1X : col2X;
            String text = "\u00a77" + statName + ": \u00a7f" + DECIMAL_FORMAT.format(value);
            context.text(font, text, drawX, rowY, TEXT_COLOR, false);

            if (index % 2 == 1) {
                rowY += font.lineHeight + 2;
            }
            index++;
        }
    }

    private void renderSkillsTab(GuiGraphicsExtractor context, int x, int y) {
        Map<String, Integer> skills = profile.getSkills();
        Map<String, Float> skillXp = profile.getSkillXp();

        if (skills.isEmpty() && skillXp.isEmpty()) {
            context.text(font, "\u00a77No skill data available", x, y, GRAY_COLOR, false);
            return;
        }

        String[] skillOrder = {"Farming", "Mining", "Combat", "Foraging", "Fishing",
                "Enchanting", "Alchemy", "Taming", "Carpentry", "Runecrafting"};

        int rowY = y;
        for (String skill : skillOrder) {
            Integer level = skills.get(skill);
            Float xp = skillXp.get(skill);

            String levelText = level != null ? String.valueOf(level) : "?";
            String xpText = xp != null ? NUMBER_FORMAT.format(xp) + " XP" : "";

            String text = "\u00a77" + skill + " \u00a7f" + levelText;
            if (!xpText.isEmpty()) {
                text += " \u00a78(" + xpText + ")";
            }

            context.text(font, text, x, rowY, GOLD_COLOR, false);
            rowY += font.lineHeight + 4;
        }
    }

    private void renderInventoryTab(GuiGraphicsExtractor context, int x, int y) {
        java.util.List<ProfileData.InventoryItem> inventory = profile.getInventory();
        java.util.List<ProfileData.InventoryItem> armor = profile.getArmor();
        java.util.List<ProfileData.InventoryItem> accessories = profile.getAccessories();

        if (inventory.isEmpty() && armor.isEmpty() && accessories.isEmpty()) {
            context.text(font, "\u00a77No inventory data available", x, y, GRAY_COLOR, false);
            return;
        }

        int rowY = y;

        if (!armor.isEmpty()) {
            context.text(font, "\u00a76Armor:", x, rowY, GOLD_COLOR, true);
            rowY += font.lineHeight + 2;
            for (ProfileData.InventoryItem item : armor) {
                if (item.getInternalName() == null || item.getInternalName().isEmpty()) continue;
                String name = item.getDisplayName() != null ? TextUtils.stripColorCodes(item.getDisplayName()) : item.getInternalName();
                String rarity = item.getRarity() != null ? " \u00a78[" + item.getRarity() + "]" : "";
                context.text(font, "\u00a7f- " + name + rarity, x + 8, rowY, TEXT_COLOR, false);
                rowY += font.lineHeight + 1;
            }
            rowY += 4;
        }

        if (!accessories.isEmpty()) {
            context.text(font, "\u00a76Accessories:", x, rowY, GOLD_COLOR, true);
            rowY += font.lineHeight + 2;
            for (ProfileData.InventoryItem item : accessories) {
                if (item.getInternalName() == null || item.getInternalName().isEmpty()) continue;
                String name = item.getDisplayName() != null ? TextUtils.stripColorCodes(item.getDisplayName()) : item.getInternalName();
                context.text(font, "\u00a7f- " + name, x + 8, rowY, TEXT_COLOR, false);
                rowY += font.lineHeight + 1;
            }
            rowY += 4;
        }

        if (!inventory.isEmpty()) {
            context.text(font, "\u00a76Inventory: \u00a77" + inventory.size() + " items", x, rowY, GOLD_COLOR, true);
        }
    }

    private void renderPetsTab(GuiGraphicsExtractor context, int x, int y) {
        java.util.List<ProfileData.PetData> pets = profile.getPets();
        if (pets.isEmpty()) {
            context.text(font, "\u00a77No pet data available", x, y, GRAY_COLOR, false);
            return;
        }

        int rowY = y;
        context.text(font, "\u00a76Pets: \u00a77" + pets.size(), x, rowY, GOLD_COLOR, true);
        rowY += font.lineHeight + 4;

        for (ProfileData.PetData pet : pets) {
            String rarityColor = TextUtils.getRarityColor(pet.getRarity());
            String text = rarityColor + pet.getName() + " \u00a77Lvl " + pet.getLevel();
            if (pet.getHeldItem() != null) {
                text += " \u00a78[" + pet.getHeldItem() + "]";
            }
            context.text(font, text, x + 4, rowY, TEXT_COLOR, false);
            rowY += font.lineHeight + 2;
        }
    }

    private void renderCollectionsTab(GuiGraphicsExtractor context, int x, int y) {
        Map<String, Long> collections = profile.getCollections();
        if (collections.isEmpty()) {
            context.text(font, "\u00a77No collection data available", x, y, GRAY_COLOR, false);
            return;
        }

        int rowY = y;
        context.text(font, "\u00a76Collections: \u00a77" + collections.size(), x, rowY, GOLD_COLOR, true);
        rowY += font.lineHeight + 4;

        List<Map.Entry<String, Long>> sorted = new ArrayList<>(collections.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Long> entry : sorted) {
            String text = "\u00a7f" + TextUtils.capitalize(entry.getKey()) + ": \u00a77" + NUMBER_FORMAT.format(entry.getValue());
            context.text(font, text, x + 4, rowY, TEXT_COLOR, false);
            rowY += font.lineHeight + 1;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean containerInteract) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (mouseY >= guiTop + 30 && mouseY < guiTop + 50) {
            int tabWidth = guiWidth / TAB_NAMES.length;
            int tab = (int) ((mouseX - guiLeft) / tabWidth);
            if (tab >= 0 && tab < TAB_NAMES.length) {
                currentTab = tab;
                scrollOffset = 0;
                return true;
            }
        }
        return super.mouseClicked(event, containerInteract);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = Math.max(0, scrollOffset - (int) verticalAmount * 10);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT) {
            currentTab = Math.max(0, currentTab - 1);
            scrollOffset = 0;
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
            currentTab = Math.min(TAB_NAMES.length - 1, currentTab + 1);
            scrollOffset = 0;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
