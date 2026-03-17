package host.plas.bou.utils;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.Nullable;
import gg.drak.thebase.lib.re2j.Matcher;
import gg.drak.thebase.lib.re2j.Pattern;
import gg.drak.thebase.utils.MatcherUtils;

import java.awt.*;
import java.util.List;

/**
 * Utility class for handling color codes, hex colors, and text formatting
 * in Bukkit/BungeeCord chat messages.
 */
public class ColorUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private ColorUtils() {
    }

    /**
     * Replaces the {@code %newline%} placeholder with actual newline characters.
     *
     * @param message the message to process
     * @return the message with newline placeholders replaced
     */
    public static String newlined(String message) {
        return message.replace("%newline%", "\n");
    }

    /**
     * Translates alternate color codes (using '{@code &}') into Bukkit color codes,
     * also replacing newline placeholders.
     *
     * @param message the message to colorize
     * @return the colorized message
     */
    public static String colorize(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', newlined(message));
    }

    /**
     * Colorizes a message with both standard color codes and hex color codes.
     * Supports multiple hex formats: {@code &#RRGGBB}, {@code {#RRGGBB}},
     * {@code #RRGGBB}, and {@code <#RRGGBB>}.
     *
     * @param message the message to colorize
     * @return the colorized message with hex colors applied
     */
    public static String colorizeHard(String message) {
        // hex format is &#RRGGBB
        // a message can contain more than just hex color codes
        String colored = colorize(message);

        List<String> regexes = List.of(
                "([&][#]([A-Fa-f0-9]{6}))",
                "([{][#]([A-Fa-f0-9]{6})[}])",
                "([#]([A-Fa-f0-9]{6}))",
                "([<][#]([A-Fa-f0-9]{6})[>])"
        );

//        String hexRegexMain = "([&][#]([A-Fa-f0-9]{6}))";
//        String hexRegexCmi = "([{][#]([A-Fa-f0-9]{6})[}])";
//        String hexRegexCmiAlt = "([#]([A-Fa-f0-9]{6}))";
//        String hexRegexOthers = "([<][#]([A-Fa-f0-9]{6})[>])";

        for (String hexRegex : regexes) {
            colored = replaceHex(colored, hexRegex);
        }

        return colored;
    }

    /**
     * Replaces hex color code matches in a message using the specified regex pattern.
     *
     * @param message  the message to process
     * @param hexRegex the regex pattern to match hex color codes
     * @return the message with hex color codes replaced by BungeeCord ChatColor values
     */
    public static String replaceHex(String message, String hexRegex) {
        Matcher matcher = MatcherUtils.matcherBuilder(hexRegex, message);
        List<String[]> groups = MatcherUtils.getGroups(matcher, 2);
        for (String[] group : groups) {
            String hex = group[1];
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            net.md_5.bungee.api.ChatColor color = net.md_5.bungee.api.ChatColor.of(new Color(r, g, b));

            message = message.replace(group[0], color.toString());
        }

        return message;
    }

    /**
     * Converts a message string into an array of colored BaseComponents.
     *
     * @param message the message to convert
     * @return an array of BaseComponents with color applied
     */
    public static BaseComponent[] color(String message) {
        return colorWithEvents(message, null, null);
    }

    /**
     * Converts a message string into an array of colored BaseComponents with a click event.
     *
     * @param message    the message to convert
     * @param clickEvent the click event to attach, or null for none
     * @return an array of BaseComponents with color and click event applied
     */
    public static BaseComponent[] colorWithClickable(String message, @Nullable ClickEvent clickEvent) {
        return colorWithEvents(message, clickEvent, null);
    }

    /**
     * Converts a message string into an array of colored BaseComponents with a hover event.
     *
     * @param message    the message to convert
     * @param hoverEvent the hover event to attach, or null for none
     * @return an array of BaseComponents with color and hover event applied
     */
    public static BaseComponent[] colorWithHoverable(String message, @Nullable HoverEvent hoverEvent) {
        return colorWithEvents(message, null, hoverEvent);
    }

    /**
     * Converts a message string into an array of colored BaseComponents with optional click and hover events.
     * Handles multiline messages and hex color codes in {@code &#RRGGBB} format.
     *
     * @param message    the message to convert
     * @param clickEvent the click event to attach, or null for none
     * @param hoverEvent the hover event to attach, or null for none
     * @return an array of BaseComponents with color and events applied
     */
    public static BaseComponent[] colorWithEvents(String message, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent) {
        if (message == null) return new ComponentBuilder().create();

        if (message.contains("\n")) {
            String[] lines = message.split("\n");

            ComponentBuilder builder = new ComponentBuilder();
            int i = 0;
            for (String line : lines) {
                boolean isLast = i == lines.length - 1;
                i ++;

                builder.append(colorWithEvents(line, clickEvent, hoverEvent));
                if (! isLast) builder.append("\n");
            }

            return builder.create();
        }

        message = colorize(message);

        // hex format is &#RRGGBB
        // a message can contain more than just hex color codes
        ComponentBuilder builder = new ComponentBuilder();
        Matcher matcher = Pattern.compile("(&#([A-Fa-f0-9]{6}))").matcher(message);
        int lastEnd = 0;
        // Loop through all found color codes
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Append text before the color code as a new component
            if (start > lastEnd) {
                builder.append(new TextComponent(message.substring(lastEnd, start)));
            }

            // Extract the color and apply it to the next component
            String hex = matcher.group(2);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            TextComponent coloredText = new TextComponent(message.substring(end));
            coloredText.setColor(net.md_5.bungee.api.ChatColor.of(new Color(r, g, b)));

            // Set the click event if provided
            if (clickEvent != null) {
                coloredText.setClickEvent(clickEvent);
            }
            // Set the hover event if provided
            if (hoverEvent != null) {
                coloredText.setHoverEvent(hoverEvent);
            }

            // Reset lastEnd to the end of the current match
            lastEnd = end;

            // Append the colored text
            builder.append(coloredText);
            break; // Break after applying the first color, assuming one color per message
        }

        // Append any remaining text after the last color code
        if (lastEnd < message.length()) {
            TextComponent component = new TextComponent(message.substring(lastEnd));

            // Set the click event if provided
            if (clickEvent != null) {
                component.setClickEvent(clickEvent);
            }
            // Set the hover event if provided
            if (hoverEvent != null) {
                component.setHoverEvent(hoverEvent);
            }

            builder.append(component);
        }

        return builder.create();
    }

    /**
     * Converts a message string with color codes into a legacy text string.
     *
     * @param message the message to convert
     * @return the legacy text representation with colors applied
     */
    public static String colorAsString(String message) {
        StringBuilder builder = new StringBuilder();

        for (BaseComponent component : color(message)) {
            builder.append(component.toLegacyText());
        }

        return builder.toString();
    }

    /**
     * Returns a simple color-coded boolean string: green "Yes" for true, red "No" for false.
     *
     * @param bool the boolean value to format
     * @return a color-coded string representation
     */
    public static String simpleColorBoolean(boolean bool) {
        return bool  ? "&aYes" : "&cNo";
    }

    /**
     * Strips all Minecraft formatting codes (both section sign and ampersand variants) from a string.
     *
     * @param from the string to strip formatting from
     * @return the string with all formatting codes removed, or an empty string if the input is null
     */
    public static String stripFormatting(String from) {
        return from != null ? from.replaceAll("(?i)§[0-9A-FK-OR]", "").replaceAll("(?i)&[0-9A-FK-OR]", "") : "";
    }
}
