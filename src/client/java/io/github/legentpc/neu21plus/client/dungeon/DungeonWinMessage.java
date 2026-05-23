package io.github.legentpc.neu21plus.client.dungeon;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonWinMessage {

    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonWinMessage.class);

    private static final DungeonWinMessage INSTANCE = new DungeonWinMessage();

    private static final Pattern DUNGEON_COMPLETE_PATTERN = Pattern.compile("Dungeon Cleared: (?:(Master )?Catacombs )?(?:Floor |F)([A-Z0-9]+)");
    private static final Pattern TIME_PATTERN = Pattern.compile("Time: (\\d+)m?(\\d+)s?");
    private static final Pattern SCORE_FINAL_PATTERN = Pattern.compile("Score: (\\d+)");
    private static final Pattern WITH_PATTERN = Pattern.compile("(\\w+) With: (.+)");

    private static final int DISPLAY_DURATION = 15000;
    private static final int FADE_DURATION = 2000;

    public static DungeonWinMessage getInstance() {
        return INSTANCE;
    }

    private boolean displaying = false;
    private long displayStartTime = -1;
    private String floor = "";
    private int finalScore = 0;
    private DungeonScore.ScoreGrade finalGrade;
    private long completionTime = 0;
    private String withPlayers = "";
    private int secretsFound = 0;
    private int secretsTotal = 0;
    private boolean milestone = false;

    private String cachedMessage = "";
    private int cachedMessageWidth = 0;

    private DungeonWinMessage() {
    }

    public void reset() {
        displaying = false;
        displayStartTime = -1;
        floor = "";
        finalScore = 0;
        finalGrade = DungeonScore.ScoreGrade.UNKNOWN;
        completionTime = 0;
        withPlayers = "";
        secretsFound = 0;
        secretsTotal = 0;
        milestone = false;
        cachedMessage = "";
        cachedMessageWidth = 0;
    }

    public boolean parseChatMessage(Component message) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.dungeons.dungeonWinMessage) return false;

        String text = message.getString();
        String cleaned = TextUtils.stripColorCodes(text).trim();

        Matcher completeMatcher = DUNGEON_COMPLETE_PATTERN.matcher(cleaned);
        if (completeMatcher.find()) {
            floor = completeMatcher.group(2);
            onDungeonComplete(cleaned);
            return true;
        }

        if (displaying) {
            parseAdditionalInfo(cleaned);
        }

        return false;
    }

    private void onDungeonComplete(String text) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        DungeonScore score = DungeonFeatures.getInstance().getScore();
        finalScore = score.getTotalScore();
        finalGrade = score.getGrade();
        secretsFound = score.getSecretCount();
        secretsTotal = score.getSecretTotal();
        completionTime = DungeonMap.getInstance().getDungeonElapsedTime();
        milestone = score.isMilestoneReached();

        displaying = true;
        displayStartTime = System.currentTimeMillis();

        buildDisplayMessage();

        playCompletionSound();

        LOGGER.info("Dungeon complete! Floor {} - Score {} - Grade {}", floor, finalScore, finalGrade.getDisplay());
    }

    private void parseAdditionalInfo(String text) {
        Matcher timeMatcher = TIME_PATTERN.matcher(text);
        if (timeMatcher.find()) {
            try {
                int minutes = timeMatcher.group(1) != null && !timeMatcher.group(1).isEmpty() ? Integer.parseInt(timeMatcher.group(1)) : 0;
                int seconds = Integer.parseInt(timeMatcher.group(2));
                completionTime = (minutes * 60 + seconds) * 1000L;
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher scoreMatcher = SCORE_FINAL_PATTERN.matcher(text);
        if (scoreMatcher.find()) {
            try {
                finalScore = Integer.parseInt(scoreMatcher.group(1));
                finalGrade = DungeonScore.ScoreGrade.fromScore(finalScore);
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher withMatcher = WITH_PATTERN.matcher(text);
        if (withMatcher.find()) {
            withPlayers = withMatcher.group(2).trim();
        }

        buildDisplayMessage();
    }

    private void buildDisplayMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(finalGrade.getDisplay());
        sb.append(" \u00a77Floor ").append(floor);

        if (completionTime > 0) {
            long seconds = completionTime / 1000;
            long mins = seconds / 60;
            long secs = seconds % 60;
            sb.append(" \u00a78(").append(mins).append("m ").append(secs).append("s)");
        }

        sb.append(" \u00a7eScore: ").append(finalScore);

        if (secretsTotal > 0) {
            sb.append(" \u00a7aSecrets: ").append(secretsFound).append("/").append(secretsTotal);
        }

        if (milestone) {
            sb.append(" \u00a7d Milestone!");
        }

        if (!withPlayers.isEmpty()) {
            sb.append(" \u00a77With: ").append(withPlayers);
        }

        cachedMessage = sb.toString();

        Minecraft client = Minecraft.getInstance();
        if (client.font != null) {
            cachedMessageWidth = client.font.width(TextUtils.stripColorCodes(cachedMessage));
        }
    }

    public void render(GuiGraphicsExtractor context) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.dungeons.dungeonWinMessage) return;
        if (!displaying) return;

        long elapsed = System.currentTimeMillis() - displayStartTime;
        if (elapsed > DISPLAY_DURATION) {
            displaying = false;
            return;
        }

        float alpha = 1.0f;
        if (elapsed < FADE_DURATION) {
            alpha = (float) elapsed / FADE_DURATION;
        } else if (elapsed > DISPLAY_DURATION - FADE_DURATION) {
            alpha = (float) (DISPLAY_DURATION - elapsed) / FADE_DURATION;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int y = client.getWindow().getGuiScaledHeight() / 4;

        int boxWidth = cachedMessageWidth + 24;
        int boxHeight = 28;
        int boxX = (screenWidth - boxWidth) / 2;

        int bgColor = applyAlpha(0xD0000000, alpha);
        int borderColor = applyAlpha(getGradeBorderColor(), alpha);

        context.fill(boxX, y, boxX + boxWidth, y + boxHeight, bgColor);
        context.outline(boxX, y, boxWidth, boxHeight, borderColor);

        int gradeColor = applyAlpha(finalGrade.getColor(), alpha);
        String gradeText = finalGrade.getDisplay().replaceAll("\u00a7.", "");
        context.text(client.font, gradeText, boxX + 8, y + boxHeight / 2 - client.font.lineHeight / 2, gradeColor, true);

        String infoText = TextUtils.stripColorCodes(cachedMessage);
        int textX = boxX + 8 + client.font.width(gradeText) + 8;
        int textColor = applyAlpha(0xFFFFFFFF, alpha);
        context.text(client.font, infoText, textX, y + boxHeight / 2 - client.font.lineHeight / 2, textColor, true);
    }

    private int getGradeBorderColor() {
        return switch (finalGrade) {
            case S_PLUS -> 0xFFFFFF00;
            case S -> 0xFF55FF55;
            case A -> 0xFF55FFFF;
            case B -> 0xFFAAAAAA;
            case C -> 0xFF777777;
            default -> 0xFF444444;
        };
    }

    private int applyAlpha(int color, float alpha) {
        int a = (int) (alpha * 255);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private void playCompletionSound() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        client.player.playSound(
                finalGrade == DungeonScore.ScoreGrade.S_PLUS
                        ? SoundEvents.PLAYER_LEVELUP
                        : SoundEvents.EXPERIENCE_ORB_PICKUP,
                1.0f, finalGrade == DungeonScore.ScoreGrade.S_PLUS ? 1.2f : 1.0f
        );
    }

    public boolean isDisplaying() {
        return displaying;
    }

    public DungeonScore.ScoreGrade getFinalGrade() {
        return finalGrade;
    }

    public int getFinalScore() {
        return finalScore;
    }
}
