package host.plas.bou.utils;

import host.plas.bou.commands.Sender;
import host.plas.bou.instances.BaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class SenderUtils {
    public static String formatUuid(String name, boolean isConsole) {
        return isConsole ? BaseManager.getBaseConfig().getConsoleUUID() : getUuidByName(name);
    }

    public static String getUuidByName(String name) {
        if (name.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
            return BaseManager.getBaseConfig().getConsoleUUID();
        } else {
            return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
        }
    }

    public static String formatName(String name, boolean isConsole) {
        return isConsole ? BaseManager.getBaseConfig().getConsoleName() : name;
    }

    public static Optional<OfflinePlayer> getOfflinePlayer(String uuid) {
        if (uuid.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
            return Optional.empty();
        } else {
            return Optional.of(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        }
    }

    public static boolean isConsole(String uuid) {
        try {
            return UuidUtils.toUuid(uuid).equals(BaseManager.getBaseConfig().getConsoleUUID());
        } catch (Throwable e) {
            return UuidUtils.toUuid(uuid).equals("%");
        }
    }

    public static Optional<CommandSender> getSender(String nameOrUuid) {
        String uuid = UuidUtils.toUuid(nameOrUuid);
        if (uuid.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
            return Optional.of(getConsoleSender());
        } else {
            return Optional.ofNullable(Bukkit.getPlayer(UUID.fromString(uuid)));
        }
    }

    public static CommandSender getConsoleSender() {
        return Bukkit.getConsoleSender();
    }

    public static Optional<Sender> getAsSender(String nameOrUuid) {
        return getSender(nameOrUuid).map(Sender::new);
    }

    public static Sender getConsoleAsSender() {
        return new Sender(getConsoleSender());
    }

    public static Sender getSender(CommandSender sender) {
        return new Sender(sender);
    }

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

    public static void broadcast(String message) {
        broadcast(message, true);
    }

    public static void broadcast(String message, boolean includeConsole) {
        broadcast(message, includeConsole, true);
    }

    public static void broadcast(String message, boolean includeConsole, boolean format) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            getSender(player).sendMessage(message, format);
        });

        if (includeConsole) getConsoleAsSender().sendMessage(message, format);
    }
}
