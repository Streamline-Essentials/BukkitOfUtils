package host.plas.bou;

import host.plas.bou.instances.BaseManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Locale;

public class MessageUtils {
    public static void doReplaceAndSend(String message, String  prefix) {
        message = message.replace("%newline%", "\n");
        for (String line : message.split("\n")) {
            sendMessage(BaseManager.getConsole(), prefix + line);
        }
    }

    public static void logInfo(String message) {
        if (PluginBase.getBaseConfig().getIsInfoLoggingEnabled()) return;

        doReplaceAndSend(message, PluginBase.getBaseConfig().getIsInfoLoggingPrefix());
    }

    public static void logWarning(String message) {
        if (PluginBase.getBaseConfig().getIsWarnLoggingEnabled()) return;

        doReplaceAndSend(message, PluginBase.getBaseConfig().getIsWarnLoggingPrefix());
    }

    public static void logSevere(String message) {
        if (PluginBase.getBaseConfig().getIsSevereLoggingEnabled()) return;

        doReplaceAndSend(message, PluginBase.getBaseConfig().getIsSevereLoggingPrefix());
    }

    public static void logDebug(String message) {
        if (PluginBase.getBaseConfig().getIsDebugLoggingEnabled()) return;

        doReplaceAndSend(message, PluginBase.getBaseConfig().getIsDebugLoggingPrefix());
    }

    public static void logInfo(StackTraceElement[] stackTraceElements) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logInfo(stackTraceElement.toString());
        });
    }

    public static void logWarning(StackTraceElement[] stackTraceElements) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logWarning(stackTraceElement.toString());
        });
    }

    public static void logSevere(StackTraceElement[] stackTraceElements) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logSevere(stackTraceElement.toString());
        });
    }

    public static void logDebug(StackTraceElement[] stackTraceElements) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logDebug(stackTraceElement.toString());
        });
    }

    public static void logInfo(String message, Throwable throwable) {
        logInfo(message);
        logInfo(throwable.getStackTrace());
    }

    public static void logWarning(String message, Throwable throwable) {
        logWarning(message);
        logWarning(throwable.getStackTrace());
    }

    public static void logSevere(String message, Throwable throwable) {
        logSevere(message);
        logSevere(throwable.getStackTrace());
    }

    public static void logDebug(String message, Throwable throwable) {
        logDebug(message);
        logDebug(throwable.getStackTrace());
    }

    public static void logInfoWithInfo(String message, Throwable throwable) {
        logInfo(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable);
    }

    public static void logWarningWithInfo(String message, Throwable throwable) {
        logWarning(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable);
    }

    public static void logSevereWithInfo(String message, Throwable throwable) {
        logSevere(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable);
    }

    public static void logDebugWithInfo(String message, Throwable throwable) {
        logDebug(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable);
    }
    
    public static void sendMessage(CommandSender to, String message) {
        to.sendMessage(codedString(message));
    }

    public static String codedString(String text){
        return formatted(newLined(ChatColor.translateAlternateColorCodes('&', text)));
    }

    public static String formatted(String string) {
        String[] strings = string.split(" ");

        for (int i = 0; i < strings.length; i ++) {
            if (strings[i].toLowerCase(Locale.ROOT).startsWith("<to_upper>")) {
                strings[i] = strings[i].toUpperCase(Locale.ROOT).replace("<TO_UPPER>", "");
            }
            if (strings[i].toLowerCase(Locale.ROOT).startsWith("<to_lower>")) {
                strings[i] = strings[i].toLowerCase(Locale.ROOT).replace("<to_lower>", "");
            }
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < strings.length; i ++) {
            if (i == strings.length - 1) {
                builder.append(strings[i]);
            } else {
                builder.append(strings[i]).append(" ");
            }
        }

        return builder.toString();
    }

    public static String newLined(String text){
        return text.replace("%newline%", "\n");
    }

    public static boolean isCommand(String msg){
        return msg.startsWith("/");
    }
}