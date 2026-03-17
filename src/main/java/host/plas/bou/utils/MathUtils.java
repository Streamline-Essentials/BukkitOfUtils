package host.plas.bou.utils;

/**
 * Utility class providing basic math operations such as clamping and number validation.
 */
public class MathUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MathUtils() {
        // utility class
    }

    /**
     * Clamps an integer value between a minimum and maximum.
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @return the clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a double value between a minimum and maximum.
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a float value between a minimum and maximum.
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @return the clamped value
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a long value between a minimum and maximum.
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @return the clamped value
     */
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Checks whether the given string can be parsed as a number (double).
     *
     * @param str the string to check
     * @return true if the string represents a valid number, false otherwise
     */
    public static boolean isNumber(String str) {
        if (str == null || str.isBlank()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
