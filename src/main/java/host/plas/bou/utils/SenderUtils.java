package host.plas.bou.utils;

import host.plas.bou.commands.Sender;
import host.plas.bou.instances.BaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for resolving, wrapping, and broadcasting messages to command senders,
 * with special handling for the console sender.
 */
public class SenderUtils {
    /** Private constructor to prevent instantiation of this utility class. */
    private SenderUtils() {}
    /**
     * Formats a UUID string for the given name, returning the console UUID if the sender is the console.
     *
     * @param name      the player name to resolve
     * @param isConsole whether the sender is the console
     * @return the formatted UUID string
     */
    public static String formatUuid(String name, boolean isConsole) {
        return isConsole ? BaseManager.getBaseConfig().getConsoleUUID() : getUuidByName(name);
    }

    /**
     * Resolves a player name to its UUID string, returning the console UUID for the console name.
     *
     * @param name the player name to resolve
     * @return the UUID string for the player
     */
    public static String getUuidByName(String name) {
        if (name.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
            return BaseManager.getBaseConfig().getConsoleUUID();
        } else {
            return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
        }
    }

    /**
     * Formats a display name, returning the console name if the sender is the console.
     *
     * @param name      the player name
     * @param isConsole whether the sender is the console
     * @return the formatted name
     */
    public static String formatName(String name, boolean isConsole) {
        return isConsole ? BaseManager.getBaseConfig().getConsoleName() : name;
    }

    /**
     * Retrieves an OfflinePlayer by UUID string, returning empty for the console UUID.
     *
     * @param uuid the UUID string to look up
     * @return an Optional containing the OfflinePlayer, or empty if the UUID is the console UUID
     */
    public static Optional<OfflinePlayer> getOfflinePlayer(String uuid) {
        if (uuid.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
            return Optional.empty();
        } else {
            return Optional.of(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        }
    }

    /**
     * Checks whether the given UUID string represents the console.
     *
     * @param uuid the UUID string to check
     * @return true if the UUID matches the console UUID
     */
    public static boolean isConsole(String uuid) {
        try {
            return UuidUtils.toUuid(uuid).equals(BaseManager.getBaseConfig().getConsoleUUID());
        } catch (Throwable e) {
            return UuidUtils.toUuid(uuid).equals("%");
        }
    }

    /**
     * Resolves a name or UUID string to a CommandSender (player or console).
     *
     * @param nameOrUuid the player name or UUID string to resolve
     * @return an Optional containing the CommandSender if found, or empty otherwise
     */
    public static Optional<CommandSender> getSender(String nameOrUuid) {
        String uuid = UuidUtils.toUuid(nameOrUuid);
        if (uuid.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
            return Optional.of(getConsoleSender());
        } else {
            return Optional.ofNullable(Bukkit.getPlayer(UUID.fromString(uuid)));
        }
    }

    /**
     * Returns the Bukkit console command sender.
     *
     * @return the console CommandSender
     */
    public static CommandSender getConsoleSender() {
        return Bukkit.getConsoleSender();
    }

    /**
     * Resolves a name or UUID string to a wrapped Sender instance.
     *
     * @param nameOrUuid the player name or UUID string to resolve
     * @return an Optional containing the Sender if the underlying CommandSender is found
     */
    public static Optional<Sender> getAsSender(String nameOrUuid) {
        return getSender(nameOrUuid).map(Sender::new);
    }

    /**
     * Returns the console wrapped as a Sender instance.
     *
     * @return the console Sender
     */
    public static Sender getConsoleAsSender() {
        return new Sender(getConsoleSender());
    }

    /**
     * Wraps a CommandSender in a Sender instance.
     *
     * @param sender the command sender to wrap
     * @return a new Sender wrapping the given CommandSender
     */
    public static Sender getSender(CommandSender sender) {
        return new Sender(sender);
    }

    /**
     * Attempts to wrap an OfflinePlayer as a Sender, succeeding only if the player is online.
     *
     * @param player the offline player to wrap
     * @return an Optional containing the Sender if the player is online, or empty otherwise
     */
    public static Optional<Sender> getOfflineSender(OfflinePlayer player) {
        try {
            if (player instanceof Player) {
                return Optional.of(getSender((Player) player));
            } else {
                Player p = player.getPlayer();
                if (p == null) {
                    return Optional.empty();
                } else {
                    return Optional.of(getSender(p));
                }
            }
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    /**
     * Broadcasts a formatted message to all online players and the console.
     *
     * @param message the message to broadcast
     */
    public static void broadcast(String message) {
        broadcast(message, true);
    }

    /**
     * Broadcasts a formatted message to all online players, optionally including the console.
     *
     * @param message        the message to broadcast
     * @param includeConsole whether to also send the message to the console
     */
    public static void broadcast(String message, boolean includeConsole) {
        broadcast(message, includeConsole, true);
    }

    /**
     * Broadcasts a message to all online players, optionally including the console and applying formatting.
     *
     * @param message        the message to broadcast
     * @param includeConsole whether to also send the message to the console
     * @param format         whether to apply color formatting to the message
     */
    public static void broadcast(String message, boolean includeConsole, boolean format) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            getSender(player).sendMessage(message, format);
        });

        if (includeConsole) getConsoleAsSender().sendMessage(message, format);
    }
}
