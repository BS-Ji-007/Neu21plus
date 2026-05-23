package io.github.legentpc.neu21plus.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextUtils {

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("[\u00a7&][0-9a-fk-orA-FK-OR]");

    public static String stripColorCodes(String text) {
        if (text == null) return "";
        return COLOR_CODE_PATTERN.matcher(text).replaceAll("");
    }

    public static String removeDuplicateSpaces(String text) {
        if (text == null) return "";
        return text.replaceAll(" +", " ").trim();
    }

    public static String cleanColor(String text) {
        return stripColorCodes(text);
    }

    public static String getCleanName(String text) {
        return removeDuplicateSpaces(stripColorCodes(text));
    }

    public static List<String> wrapText(String text, int maxCharsPerLine) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxCharsPerLine && currentLine.length() > 0) {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder();
            }
            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }

        return lines;
    }

    public static String formatNumber(double number) {
        if (number >= 1_000_000_000) {
            return String.format("%.1fB", number / 1_000_000_000);
        } else if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000);
        }
        return String.format("%.0f", number);
    }

    public static String formatNumberWithCommas(long number) {
        return String.format("%,d", number);
    }

    public static String getRarityColor(String rarity) {
        if (rarity == null) return "\u00a7f";
        return switch (rarity.toUpperCase()) {
            case "COMMON" -> "\u00a7f";
            case "UNCOMMON" -> "\u00a7a";
            case "RARE" -> "\u00a79";
            case "EPIC" -> "\u00a75";
            case "LEGENDARY" -> "\u00a76";
            case "MYTHIC" -> "\u00a7d";
            case "SPECIAL", "VERY SPECIAL" -> "\u00a7c";
            case "DIVINE" -> "\u00a7b";
            default -> "\u00a7f";
        };
    }

    public static Component createClickableText(String text, String command) {
        return Component.literal(text).withStyle(style -> style.withClickEvent(
                new ClickEvent.RunCommand(command)
        ));
    }

    public static Component createUrlText(String text, String url) {
        return Component.literal(text).withStyle(style -> style.withClickEvent(
                new ClickEvent.OpenUrl(URI.create(url))
        ));
    }
}
