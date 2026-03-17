package host.plas.bou.utils;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.obj.CapitalizationType;

/**
 * Utility class for string manipulation including capitalization,
 * truncation, and character removal.
 */
public class StringUtils {
    /** Private constructor to prevent instantiation of this utility class. */
    private StringUtils() {}
    /**
     * Capitalizes a string according to the specified capitalization type.
     *
     * @param input the string to capitalize
     * @param type  the capitalization type to apply
     * @return the capitalized string, or the original input if null/empty or type is unknown
     */
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

    /**
     * Truncates a string to the specified maximum length.
     *
     * @param input  the string to truncate
     * @param length the maximum length
     * @return the truncated string, or the original if it is shorter than or equal to the specified length
     */
    public static String truncate(String input, int length) {
        if (input == null || input.length() <= length) {
            return input;
        }

        return input.substring(0, length);
    }

    /**
     * Truncates an object's string representation to the specified maximum length.
     *
     * @param input  the object whose string representation to truncate
     * @param length the maximum length
     * @return the truncated string, or null if the input is null
     */
    public static String truncate(Object input, int length) {
        if (input == null) return null;

        if (input instanceof String) {
            return truncate((String) input, length);
        }

        return truncate(input.toString(), length);
    }

    /**
     * Truncates the decimal portion of a numeric string to the specified length.
     * If the string does not contain a decimal point, it is truncated normally.
     *
     * @param input  the string to truncate
     * @param length the maximum number of decimal places
     * @return the string with truncated decimals
     */
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

    /**
     * Truncates the decimal portion of an object's string representation to the specified length.
     *
     * @param input  the object whose string representation to process
     * @param length the maximum number of decimal places
     * @return the string with truncated decimals, or null if the input is null
     */
    public static String truncateDecimals(Object input, int length) {
        if (input == null) return null;

        if (input instanceof String) {
            return truncateDecimals((String) input, length);
        }

        return truncateDecimals(input.toString(), length);
    }

    /**
     * Removes all whitespace characters from a string.
     *
     * @param input the string to process
     * @return the string with all whitespace removed, or null if the input is null
     */
    public static String removeSpaces(String input) {
        if (input == null) {
            return null;
        }

        return input.replaceAll("\\s+", "");
    }

    /**
     * Removes all non-alphanumeric characters from a string.
     *
     * @param input the string to process
     * @return the string with only alphanumeric characters remaining, or null if the input is null
     */
    public static String removeSpecialCharacters(String input) {
        if (input == null) {
            return null;
        }

        return input.replaceAll("[^a-zA-Z0-9]", "");
    }
}
