package io.github.legentpc.neu21plus.client.dungeon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonScore {

    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonScore.class);

    private static final Pattern SCORE_LINE_PATTERN = Pattern.compile("Score: (\\d+)");
    private static final Pattern SECRET_BONUS_PATTERN = Pattern.compile("\\+([\\d.]+) Score \\(Secrets\\)");
    private static final Pattern EXPLORATION_PATTERN = Pattern.compile("Exploration: (\\d+)");
    private static final Pattern SPEED_PATTERN = Pattern.compile("Speed: (\\d+)");
    private static final Pattern SKILL_PATTERN = Pattern.compile("Skill: (\\d+)");
    private static final Pattern BONUS_PATTERN = Pattern.compile("Bonus: (\\d+)");

    private int explorationScore;
    private int speedScore;
    private int skillScore;
    private int bonusScore;
    private double secretBonus;
    private int totalScore;
    private ScoreGrade grade;
    private boolean milestoneReached;
    private int milestoneTarget;

    private int secretCount;
    private int secretTotal;
    private int deathCount;
    private int puzzleCount;
    private int puzzleTotal;
    private boolean trapCleared;
    private boolean bloodCleared;
    private boolean witherKillsComplete;

    public DungeonScore() {
        reset();
    }

    public void reset() {
        explorationScore = 0;
        speedScore = 0;
        skillScore = 0;
        bonusScore = 0;
        secretBonus = 0;
        totalScore = 0;
        grade = ScoreGrade.UNKNOWN;
        milestoneReached = false;
        milestoneTarget = 300;
        secretCount = 0;
        secretTotal = 0;
        deathCount = 0;
        puzzleCount = 0;
        puzzleTotal = 0;
        trapCleared = false;
        bloodCleared = false;
        witherKillsComplete = false;
    }

    public void parseScoreboardLine(String line) {
        if (line == null || line.isEmpty()) return;

        Matcher scoreMatcher = SCORE_LINE_PATTERN.matcher(line);
        if (scoreMatcher.find()) {
            totalScore = Integer.parseInt(scoreMatcher.group(1));
            updateGrade();
            checkMilestone();
        }

        Matcher secretMatcher = SECRET_BONUS_PATTERN.matcher(line);
        if (secretMatcher.find()) {
            secretBonus = Double.parseDouble(secretMatcher.group(1));
        }

        Matcher explorationMatcher = EXPLORATION_PATTERN.matcher(line);
        if (explorationMatcher.find()) {
            explorationScore = Integer.parseInt(explorationMatcher.group(1));
        }

        Matcher speedMatcher = SPEED_PATTERN.matcher(line);
        if (speedMatcher.find()) {
            speedScore = Integer.parseInt(speedMatcher.group(1));
        }

        Matcher skillMatcher = SKILL_PATTERN.matcher(line);
        if (skillMatcher.find()) {
            skillScore = Integer.parseInt(skillMatcher.group(1));
        }

        Matcher bonusMatcher = BONUS_PATTERN.matcher(line);
        if (bonusMatcher.find()) {
            bonusScore = Integer.parseInt(bonusMatcher.group(1));
        }
    }

    public void onDeath() {
        deathCount++;
    }

    public void onSecretFound() {
        secretCount++;
    }

    public void onPuzzleSolved() {
        puzzleCount++;
    }

    public void onTrapCleared() {
        trapCleared = true;
    }

    public void onBloodCleared() {
        bloodCleared = true;
    }

    public void onWitherKillsComplete() {
        witherKillsComplete = true;
    }

    public int calculateEstimatedScore() {
        int base = explorationScore + speedScore + skillScore + bonusScore;
        int secretPortion = (int) secretBonus;
        return base + secretPortion;
    }

    private void updateGrade() {
        grade = ScoreGrade.fromScore(totalScore);
    }

    private void checkMilestone() {
        if (totalScore >= milestoneTarget) {
            milestoneReached = true;
        }
    }

    public int getRemainingForMilestone() {
        return Math.max(0, milestoneTarget - totalScore);
    }

    public int getRemainingForGrade(ScoreGrade target) {
        return Math.max(0, target.getMinScore() - totalScore);
    }

    public ScoreGrade getGrade() {
        return grade;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getExplorationScore() {
        return explorationScore;
    }

    public int getSpeedScore() {
        return speedScore;
    }

    public int getSkillScore() {
        return skillScore;
    }

    public int getBonusScore() {
        return bonusScore;
    }

    public double getSecretBonus() {
        return secretBonus;
    }

    public boolean isMilestoneReached() {
        return milestoneReached;
    }

    public int getMilestoneTarget() {
        return milestoneTarget;
    }

    public void setMilestoneTarget(int target) {
        this.milestoneTarget = target;
    }

    public int getSecretCount() {
        return secretCount;
    }

    public void setSecretCount(int count) {
        this.secretCount = count;
    }

    public int getSecretTotal() {
        return secretTotal;
    }

    public void setSecretTotal(int total) {
        this.secretTotal = total;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public int getPuzzleCount() {
        return puzzleCount;
    }

    public int getPuzzleTotal() {
        return puzzleTotal;
    }

    public void setPuzzleTotal(int total) {
        this.puzzleTotal = total;
    }

    public boolean isTrapCleared() {
        return trapCleared;
    }

    public boolean isBloodCleared() {
        return bloodCleared;
    }

    public boolean isWitherKillsComplete() {
        return witherKillsComplete;
    }

    public enum ScoreGrade {
        S_PLUS(300, "\u00a76S+", 0xFF55FF55),
        S(270, "\u00a7aS", 0xFF55FF55),
        A(230, "\u00a7eA", 0xFFFFFF55),
        B(180, "\u00a77B", 0xFFAAAAAA),
        C(140, "\u00a7fC", 0xFFAAAAAA),
        D(0, "\u00a78D", 0xFF666666),
        UNKNOWN(-1, "\u00a78?", 0xFF666666);

        private final int minScore;
        private final String display;
        private final int color;

        ScoreGrade(int minScore, String display, int color) {
            this.minScore = minScore;
            this.display = display;
            this.color = color;
        }

        public int getMinScore() {
            return minScore;
        }

        public String getDisplay() {
            return display;
        }

        public int getColor() {
            return color;
        }

        public static ScoreGrade fromScore(int score) {
            if (score >= S_PLUS.minScore) return S_PLUS;
            if (score >= S.minScore) return S;
            if (score >= A.minScore) return A;
            if (score >= B.minScore) return B;
            if (score >= C.minScore) return C;
            if (score >= D.minScore) return D;
            return UNKNOWN;
        }
    }
}
