package host.plas.bou.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import tv.quaint.thebase.lib.re2j.Matcher;
import tv.quaint.thebase.lib.re2j.Pattern;
import tv.quaint.utils.MatcherUtils;

import java.awt.*;
import java.util.List;

public class ColorUtils {
    public static String colorize(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }

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

    public static BaseComponent[] color(String message) {
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
            String hex = matcher.group(1);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            TextComponent coloredText = new TextComponent(message.substring(end));
            coloredText.setColor(net.md_5.bungee.api.ChatColor.of(new Color(r, g, b)));

            // Reset lastEnd to the end of the current match
            lastEnd = end;

            // Append the colored text
            builder.append(coloredText);
            break; // Break after applying the first color, assuming one color per message
        }

        // Append any remaining text after the last color code
        if (lastEnd < message.length()) {
            builder.append(new TextComponent(message.substring(lastEnd)));
        }

        return builder.create();
    }
}
