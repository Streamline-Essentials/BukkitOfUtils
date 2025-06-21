package host.plas.bou.utils;

import host.plas.bou.instances.BaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class UuidUtils {
    public static boolean isUuid(String thing) {
        try {
            UUID.fromString(thing);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

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

    public static boolean isConsole(String thing) {
        return thing.equals(getConsoleName()) || thing.equals(getConsoleUUID());
    }

    public static String getConsoleName() {
        return BaseManager.getBaseConfig().getConsoleName();
    }

    public static String getConsoleUUID() {
        return BaseManager.getBaseConfig().getConsoleUUID();
    }

    public static boolean isValidPlayer(OfflinePlayer player) {
        if (player == null) return false;
        if (player.getName() == null || player.getName().isBlank()) return false;

        String name = UUIDFetcher.getName(player.getUniqueId());
        return name != null && !name.isBlank() && player.getName() != null && !player.getName().isBlank();
    }

    public static boolean isValidPlayerName(String playerName) {
        if (playerName == null) return false;
        if (playerName.isBlank()) return false;

        UUID uuid = UUIDFetcher.getUUID(playerName);
        try {
            return uuid != null && isValidPlayer(Bukkit.getOfflinePlayer(uuid));
        } catch (Throwable e) {
            return false; // If the player is not found or has no valid profile
        }
    }

    public static boolean isValidPlayerUUID(String uuid) {
        if (uuid == null) return false;
        if (uuid.isBlank()) return false;

        String name = UUIDFetcher.getName(UUID.fromString(uuid));
        try {
            return name != null && isValidPlayer(Bukkit.getOfflinePlayer(name));
        } catch (Throwable e) {
            return false; // If the player is not found or has no valid profile
        }
    }
}
