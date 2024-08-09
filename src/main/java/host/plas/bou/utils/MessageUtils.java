package host.plas.bou.utils;

import host.plas.bou.BetterPlugin;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.notifications.NotificationTimer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Locale;

public class MessageUtils {
    public static String getNotificationIdentifier(String message) {
        return truncateString(message, 17);
    }

    public static String truncateString(String string, int length) {
        if (string.length() > length) {
            return string.substring(0, length);
        }
        return string;
    }

    public static void doReplaceAndSend(CommandSender to, String message, String prefix) {
        if (NotificationTimer.hasNotification(getNotificationIdentifier(message), to)) return;

        message = message.replace("%newline%", "\n");
        for (String line : message.split("\n")) {
            sendMessage(to, prefix + line);
        }

        NotificationTimer.addNotification(getNotificationIdentifier(message), to);
    }

    public static void doReplaceAndSend(CommandSender to, String message) {
        doReplaceAndSend(to, message, "");
    }

    public static void doReplaceAndSend(String message, String prefix) {
        doReplaceAndSend(BaseManager.getConsole(), message, prefix);
    }

    public static void doReplaceAndSend(String message, BetterPlugin base, String prefix) {
        doReplaceAndSend(message, base.getLogPrefix() + prefix);
    }

    @Deprecated
    public static void logInfo(String message) {
        logInfo(message, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logWarning(String message) {
        logWarning(message, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logSevere(String message) {
        logSevere(message, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logDebug(String message) {
        logDebug(message, BaseManager.getBaseInstance());
    }

    public static void logInfo(String message, BetterPlugin base) {
        if (! BetterPlugin.getBaseConfig().getIsInfoLoggingEnabled()) return;

        doReplaceAndSend(message, base, BetterPlugin.getBaseConfig().getIsInfoLoggingPrefix());
    }

    public static void logWarning(String message, BetterPlugin base) {
        if (! BetterPlugin.getBaseConfig().getIsWarnLoggingEnabled()) return;

        doReplaceAndSend(message, base, BetterPlugin.getBaseConfig().getIsWarnLoggingPrefix());
    }

    public static void logSevere(String message, BetterPlugin base) {
        if (! BetterPlugin.getBaseConfig().getIsSevereLoggingEnabled()) return;

        doReplaceAndSend(message, base, BetterPlugin.getBaseConfig().getIsSevereLoggingPrefix());
    }

    public static void logDebug(String message, BetterPlugin base) {
        if (! BetterPlugin.getBaseConfig().getIsDebugLoggingEnabled()) return;

        doReplaceAndSend(message, base, BetterPlugin.getBaseConfig().getIsDebugLoggingPrefix());
    }

    @Deprecated
    public static void logInfo(StackTraceElement[] stackTraceElements) {
        logInfo(stackTraceElements, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logWarning(StackTraceElement[] stackTraceElements) {
        logWarning(stackTraceElements, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logSevere(StackTraceElement[] stackTraceElements) {
        logSevere(stackTraceElements, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logDebug(StackTraceElement[] stackTraceElements) {
        logDebug(stackTraceElements, BaseManager.getBaseInstance());
    }

    public static void logInfo(StackTraceElement[] stackTraceElements, BetterPlugin base) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logInfo(stackTraceElement.toString(), base);
        });
    }

    public static void logWarning(StackTraceElement[] stackTraceElements, BetterPlugin base) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logWarning(stackTraceElement.toString(), base);
        });
    }

    public static void logSevere(StackTraceElement[] stackTraceElements, BetterPlugin base) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logSevere(stackTraceElement.toString(), base);
        });
    }

    public static void logDebug(StackTraceElement[] stackTraceElements, BetterPlugin base) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logDebug(stackTraceElement.toString(), base);
        });
    }

    public static void logInfo(Throwable throwable, BetterPlugin base) {
        logInfo(throwable.getMessage(), base);
        logInfo(throwable.getStackTrace(), base);
    }

    public static void logWarning(Throwable throwable, BetterPlugin base) {
        logWarning(throwable.getMessage(), base);
        logWarning(throwable.getStackTrace(), base);
    }

    public static void logSevere(Throwable throwable, BetterPlugin base) {
        logSevere(throwable.getMessage(), base);
        logSevere(throwable.getStackTrace(), base);
    }

    public static void logDebug(Throwable throwable, BetterPlugin base) {
        logDebug(throwable.getMessage(), base);
        logDebug(throwable.getStackTrace(), base);
    }

    @Deprecated
    public static void logInfo(String message, Throwable throwable) {
        logInfo(message, throwable, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logWarning(String message, Throwable throwable) {
        logWarning(message, throwable, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logSevere(String message, Throwable throwable) {
        logSevere(message, throwable, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logDebug(String message, Throwable throwable) {
        logDebug(message, throwable, BaseManager.getBaseInstance());
    }

    public static void logInfo(String message, Throwable throwable, BetterPlugin base) {
        logInfo(message, base);
        logInfo(throwable.getStackTrace(), base);
    }

    public static void logWarning(String message, Throwable throwable, BetterPlugin base) {
        logWarning(message, base);
        logWarning(throwable.getStackTrace(), base);
    }

    public static void logSevere(String message, Throwable throwable, BetterPlugin base) {
        logSevere(message, base);
        logSevere(throwable.getStackTrace(), base);
    }

    public static void logDebug(String message, Throwable throwable, BetterPlugin base) {
        logDebug(message, base);
        logDebug(throwable.getStackTrace(), base);
    }

    @Deprecated
    public static void logInfoWithInfo(String message, Throwable throwable) {
        logInfoWithInfo(message, throwable, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logWarningWithInfo(String message, Throwable throwable) {
        logWarningWithInfo(message, throwable, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logSevereWithInfo(String message, Throwable throwable) {
        logSevereWithInfo(message, throwable, BaseManager.getBaseInstance());
    }

    @Deprecated
    public static void logDebugWithInfo(String message, Throwable throwable) {
        logDebugWithInfo(message, throwable, BaseManager.getBaseInstance());
    }

    public static void logInfoWithInfo(String message, Throwable throwable, BetterPlugin base) {
        logInfo(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable, base);
    }

    public static void logWarningWithInfo(String message, Throwable throwable, BetterPlugin base) {
        logWarning(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable, base);
    }

    public static void logSevereWithInfo(String message, Throwable throwable, BetterPlugin base) {
        logSevere(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable, base);
    }

    public static void logDebugWithInfo(String message, Throwable throwable, BetterPlugin base) {
        logDebug(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable, base);
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