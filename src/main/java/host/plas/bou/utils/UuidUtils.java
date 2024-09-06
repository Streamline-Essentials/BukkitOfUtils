package host.plas.bou.utils;

import host.plas.bou.instances.BaseManager;
import org.bukkit.Bukkit;

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
            if (name.equals(BaseManager.getBaseConfig().getConsoleName()) || name.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
                return BaseManager.getBaseConfig().getConsoleUUID();
            } else {
                return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
            }
        }
    }

    public static String toName(String uuid) {
        if (isUuid(uuid)) {
            return Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
        } else {
            if (uuid.equals(BaseManager.getBaseConfig().getConsoleName()) || uuid.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
                return BaseManager.getBaseConfig().getConsoleUUID(); // We use the Console UUID internally.
            } else {
                return Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
            }
        }
    }
}
