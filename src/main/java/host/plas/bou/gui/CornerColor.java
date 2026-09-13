package host.plas.bou.gui;

import host.plas.bou.compat.LegacySupport;
import org.bukkit.Material;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stained-glass corner accent colors for inventory GUIs.
 */
public enum CornerColor {
    WHITE("WHITE_STAINED_GLASS_PANE", 0, 0xF9, 0xFF, 0xFE),
    ORANGE("ORANGE_STAINED_GLASS_PANE", 1, 0xF9, 0x80, 0x1D),
    MAGENTA("MAGENTA_STAINED_GLASS_PANE", 2, 0xC7, 0x4E, 0xBD),
    LIGHT_BLUE("LIGHT_BLUE_STAINED_GLASS_PANE", 3, 0x3A, 0xB3, 0xDA),
    YELLOW("YELLOW_STAINED_GLASS_PANE", 4, 0xFF, 0xE7, 0x0E),
    LIME("LIME_STAINED_GLASS_PANE", 5, 0x80, 0xC7, 0x1F),
    PINK("PINK_STAINED_GLASS_PANE", 6, 0xF3, 0x8B, 0xAA),
    GRAY("GRAY_STAINED_GLASS_PANE", 7, 0x9D, 0x9D, 0x97),
    LIGHT_GRAY("LIGHT_GRAY_STAINED_GLASS_PANE", 8, 0xD0, 0xD0, 0xD0),
    CYAN("CYAN_STAINED_GLASS_PANE", 9, 0x16, 0x9C, 0x9C),
    PURPLE("PURPLE_STAINED_GLASS_PANE", 10, 0x89, 0x32, 0xB8),
    BLUE("BLUE_STAINED_GLASS_PANE", 11, 0x3C, 0x44, 0xAA),
    BROWN("BROWN_STAINED_GLASS_PANE", 12, 0x83, 0x54, 0x32),
    GREEN("GREEN_STAINED_GLASS_PANE", 13, 0x5E, 0x7C, 0x16),
    RED("RED_STAINED_GLASS_PANE", 14, 0xB0, 0x2E, 0x26),
    BLACK("BLACK_STAINED_GLASS_PANE", 15, 0x1D, 0x1D, 0x21);

    private static final Pattern HEX_PATTERN = Pattern.compile("(?:&#|#)([0-9A-Fa-f]{6})");
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("[&§]x(?:[&§][0-9A-Fa-f]){6}");

    private final String materialName;
    private final int legacyData;
    private final int red;
    private final int green;
    private final int blue;

    CornerColor(String materialName, int legacyData, int red, int green, int blue) {
        this.materialName = materialName;
        this.legacyData = legacyData;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Resolves the pane material for this color on the running server.
     *
     * <p>Materials are resolved by name rather than referenced directly, because the flattened
     * per-color names only exist on 1.13+. Referencing them as enum constants would make this
     * whole class fail to initialize on 1.8 with a {@link NoSuchFieldError}. On legacy servers
     * all colors share {@code STAINED_GLASS_PANE} and are distinguished by
     * {@link #legacyData()}.</p>
     *
     * @return the pane material for this server version
     */
    public Material paneMaterial() {
        return LegacySupport.material(null, this.materialName, "STAINED_GLASS_PANE");
    }

    /**
     * Returns the pre-1.13 data value that selects this color on a shared legacy material.
     *
     * @return the legacy damage/data value, 0-15
     */
    public int legacyData() {
        return this.legacyData;
    }

    /**
     * Picks the corner color whose pane RGB is closest to the first color found in {@code displayText}.
     *
     * @param displayText title or other colored display text
     * @return closest corner color, or {@link #YELLOW} when no color is found
     */
    public static CornerColor fromDisplayText(String displayText) {
        if (displayText == null || displayText.isBlank()) {
            return YELLOW;
        }

        int[] rgb = extractRgb(displayText);
        if (rgb == null) {
            return YELLOW;
        }
        return closestTo(rgb[0], rgb[1], rgb[2]);
    }

    public static CornerColor closestTo(int red, int green, int blue) {
        CornerColor closest = YELLOW;
        long bestDistance = Long.MAX_VALUE;

        for (CornerColor color : values()) {
            long distance = colorDistanceSquared(red, green, blue, color.red, color.green, color.blue);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = color;
            }
        }
        return closest;
    }

    private static long colorDistanceSquared(int r1, int g1, int b1, int r2, int g2, int b2) {
        long dr = r1 - r2;
        long dg = g1 - g2;
        long db = b1 - b2;
        return dr * dr + dg * dg + db * db;
    }

    private static int[] extractRgb(String text) {
        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        if (hexMatcher.find()) {
            return parseHex(hexMatcher.group(1));
        }

        Matcher legacyHexMatcher = LEGACY_HEX_PATTERN.matcher(text);
        if (legacyHexMatcher.find()) {
            String match = legacyHexMatcher.group();
            StringBuilder builder = new StringBuilder(6);
            for (int i = 0; i < match.length(); i++) {
                char character = match.charAt(i);
                if (character == '&' || character == '§' || character == 'x' || character == 'X') {
                    continue;
                }
                builder.append(character);
            }
            if (builder.length() == 6) {
                return parseHex(builder.toString());
            }
        }

        for (int i = 0; i < text.length() - 1; i++) {
            char marker = text.charAt(i);
            if (marker != '&' && marker != '§') {
                continue;
            }
            char code = Character.toLowerCase(text.charAt(i + 1));
            int[] legacy = legacyCodeRgb(code);
            if (legacy != null) {
                return legacy;
            }
        }

        return null;
    }

    private static int[] parseHex(String hex) {
        return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private static int[] legacyCodeRgb(char code) {
        switch (code) {
            case '0':
                return new int[]{0, 0, 0};
            case '1':
                return new int[]{0, 0, 170};
            case '2':
                return new int[]{0, 170, 0};
            case '3':
                return new int[]{0, 170, 170};
            case '4':
                return new int[]{170, 0, 0};
            case '5':
                return new int[]{170, 0, 170};
            case '6':
                return new int[]{255, 170, 0};
            case '7':
                return new int[]{170, 170, 170};
            case '8':
                return new int[]{85, 85, 85};
            case '9':
                return new int[]{85, 85, 255};
            case 'a':
                return new int[]{85, 255, 85};
            case 'b':
                return new int[]{85, 255, 255};
            case 'c':
                return new int[]{255, 85, 85};
            case 'd':
                return new int[]{255, 85, 255};
            case 'e':
                return new int[]{255, 255, 85};
            case 'f':
                return new int[]{255, 255, 255};
            default:
                return null;
        }
    }
}
