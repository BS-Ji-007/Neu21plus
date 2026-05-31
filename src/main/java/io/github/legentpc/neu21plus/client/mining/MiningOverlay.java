package io.github.legentpc.neu21plus.client.mining;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiningOverlay {

    private static final Logger LOGGER = LoggerFactory.getLogger(MiningOverlay.class);

    private static final MiningOverlay INSTANCE = new MiningOverlay();

    private static final Pattern MITHRIL_POWDER_PATTERN = Pattern.compile("Mithril: ([\\d,]+)");
    private static final Pattern GEMSTONE_POWDER_PATTERN = Pattern.compile("Gemstone: ([\\d,]+)");
    private static final Pattern GLACITE_POWDER_PATTERN = Pattern.compile("Glacite: ([\\d,]+)");
    private static final Pattern HOTM_LEVEL_PATTERN = Pattern.compile("(?:HOTM|Heart of the Mountain) (?:Level|Lvl)? ?(\\d+)");
    private static final Pattern HOTM_TOKENS_PATTERN = Pattern.compile("(?:Tokens|Available): ([\\d,]+)");
    private static final Pattern COMMISSION_PATTERN = Pattern.compile("(.+?)(?:\\s+(\\d+)%\\s*(?:Done|Complete|done|complete)?)?");

    private static final int PANEL_WIDTH = 150;
    private static final int PANEL_PADDING = 4;
    private static final int LINE_HEIGHT = 10;

    public static MiningOverlay getInstance() {
        return INSTANCE;
    }

    private int mithrilPowder = 0;
    private int gemstonePowder = 0;
    private int glacitePowder = 0;
    private int hotmLevel = 0;
    private int hotmTokens = 0;
    private CommissionSlot[] commissions = new CommissionSlot[2];
    private int tickCount = 0;
    private boolean inMiningArea = false;
    private long lastScoreboardUpdate = -1;

    private int mithrilPowderDelta = 0;
    private int gemstonePowderDelta = 0;
    private int glacitePowderDelta = 0;
    private long deltaResetTime = -1;

    public static class CommissionSlot {
        private String name = "";
        private int progress = 0;
        private CommissionState state = CommissionState.NOT_STARTED;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name != null ? name : "";
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = Math.max(0, Math.min(100, progress));
        }

        public CommissionState getState() {
            return state;
        }

        public void setState(CommissionState state) {
            this.state = state;
        }
    }

    public enum CommissionState {
        NOT_STARTED(0xFFAAAAAA),
        IN_PROGRESS(0xFFFFFF55),
        COMPLETED(0xFF55FF55);

        private final int color;

        CommissionState(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    private MiningOverlay() {
        commissions[0] = new CommissionSlot();
        commissions[1] = new CommissionSlot();
    }

    public void tick() {
        tickCount++;

        if (deltaResetTime > 0 && System.currentTimeMillis() - deltaResetTime > 30000) {
            mithrilPowderDelta = 0;
            gemstonePowderDelta = 0;
            glacitePowderDelta = 0;
            deltaResetTime = -1;
        }
    }

    public void onMiningAreaEnter() {
        inMiningArea = true;
        resetDeltas();
    }

    public void parseScoreboard(Minecraft client) {
        if (client.level == null || client.player == null) return;

        try {
            Scoreboard scoreboard = client.level.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
            if (objective == null) return;

            List<PlayerScoreEntry> scores = new ArrayList<>(scoreboard.listPlayerScores(objective));
            scores.sort(Comparator.<PlayerScoreEntry>comparingInt(PlayerScoreEntry::value).reversed());

            int commissionIndex = 0;

            for (PlayerScoreEntry entry : scores) {
                String line = entry.display() != null ? entry.display().getString() : entry.owner();
                String cleaned = TextUtils.stripColorCodes(line).trim();

                Matcher mithrilMatcher = MITHRIL_POWDER_PATTERN.matcher(cleaned);
                if (mithrilMatcher.find()) {
                    mithrilPowder = parseFormattedNumber(mithrilMatcher.group(1));
                    continue;
                }

                Matcher gemstoneMatcher = GEMSTONE_POWDER_PATTERN.matcher(cleaned);
                if (gemstoneMatcher.find()) {
                    gemstonePowder = parseFormattedNumber(gemstoneMatcher.group(1));
                    continue;
                }

                Matcher glaciteMatcher = GLACITE_POWDER_PATTERN.matcher(cleaned);
                if (glaciteMatcher.find()) {
                    glacitePowder = parseFormattedNumber(glaciteMatcher.group(1));
                    continue;
                }

                Matcher hotmMatcher = HOTM_LEVEL_PATTERN.matcher(cleaned);
                if (hotmMatcher.find()) {
                    hotmLevel = TextUtils.parseIntSafe(hotmMatcher.group(1), hotmLevel);
                    continue;
                }

                Matcher tokenMatcher = HOTM_TOKENS_PATTERN.matcher(cleaned);
                if (tokenMatcher.find()) {
                    hotmTokens = parseFormattedNumber(tokenMatcher.group(1));
                    continue;
                }

                if (cleaned.toLowerCase().contains("commission") && commissionIndex < 2) {
                    parseCommissionLine(cleaned, commissionIndex);
                    commissionIndex++;
                }
            }

            lastScoreboardUpdate = System.currentTimeMillis();
        } catch (Exception e) {
            LOGGER.debug("Failed to parse mining scoreboard", e);
        }
    }

    private void parseCommissionLine(String line, int index) {
        if (index < 0 || index >= commissions.length) return;

        CommissionSlot slot = commissions[index];
        String cleanLine = line.replaceAll("(?i)commission[:\\s]*", "").trim();

        if (cleanLine.contains("DONE") || cleanLine.contains("COMPLETE") || cleanLine.contains("\u2714")) {
            slot.setState(CommissionState.COMPLETED);
            slot.setProgress(100);
            String namePart = cleanLine.split("(?i)(?:DONE|COMPLETE|\u2714)")[0].trim();
            slot.setName(namePart.isEmpty() ? cleanLine : namePart);
        } else {
            java.util.regex.Pattern progressPattern = java.util.regex.Pattern.compile("(\\d+)%");
            Matcher progressMatcher = progressPattern.matcher(cleanLine);
            if (progressMatcher.find()) {
                int progress = TextUtils.parseIntSafe(progressMatcher.group(1), -1);
                if (progress >= 0) {
                    slot.setProgress(progress);
                    slot.setState(progress >= 100 ? CommissionState.COMPLETED : CommissionState.IN_PROGRESS);
                } else {
                    slot.setState(CommissionState.NOT_STARTED);
                }
            } else {
                slot.setState(CommissionState.NOT_STARTED);
            }

            String namePart = cleanLine.replaceAll("\\d+%", "").trim();
            slot.setName(namePart.isEmpty() ? cleanLine : namePart);
        }
    }

    private int parseFormattedNumber(String text) {
        if (text == null || text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text.replace(",", "").replace(".", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void addMithrilPowder(int amount) {
        mithrilPowderDelta += amount;
        deltaResetTime = System.currentTimeMillis();
    }

    public void addGemstonePowder(int amount) {
        gemstonePowderDelta += amount;
        deltaResetTime = System.currentTimeMillis();
    }

    public void addGlacitePowder(int amount) {
        glacitePowderDelta += amount;
        deltaResetTime = System.currentTimeMillis();
    }

    private void resetDeltas() {
        mithrilPowderDelta = 0;
        gemstonePowderDelta = 0;
        glacitePowderDelta = 0;
        deltaResetTime = -1;
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.mining.miningOverlay) return;
        if (!inMiningArea) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        List<OverlayLine> lines = buildOverlayLines();

        if (lines.isEmpty()) return;

        int panelHeight = PANEL_PADDING * 2 + lines.size() * LINE_HEIGHT;
        int panelX = screenWidth - PANEL_WIDTH - 4;
        int panelY = 4;

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, 0x90000000);

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, 0xFF55FFFF);
        context.fill(panelX, panelY + panelHeight - 1, panelX + PANEL_WIDTH, panelY + panelHeight, 0xFF55FFFF);

        int textY = panelY + PANEL_PADDING;
        for (OverlayLine line : lines) {
            context.text(client.font, line.text(), panelX + PANEL_PADDING, textY, line.color(), true);
            textY += LINE_HEIGHT;
        }
    }

    private List<OverlayLine> buildOverlayLines() {
        List<OverlayLine> lines = new ArrayList<>();

        lines.add(new OverlayLine("\u00a7bMining Info", 0xFF55FFFF));

        if (mithrilPowder > 0 || mithrilPowderDelta > 0) {
            String mithrilText = "\u00a77Mithril: \u00a7a" + TextUtils.formatNumber(mithrilPowder);
            if (mithrilPowderDelta > 0) {
                mithrilText += " \u00a7e(+" + TextUtils.formatNumber(mithrilPowderDelta) + ")";
            }
            lines.add(new OverlayLine(mithrilText, 0xFF55FF55));
        }

        if (gemstonePowder > 0 || gemstonePowderDelta > 0) {
            String gemstoneText = "\u00a77Gemstone: \u00a7d" + TextUtils.formatNumber(gemstonePowder);
            if (gemstonePowderDelta > 0) {
                gemstoneText += " \u00a7e(+" + TextUtils.formatNumber(gemstonePowderDelta) + ")";
            }
            lines.add(new OverlayLine(gemstoneText, 0xFF55FFFF));
        }

        if (glacitePowder > 0 || glacitePowderDelta > 0) {
            String glaciteText = "\u00a77Glacite: \u00a7b" + TextUtils.formatNumber(glacitePowder);
            if (glacitePowderDelta > 0) {
                glaciteText += " \u00a7e(+" + TextUtils.formatNumber(glacitePowderDelta) + ")";
            }
            lines.add(new OverlayLine(glaciteText, 0xFF55FFFF));
        }

        if (hotmLevel > 0) {
            String hotmText = "\u00a76HOTM: \u00a7fLevel " + hotmLevel;
            if (hotmTokens > 0) {
                hotmText += " \u00a7e(" + hotmTokens + " tokens)";
            }
            lines.add(new OverlayLine(hotmText, 0xFFFFAA00));
        }

        for (int i = 0; i < commissions.length; i++) {
            CommissionSlot slot = commissions[i];
            if (slot.getName().isEmpty()) continue;

            String stateSymbol = switch (slot.getState()) {
                case COMPLETED -> "\u00a7a\u2714";
                case IN_PROGRESS -> "\u00a7e\u25B6";
                case NOT_STARTED -> "\u00a78\u25CB";
            };

            String commText = stateSymbol + " \u00a7f" + truncateCommissionName(slot.getName());
            if (slot.getState() == CommissionState.IN_PROGRESS && slot.getProgress() > 0) {
                commText += " \u00a7e" + slot.getProgress() + "%";
            }
            lines.add(new OverlayLine(commText, slot.getState().getColor()));
        }

        return lines;
    }

    private String truncateCommissionName(String name) {
        if (name.length() > 20) {
            return name.substring(0, 17) + "...";
        }
        return name;
    }

    public void reset() {
        mithrilPowder = 0;
        gemstonePowder = 0;
        glacitePowder = 0;
        hotmLevel = 0;
        hotmTokens = 0;
        commissions[0] = new CommissionSlot();
        commissions[1] = new CommissionSlot();
        tickCount = 0;
        inMiningArea = false;
        lastScoreboardUpdate = -1;
        resetDeltas();
    }

    public int getMithrilPowder() {
        return mithrilPowder;
    }

    public int getGemstonePowder() {
        return gemstonePowder;
    }

    public int getGlacitePowder() {
        return glacitePowder;
    }

    public int getHotmLevel() {
        return hotmLevel;
    }

    public int getHotmTokens() {
        return hotmTokens;
    }

    public CommissionSlot getCommission(int index) {
        if (index < 0 || index >= commissions.length) return null;
        return commissions[index];
    }

    private record OverlayLine(String text, int color) {
    }
}
