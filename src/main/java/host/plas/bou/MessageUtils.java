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

//    public static String loggedModulePrefix(ModuleLike module) {
//        return "[" + module.getIdentifier() + "] ";
//    }
//
//    public static void logInfo(ModuleLike module, String message) {
//        message = message.replace("%newline%", "\n");
//        for (String line : message.split("\n")) {
//            logInfo(loggedModulePrefix(module) + line);
//        }
//    }
//
//    public static void logWarning(ModuleLike module, String message) {
//        message = message.replace("%newline%", "\n");
//        for (String line : message.split("\n")) {
//            logWarning(loggedModulePrefix(module) + line);
//        }
//    }
//
//    public static void logSevere(ModuleLike module, String message) {
//        message = message.replace("%newline%", "\n");
//        for (String line : message.split("\n")) {
//            logSevere(loggedModulePrefix(module) + line);
//        }
//    }
//
//    public static void logDebug(ModuleLike module, String message) {
//        message = message.replace("%newline%", "\n");
//        for (String line : message.split("\n")) {
//            logDebug(loggedModulePrefix(module) + line);
//        }
//    }

//    public static void logInfo(ModuleLike module, StackTraceElement[] elements) {
//        Arrays.stream(elements).forEach(stackTraceElement -> {
//            logInfo(loggedModulePrefix(module) + stackTraceElement);
//        });
//    }
//
//    public static void logWarning(ModuleLike module, StackTraceElement[] elements) {
//        Arrays.stream(elements).forEach(stackTraceElement -> {
//            logWarning(loggedModulePrefix(module) + stackTraceElement);
//        });
//    }
//
//    public static void logSevere(ModuleLike module, StackTraceElement[] elements) {
//        Arrays.stream(elements).forEach(stackTraceElement -> {
//            logSevere(loggedModulePrefix(module) + stackTraceElement);
//        });
//    }
//
//    public static void logDebug(ModuleLike module, StackTraceElement[] elements) {
//        Arrays.stream(elements).forEach(stackTraceElement -> {
//            logDebug(loggedModulePrefix(module) + stackTraceElement);
//        });
//    }

//    public static void sendMessage(String to, String message) {
//        StreamlineUser user = UserUtils.getOrGetUser(to);
//
//        sendMessage(user, message);
//    }

//    public static void sendMessage(@Nullable String to, String otherUUID, String message) {
//        StreamlineUser user = UserUtils.getOrGetUser(to);
//
//        sendMessage(user, replaceAllPlayerBungee(otherUUID, message));
//    }

//    public static String replaceAllPlayerBungee(StreamlineUser user, String of) {
//        if (user == null) return of;
//
////        return SLAPI.getRatAPI().parseAllPlaceholders(user, of).completeOnTimeout(of, 77, TimeUnit.MILLISECONDS).join();
//        return ModuleUtils.replacePlaceholders(user, of);
//    }
//
//    public static String replaceAllPlayerBungee(String uuid, String of) {
//        return replaceAllPlayerBungee(UserUtils.getOrGetUser(uuid), of);
//    }
}