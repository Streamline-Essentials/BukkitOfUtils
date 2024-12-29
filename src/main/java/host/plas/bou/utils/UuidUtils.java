package host.plas.bou.utils;

import host.plas.bou.instances.BaseManager;

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
}
