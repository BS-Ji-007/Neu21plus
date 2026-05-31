package io.github.legentpc.neu21plus.client.dungeon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class DungeonRoom {

    private static final Pattern ROOM_NAME_PATTERN = Pattern.compile("\\[([A-Z])] (.+)");
    private static final Pattern PUZZLE_PATTERN = Pattern.compile("(?i)(trivia|blaze|creeper|waterboard|ice|silverfish|boulder|teleport)");

    private RoomType type;
    private RoomColor color;
    private String name;
    private int x;
    private int y;
    private boolean completed;
    private boolean current;
    private boolean puzzleSolved;

    public DungeonRoom(int x, int y) {
        this.x = x;
        this.y = y;
        this.type = RoomType.UNKNOWN;
        this.color = RoomColor.GRAY;
        this.name = "";
        this.completed = false;
        this.current = false;
        this.puzzleSolved = false;
    }

    public DungeonRoom(int x, int y, @NotNull RoomType type, @NotNull RoomColor color, @NotNull String name) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.color = color;
        this.name = name;
        this.completed = false;
        this.current = false;
        this.puzzleSolved = false;
    }

    public void updateFromMapChar(char mapChar) {
        this.color = RoomColor.fromMapChar(mapChar);
        this.type = inferTypeFromColor(this.color);
        if (this.color == RoomColor.GREEN || this.color == RoomColor.PINK) {
            this.completed = true;
        }
    }

    private RoomType inferTypeFromColor(RoomColor roomColor) {
        return switch (roomColor) {
            case GREEN, PINK -> {
                if (isPuzzleName(name)) yield RoomType.PUZZLE;
                yield RoomType.COMPLETED;
            }
            case RED -> RoomType.COMBAT;
            case ORANGE -> RoomType.COMBAT;
            case PURPLE -> RoomType.BLOOD;
            case YELLOW -> RoomType.TRAP;
            case BROWN -> RoomType.ENTRANCE;
            case BLUE -> RoomType.MINIBOSS;
            case WHITE -> RoomType.WITHER;
            case GRAY -> RoomType.UNEXPLORED;
            default -> RoomType.UNKNOWN;
        };
    }

    public boolean isPuzzleName(@Nullable String roomName) {
        if (roomName == null || roomName.isEmpty()) return false;
        return PUZZLE_PATTERN.matcher(roomName).find();
    }

    public int getMapColor() {
        return color.getRgb();
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public RoomColor getColor() {
        return color;
    }

    public void setColor(RoomColor color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
        if (isPuzzleName(this.name)) {
            this.type = RoomType.PUZZLE;
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isPuzzleSolved() {
        return puzzleSolved;
    }

    public void setPuzzleSolved(boolean puzzleSolved) {
        this.puzzleSolved = puzzleSolved;
    }

    public enum RoomType {
        UNKNOWN,
        ENTRANCE,
        COMBAT,
        PUZZLE,
        TRAP,
        MINIBOSS,
        BLOOD,
        WITHER,
        COMPLETED,
        FAIRY,
        SHOP,
        UNEXPLORED
    }

    public enum RoomColor {
        GRAY(0x404040, ' '),
        BROWN(0x6B4226, 'B'),
        GREEN(0x00AA00, 'G'),
        RED(0xFF0000, 'R'),
        ORANGE(0xFF6600, 'O'),
        PURPLE(0xAA00AA, 'P'),
        YELLOW(0xFFFF00, 'Y'),
        BLUE(0x5555FF, 'L'),
        PINK(0xFF69B4, 'K'),
        WHITE(0xFFFFFF, 'W'),
        BLACK(0x000000, 'X'),
        CYAN(0x00FFFF, 'C');

        private final int rgb;
        private final char mapChar;

        RoomColor(int rgb, char mapChar) {
            this.rgb = rgb;
            this.mapChar = mapChar;
        }

        public int getRgb() {
            return rgb;
        }

        public char getMapChar() {
            return mapChar;
        }

        public static RoomColor fromMapChar(char c) {
            for (RoomColor color : values()) {
                if (color.mapChar == c) return color;
            }
            return GRAY;
        }

        public static RoomColor fromName(String name) {
            if (name == null) return GRAY;
            return switch (name.toUpperCase()) {
                case "ENTRANCE" -> BROWN;
                case "BLOOD" -> PURPLE;
                case "PUZZLE" -> GREEN;
                case "TRAP" -> YELLOW;
                case "MINIBOSS" -> BLUE;
                case "WITHER" -> WHITE;
                case "FAIRY" -> PINK;
                case "SHOP" -> CYAN;
                default -> GRAY;
            };
        }
    }
}
