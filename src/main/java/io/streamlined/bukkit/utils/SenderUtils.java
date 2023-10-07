package io.streamlined.bukkit.utils;

import io.streamlined.bukkit.BukkitBase;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.UUID;

public class SenderUtils {
    public static String formatUuid(String name, boolean isConsole) {
        return isConsole ? BukkitBase.getBaseConfig().getConsoleUUID() : getUuidByName(name);
    }

    public static String getUuidByName(String name) {
        if (name.equals(BukkitBase.getBaseConfig().getConsoleUUID())) {
            return BukkitBase.getBaseConfig().getConsoleUUID();
        } else {
            return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
        }
    }

    public static String formatName(String name, boolean isConsole) {
        return isConsole ? BukkitBase.getBaseConfig().getConsoleName() : name;
    }

    public static Optional<OfflinePlayer> getOfflinePlayer(String uuid) {
        if (uuid.equals(BukkitBase.getBaseConfig().getConsoleUUID())) {
            return Optional.empty();
        } else {
            return Optional.of(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        }
    }
}
