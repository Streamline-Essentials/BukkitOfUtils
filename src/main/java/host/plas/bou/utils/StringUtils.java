package host.plas.bou.utils;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.obj.CapitalizationType;

public class StringUtils {
    public static String capitalize(String input, CapitalizationType type) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.split(" ");

        switch (type) {
            case LOWER_ALL:
                return input.toLowerCase();
            case LOWER_FIRST:
                return Character.toLowerCase(input.charAt(0)) + input.substring(1);
            case UPPER_ALL:
                return input.toUpperCase();
            case UPPER_FIRST:
                return Character.toUpperCase(input.charAt(0)) + input.substring(1);
            case WORD_LOWER_ALL:
                for (int i = 0; i < words.length; i++) {
                    words[i] = words[i].toLowerCase();
                }
                return String.join(" ", words);
            case WORD_LOWER_FIRST:
                for (int i = 0; i < words.length; i++) {
                    if (i == 0) {
                        words[i] = Character.toLowerCase(words[i].charAt(0)) + words[i].substring(1);
                    } else {
                        words[i] = words[i].toLowerCase();
                    }
                }
                return String.join(" ", words);
            case WORD_UPPER_ALL:
                for (int i = 0; i < words.length; i++) {
                    words[i] = words[i].toUpperCase();
                }
                return String.join(" ", words);
            case WORD_UPPER_FIRST:
                for (int i = 0; i < words.length; i++) {
                    if (i == 0) {
                        words[i] = Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
                    } else {
                        words[i] = words[i].toUpperCase();
                    }
                }
                return String.join(" ", words);
            default:
                BukkitOfUtils.getInstance().logSevere("Unknown capitalization type: " + type);
                return input;
        }
    }

    public static String truncate(String input, int length) {
        if (input == null || input.length() <= length) {
            return input;
        }

        return input.substring(0, length);
    }

    public static String truncate(Object input, int length) {
        if (input == null) return null;

        if (input instanceof String) {
            return truncate((String) input, length);
        }

        return truncate(input.toString(), length);
    }

    public static String truncateDecimals(String input, int length) {
        if (input == null || input.length() <= length) {
            return input;
        }

        if (input.contains(".")) {
            String[] parts = input.split("\\.");
            if (parts.length > 1) {
                return parts[0] + "." + truncate(parts[1], length);
            }
        }

        return truncate(input, length);
    }

    public static String truncateDecimals(Object input, int length) {
        if (input == null) return null;

        if (input instanceof String) {
            return truncateDecimals((String) input, length);
        }

        return truncateDecimals(input.toString(), length);
    }

    public static String removeSpaces(String input) {
        if (input == null) {
            return null;
        }

        return input.replaceAll("\\s+", "");
    }

    public static String removeSpecialCharacters(String input) {
        if (input == null) {
            return null;
        }

        return input.replaceAll("[^a-zA-Z0-9]", "");
    }
}
