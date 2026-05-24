package io.github.legentpc.neu21plus.client.actionbar;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionBarDisplay {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionBarDisplay.class);

    private static final ActionBarDisplay INSTANCE = new ActionBarDisplay();

    public static ActionBarDisplay getInstance() {
        return INSTANCE;
    }

    private static final Pattern HEALTH_PATTERN = Pattern.compile(".*[\\u2764\\u2665].*?(\\d+)/(\\d+).*");
    private static final Pattern DEFENSE_PATTERN = Pattern.compile(".*[\\u2764\\u2665].*?Def.*?(\\d+).*");
    private static final Pattern MANA_PATTERN = Pattern.compile(".*[\\u2747\\u2744].*?(\\d+)/(\\d+).*");

    private int currentHealth = 0;
    private int maxHealth = 0;
    private int defense = 0;
    private int currentMana = 0;
    private int maxMana = 0;
    private String lastActionBar = "";

    private ActionBarDisplay() {
    }

    public void onActionBar(Component message) {
        String text = message.getString();
        if (text.equals(lastActionBar)) return;
        lastActionBar = text;

        Matcher healthMatcher = HEALTH_PATTERN.matcher(text);
        if (healthMatcher.find()) {
            try {
                currentHealth = Integer.parseInt(healthMatcher.group(1));
                maxHealth = Integer.parseInt(healthMatcher.group(2));
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher defenseMatcher = DEFENSE_PATTERN.matcher(text);
        if (defenseMatcher.find()) {
            try {
                defense = Integer.parseInt(defenseMatcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher manaMatcher = MANA_PATTERN.matcher(text);
        if (manaMatcher.find()) {
            try {
                currentMana = Integer.parseInt(manaMatcher.group(1));
                maxMana = Integer.parseInt(manaMatcher.group(2));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.display.actionBarDisplay) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        int x = screenWidth / 2;
        int y = screenHeight - 55;

        int barWidth = 182;
        int barHeight = 5;

        if (maxHealth > 0) {
            renderBar(context, x - barWidth / 2, y, barWidth, barHeight,
                    currentHealth, maxHealth, 0xFFFF3333, 0xFF551111, "\u00a7c" + currentHealth + "/" + maxHealth + " HP");
        }

        if (maxMana > 0) {
            renderBar(context, x - barWidth / 2, y + barHeight + 3, barWidth, barHeight,
                    currentMana, maxMana, 0xFF3333FF, 0xFF111155, "\u00a79" + currentMana + "/" + maxMana + " Mana");
        }

        if (defense > 0) {
            String defText = "\u00a77Def: \u00a7f" + defense;
            context.text(client.font, defText, x - client.font.width(defText) / 2, y + (barHeight + 3) * 2 + 1, 0xFFAAAAAA, false);
        }
    }

    private void renderBar(GuiGraphicsExtractor context, int x, int y, int width, int height,
                           int current, int max, int fillColor, int bgColor, String text) {
        context.fill(x, y, x + width, y + height, bgColor);

        if (max > 0) {
            int fillWidth = (int) ((current / (double) max) * width);
            fillWidth = Math.max(0, Math.min(fillWidth, width));
            context.fill(x, y, x + fillWidth, y + height, fillColor);
        }

        context.outline(x, y, width, height, 0xFF333333);

        Minecraft client = Minecraft.getInstance();
        context.text(client.font, text, x + width / 2 - client.font.width(text) / 2, y - client.font.lineHeight - 1, 0xFFE0E0E0, true);
    }

    public void reset() {
        currentHealth = 0;
        maxHealth = 0;
        defense = 0;
        currentMana = 0;
        maxMana = 0;
        lastActionBar = "";
    }
}
