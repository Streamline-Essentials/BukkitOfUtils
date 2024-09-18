package host.plas.bou.utils;

import host.plas.bou.BetterPlugin;
import host.plas.bou.commands.Sender;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.UUID;

public class SenderUtils {
    public static String formatUuid(String name, boolean isConsole) {
        return isConsole ? BetterPlugin.getBaseConfig().getConsoleUUID() : getUuidByName(name);
    }

    public static String getUuidByName(String name) {
        if (name.equals(BetterPlugin.getBaseConfig().getConsoleUUID())) {
            return BetterPlugin.getBaseConfig().getConsoleUUID();
        } else {
            return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
        }
    }

    public static String formatName(String name, boolean isConsole) {
        return isConsole ? BetterPlugin.getBaseConfig().getConsoleName() : name;
    }

    public static Optional<OfflinePlayer> getOfflinePlayer(String uuid) {
        if (uuid.equals(BetterPlugin.getBaseConfig().getConsoleUUID())) {
            return Optional.empty();
        } else {
            return Optional.of(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        }
    }

    public static boolean isConsole(String uuid) {
        return UuidUtils.toUuid(uuid).equals(BetterPlugin.getBaseConfig().getConsoleUUID());
    }

    public static Optional<CommandSender> getSender(String nameOrUuid) {
        String uuid = UuidUtils.toUuid(nameOrUuid);
        if (uuid.equals(BetterPlugin.getBaseConfig().getConsoleUUID())) {
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
}
