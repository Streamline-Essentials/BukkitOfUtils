package host.plas.bou.utils;

import host.plas.bou.instances.BaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Utility class for UUID validation, conversion between player names and UUIDs,
 * and player validity checks.
 */
public class UuidUtils {
    /** Private constructor to prevent instantiation of this utility class. */
    private UuidUtils() {}
    /**
     * Checks whether the given string is a valid UUID.
     *
     * @param thing the string to check
     * @return true if the string is a valid UUID
     */
    public static boolean isUuid(String thing) {
        try {
            UUID.fromString(thing);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Converts a player name to its UUID string. If the input is already a UUID, it is returned as-is.
     * Console names are mapped to the configured console UUID.
     *
     * @param name the player name or UUID to convert
     * @return the UUID string, or null if the player cannot be found
     */
    public static String toUuid(String name) {
        if (isUuid(name)) {
            return name;
        } else {
            if (isConsole(name)) {
                return getConsoleUUID();
            } else {
                UUID uuid = UUIDFetcher.getUUID(name);
                if (uuid == null) {
                    return null;
                } else {
                    return uuid.toString();
                }
            }
        }
    }

    /**
     * Converts a UUID string to a player name. Console UUIDs are mapped to the configured console name.
     *
     * @param uuid the UUID string to convert
     * @return the player name, or null if the player cannot be found
     */
    public static String toName(String uuid) {
        if (isUuid(uuid)) {
            return UUIDFetcher.getName(uuid);
        } else {
            if (isConsole(uuid)) {
                return getConsoleName();
            } else {
                return null;
            }
        }
    }

    /**
     * Checks whether the given string matches the console name or console UUID.
     *
     * @param thing the string to check
     * @return true if the string represents the console
     */
    public static boolean isConsole(String thing) {
        return thing.equals(getConsoleName()) || thing.equals(getConsoleUUID());
    }

    /**
     * Returns the configured console display name.
     *
     * @return the console name
     */
    public static String getConsoleName() {
        return BaseManager.getBaseConfig().getConsoleName();
    }

    /**
     * Returns the configured console UUID string.
     *
     * @return the console UUID
     */
    public static String getConsoleUUID() {
        return BaseManager.getBaseConfig().getConsoleUUID();
    }

    /**
     * Checks whether the given OfflinePlayer represents a valid player (non-null with a non-blank name).
     *
     * @param player the offline player to check
     * @return true if the player is valid
     */
    public static boolean isValidPlayer(OfflinePlayer player) {
        if (player == null) return false;
        if (player.getName() == null || player.getName().isBlank()) return false;

        return true;
    }

    /**
     * Checks whether a player name corresponds to a valid Minecraft player.
     * In offline mode, any non-blank name is considered valid.
     *
     * @param playerName the player name to validate
     * @return true if the player name is valid
     */
    public static boolean isValidPlayerName(String playerName) {
        if (playerName == null) return false;
        if (playerName.isBlank()) return false;

        if (isOfflineMode()) return true;

        try {
            return UUIDFetcher.getUUID(playerName) != null;
        } catch (Throwable e) {
            return false; // If the player is not found or has no valid profile
        }
    }

    /**
     * Checks whether a UUID string corresponds to a valid Minecraft player.
     * In offline mode, any non-blank UUID is considered valid.
     *
     * @param uuid the UUID string to validate
     * @return true if the UUID is valid
     */
    public static boolean isValidPlayerUUID(String uuid) {
        if (uuid == null) return false;
        if (uuid.isBlank()) return false;

        if (isOfflineMode()) return true;

        try {
            return UUIDFetcher.getName(uuid) != null;
        } catch (Throwable e) {
            return false; // If the player is not found or has no valid profile
        }
    }

    /**
     * Checks whether the server is running in offline (cracked) mode.
     *
     * @return true if the server is in offline mode
     */
    public static boolean isOfflineMode() {
        return ! Bukkit.getOnlineMode();
    }
}
