package host.plas.bou.utils;

import host.plas.bou.BetterPlugin;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.notifications.NotificationTimer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Locale;

/**
 * Utility class for logging messages at various levels and sending formatted messages
 * to command senders with notification deduplication support.
 */
public class MessageUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MessageUtils() {
        // utility class
    }

    /**
     * Creates a notification identifier by truncating the message to 50 characters.
     *
     * @param message the message to create an identifier from
     * @return the truncated notification identifier
     */
    public static String getNotificationIdentifier(String message) {
        return truncateString(message, 50);
    }

    /**
     * Truncates a string to the specified maximum length.
     *
     * @param string the string to truncate
     * @param length the maximum length
     * @return the truncated string, or "null" if the input is null
     */
    public static String truncateString(String string, int length) {
        if (string == null) return "null";
        if (string.length() > length) {
            return string.substring(0, length);
        }
        return string;
    }

    /**
     * Replaces newline placeholders in a message, prepends a prefix to each line,
     * and sends the result to the specified command sender.
     *
     * @param to      the command sender to send the message to
     * @param message the message to process and send
     * @param prefix  the prefix to prepend to each line
     */
    public static void doReplaceAndSend(CommandSender to, String message, String prefix) {
        if (message == null) return;
//        if (NotificationTimer.hasNotification(getNotificationIdentifier(message), to)) return;

        message = message.replace("%newline%", "\n");
        for (String line : message.split("\n")) {
            sendMessage(to, prefix + line);
        }

        NotificationTimer.addNotification(getNotificationIdentifier(message), to);
    }

    /**
     * Replaces newline placeholders and sends a message to the specified command sender without a prefix.
     *
     * @param to      the command sender to send the message to
     * @param message the message to process and send
     */
    public static void doReplaceAndSend(CommandSender to, String message) {
        doReplaceAndSend(to, message, "");
    }

    /**
     * Replaces newline placeholders and sends a message to the console with a prefix.
     *
     * @param message the message to process and send
     * @param prefix  the prefix to prepend to each line
     */
    public static void doReplaceAndSend(String message, String prefix) {
        doReplaceAndSend(BaseManager.getConsole(), message, prefix);
    }

    /**
     * Replaces newline placeholders and sends a message to the console using the plugin's log prefix.
     *
     * @param message the message to process and send
     * @param base    the plugin whose log prefix to use
     * @param prefix  an additional prefix to append after the plugin's log prefix
     */
    public static void doReplaceAndSend(String message, BetterPlugin base, String prefix) {
        doReplaceAndSend(message, base.getLogPrefix() + prefix);
    }

    /**
     * Logs an informational message using the base plugin instance.
     *
     * @param message the message to log
     * @deprecated Use {@link #logInfo(String, BetterPlugin)} instead
     */
    @Deprecated
    public static void logInfo(String message) {
        logInfo(message, BaseManager.getBaseInstance());
    }

    /**
     * Logs a warning message using the base plugin instance.
     *
     * @param message the message to log
     * @deprecated Use {@link #logWarning(String, BetterPlugin)} instead
     */
    @Deprecated
    public static void logWarning(String message) {
        logWarning(message, BaseManager.getBaseInstance());
    }

    /**
     * Logs a severe message using the base plugin instance.
     *
     * @param message the message to log
     * @deprecated Use {@link #logSevere(String, BetterPlugin)} instead
     */
    @Deprecated
    public static void logSevere(String message) {
        logSevere(message, BaseManager.getBaseInstance());
    }

    /**
     * Logs a debug message using the base plugin instance.
     *
     * @param message the message to log
     * @deprecated Use {@link #logDebug(String, BetterPlugin)} instead
     */
    @Deprecated
    public static void logDebug(String message) {
        logDebug(message, BaseManager.getBaseInstance());
    }

    /**
     * Logs an informational message to the console if info logging is enabled.
     *
     * @param message the message to log
     * @param base    the plugin to log on behalf of
     */
    public static void logInfo(String message, BetterPlugin base) {
        if (! BaseManager.getBaseConfig().getIsInfoLoggingEnabled()) return;

        doReplaceAndSend(message, base, BaseManager.getBaseConfig().getIsInfoLoggingPrefix());
    }

    /**
     * Logs a warning message to the console if warning logging is enabled.
     *
     * @param message the message to log
     * @param base    the plugin to log on behalf of
     */
    public static void logWarning(String message, BetterPlugin base) {
        if (! BaseManager.getBaseConfig().getIsWarnLoggingEnabled()) return;

        doReplaceAndSend(message, base, BaseManager.getBaseConfig().getIsWarnLoggingPrefix());
    }

    /**
     * Logs a severe message to the console if severe logging is enabled.
     *
     * @param message the message to log
     * @param base    the plugin to log on behalf of
     */
    public static void logSevere(String message, BetterPlugin base) {
        if (! BaseManager.getBaseConfig().getIsSevereLoggingEnabled()) return;

        doReplaceAndSend(message, base, BaseManager.getBaseConfig().getIsSevereLoggingPrefix());
    }

    /**
     * Logs a debug message to the console if debug logging is enabled.
     *
     * @param message the message to log
     * @param base    the plugin to log on behalf of
     */
    public static void logDebug(String message, BetterPlugin base) {
        if (! BaseManager.getBaseConfig().getIsDebugLoggingEnabled()) return;

        doReplaceAndSend(message, base, BaseManager.getBaseConfig().getIsDebugLoggingPrefix());
    }

    /**
     * Logs stack trace elements as informational messages using the base plugin instance.
     *
     * @param stackTraceElements the stack trace elements to log
     * @deprecated Use {@link #logInfo(StackTraceElement[], BetterPlugin)} instead
     */
    @Deprecated
    public static void logInfo(StackTraceElement[] stackTraceElements) {
        logInfo(stackTraceElements, BaseManager.getBaseInstance());
    }

    /**
     * Logs stack trace elements as warning messages using the base plugin instance.
     *
     * @param stackTraceElements the stack trace elements to log
     * @deprecated Use {@link #logWarning(StackTraceElement[], BetterPlugin)} instead
     */
    @Deprecated
    public static void logWarning(StackTraceElement[] stackTraceElements) {
        logWarning(stackTraceElements, BaseManager.getBaseInstance());
    }

    /**
     * Logs stack trace elements as severe messages using the base plugin instance.
     *
     * @param stackTraceElements the stack trace elements to log
     * @deprecated Use {@link #logSevere(StackTraceElement[], BetterPlugin)} instead
     */
    @Deprecated
    public static void logSevere(StackTraceElement[] stackTraceElements) {
        logSevere(stackTraceElements, BaseManager.getBaseInstance());
    }

    /**
     * Logs stack trace elements as debug messages using the base plugin instance.
     *
     * @param stackTraceElements the stack trace elements to log
     * @deprecated Use {@link #logDebug(StackTraceElement[], BetterPlugin)} instead
     */
    @Deprecated
    public static void logDebug(StackTraceElement[] stackTraceElements) {
        logDebug(stackTraceElements, BaseManager.getBaseInstance());
    }

    /**
     * Logs each stack trace element as an informational message.
     *
     * @param stackTraceElements the stack trace elements to log
     * @param base               the plugin to log on behalf of
     */
    public static void logInfo(StackTraceElement[] stackTraceElements, BetterPlugin base) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logInfo(stackTraceElement.toString(), base);
        });
    }

    /**
     * Logs each stack trace element as a warning message.
     *
     * @param stackTraceElements the stack trace elements to log
     * @param base               the plugin to log on behalf of
     */
    public static void logWarning(StackTraceElement[] stackTraceElements, BetterPlugin base) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logWarning(stackTraceElement.toString(), base);
        });
    }

    /**
     * Logs each stack trace element as a severe message.
     *
     * @param stackTraceElements the stack trace elements to log
     * @param base               the plugin to log on behalf of
     */
    public static void logSevere(StackTraceElement[] stackTraceElements, BetterPlugin base) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logSevere(stackTraceElement.toString(), base);
        });
    }

    /**
     * Logs each stack trace element as a debug message.
     *
     * @param stackTraceElements the stack trace elements to log
     * @param base               the plugin to log on behalf of
     */
    public static void logDebug(StackTraceElement[] stackTraceElements, BetterPlugin base) {
        Arrays.stream(stackTraceElements).forEach(stackTraceElement -> {
            logDebug(stackTraceElement.toString(), base);
        });
    }

    /**
     * Logs a throwable's message and stack trace as informational messages.
     *
     * @param throwable the throwable to log
     * @param base      the plugin to log on behalf of
     */
    public static void logInfo(Throwable throwable, BetterPlugin base) {
        logInfo(throwable.getMessage(), base);
        logInfo(throwable.getStackTrace(), base);
    }

    /**
     * Logs a throwable's message and stack trace as warning messages.
     *
     * @param throwable the throwable to log
     * @param base      the plugin to log on behalf of
     */
    public static void logWarning(Throwable throwable, BetterPlugin base) {
        logWarning(throwable.getMessage(), base);
        logWarning(throwable.getStackTrace(), base);
    }

    /**
     * Logs a throwable's message and stack trace as severe messages.
     *
     * @param throwable the throwable to log
     * @param base      the plugin to log on behalf of
     */
    public static void logSevere(Throwable throwable, BetterPlugin base) {
        logSevere(throwable.getMessage(), base);
        logSevere(throwable.getStackTrace(), base);
    }

    /**
     * Logs a throwable's message and stack trace as debug messages.
     *
     * @param throwable the throwable to log
     * @param base      the plugin to log on behalf of
     */
    public static void logDebug(Throwable throwable, BetterPlugin base) {
        logDebug(throwable.getMessage(), base);
        logDebug(throwable.getStackTrace(), base);
    }

    /**
     * Logs a message and throwable as informational messages using the base plugin instance.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @deprecated Use {@link #logInfo(String, Throwable, BetterPlugin)} instead
     */
    @Deprecated
    public static void logInfo(String message, Throwable throwable) {
        logInfo(message, throwable, BaseManager.getBaseInstance());
    }

    /**
     * Logs a message and throwable as warning messages using the base plugin instance.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @deprecated Use {@link #logWarning(String, Throwable, BetterPlugin)} instead
     */
    @Deprecated
    public static void logWarning(String message, Throwable throwable) {
        logWarning(message, throwable, BaseManager.getBaseInstance());
    }

    /**
     * Logs a message and throwable as severe messages using the base plugin instance.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @deprecated Use {@link #logSevere(String, Throwable, BetterPlugin)} instead
     */
    @Deprecated
    public static void logSevere(String message, Throwable throwable) {
        logSevere(message, throwable, BaseManager.getBaseInstance());
    }

    /**
     * Logs a message and throwable as debug messages using the base plugin instance.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @deprecated Use {@link #logDebug(String, Throwable, BetterPlugin)} instead
     */
    @Deprecated
    public static void logDebug(String message, Throwable throwable) {
        logDebug(message, throwable, BaseManager.getBaseInstance());
    }

    /**
     * Logs a message followed by the throwable's message and stack trace as informational messages.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @param base      the plugin to log on behalf of
     */
    public static void logInfo(String message, Throwable throwable, BetterPlugin base) {
        logInfo(message, base);
        logInfo(throwable.getMessage(), base);
        logInfo(throwable.getStackTrace(), base);
    }

    /**
     * Logs a message followed by the throwable's message and stack trace as warning messages.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @param base      the plugin to log on behalf of
     */
    public static void logWarning(String message, Throwable throwable, BetterPlugin base) {
        logWarning(message, base);
        logWarning(throwable.getMessage(), base);
        logWarning(throwable.getStackTrace(), base);
    }

    /**
     * Logs a message followed by the throwable's message and stack trace as severe messages.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @param base      the plugin to log on behalf of
     */
    public static void logSevere(String message, Throwable throwable, BetterPlugin base) {
        logSevere(message, base);
        logSevere(throwable.getMessage(), base);
        logSevere(throwable.getStackTrace(), base);
    }

    /**
     * Logs a message followed by the throwable's message and stack trace as debug messages.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @param base      the plugin to log on behalf of
     */
    public static void logDebug(String message, Throwable throwable, BetterPlugin base) {
        logDebug(message, base);
        logDebug(throwable.getMessage(), base);
        logDebug(throwable.getStackTrace(), base);
    }

    /**
     * Logs a message with the throwable's message appended, followed by the full stack trace, as info.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @deprecated Use {@link #logInfoWithInfo(String, Throwable, BetterPlugin)} instead
     */
    @Deprecated
    public static void logInfoWithInfo(String message, Throwable throwable) {
        logInfoWithInfo(message, throwable, BaseManager.getBaseInstance());
    }

    /**
     * Logs a message with the throwable's message appended, followed by the full stack trace, as warning.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @deprecated Use {@link #logWarningWithInfo(String, Throwable, BetterPlugin)} instead
     */
    @Deprecated
    public static void logWarningWithInfo(String message, Throwable throwable) {
        logWarningWithInfo(message, throwable, BaseManager.getBaseInstance());
    }

    /**
     * Logs a message with the throwable's message appended, followed by the full stack trace, as severe.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @deprecated Use {@link #logSevereWithInfo(String, Throwable, BetterPlugin)} instead
     */
    @Deprecated
    public static void logSevereWithInfo(String message, Throwable throwable) {
        logSevereWithInfo(message, throwable, BaseManager.getBaseInstance());
    }

    /**
     * Logs a message with the throwable's message appended, followed by the full stack trace, as debug.
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @deprecated Use {@link #logDebugWithInfo(String, Throwable, BetterPlugin)} instead
     */
    @Deprecated
    public static void logDebugWithInfo(String message, Throwable throwable) {
        logDebugWithInfo(message, throwable, BaseManager.getBaseInstance());
    }

    /**
     * Logs a combined message (original message + throwable message) with the full stack trace as info.
     *
     * @param message   the message to log
     * @param throwable the throwable whose message and stack trace to include
     * @param base      the plugin to log on behalf of
     */
    public static void logInfoWithInfo(String message, Throwable throwable, BetterPlugin base) {
        logInfo(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable, base);
    }

    /**
     * Logs a combined message (original message + throwable message) with the full stack trace as warning.
     *
     * @param message   the message to log
     * @param throwable the throwable whose message and stack trace to include
     * @param base      the plugin to log on behalf of
     */
    public static void logWarningWithInfo(String message, Throwable throwable, BetterPlugin base) {
        logWarning(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable, base);
    }

    /**
     * Logs a combined message (original message + throwable message) with the full stack trace as severe.
     *
     * @param message   the message to log
     * @param throwable the throwable whose message and stack trace to include
     * @param base      the plugin to log on behalf of
     */
    public static void logSevereWithInfo(String message, Throwable throwable, BetterPlugin base) {
        logSevere(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable, base);
    }

    /**
     * Logs a combined message (original message + throwable message) with the full stack trace as debug.
     *
     * @param message   the message to log
     * @param throwable the throwable whose message and stack trace to include
     * @param base      the plugin to log on behalf of
     */
    public static void logDebugWithInfo(String message, Throwable throwable, BetterPlugin base) {
        logDebug(message + (message.endsWith(" ") ? "" : " ") + throwable.getMessage(), throwable, base);
    }

    /**
     * Sends a color-coded message to a command sender.
     *
     * @param to      the command sender to send the message to
     * @param message the message to send (color codes will be translated)
     */
    public static void sendMessage(CommandSender to, String message) {
        to.sendMessage(codedString(message));
    }

    /**
     * Translates color codes and newline placeholders in a string.
     *
     * @param text the text to translate
     * @return the color-coded and formatted string
     */
    public static String codedString(String text){
        return formatted(newLined(ChatColor.translateAlternateColorCodes('&', text)));
    }

    /**
     * Applies special formatting tags to a string, processing {@code <to_upper>} and {@code <to_lower>} markers.
     *
     * @param string the string to format
     * @return the formatted string
     */
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

    /**
     * Replaces the {@code %newline%} placeholder with actual newline characters.
     *
     * @param text the text to process
     * @return the text with newline placeholders replaced
     */
    public static String newLined(String text){
        return text.replace("%newline%", "\n");
    }

    /**
     * Checks whether the given message string starts with a command prefix ("/").
     *
     * @param msg the message to check
     * @return true if the message starts with "/"
     */
    public static boolean isCommand(String msg){
        return msg.startsWith("/");
    }
}