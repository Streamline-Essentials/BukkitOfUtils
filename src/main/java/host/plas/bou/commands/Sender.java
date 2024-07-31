package host.plas.bou.commands;

import host.plas.bou.MessageUtils;
import host.plas.bou.BetterPlugin;
import host.plas.bou.utils.SenderUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tv.quaint.objects.AtomicString;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter @Setter
public class Sender {
    private String uuid;
    private String name;

    private CommandSender commandSender;

    public Sender(String uuid, String name, CommandSender sender) {
        this.uuid = uuid;
        this.name = name;
        this.commandSender = sender;
    }

    public Sender(String name, CommandSender sender, boolean isConsole) {
        this(SenderUtils.formatUuid(name, isConsole), SenderUtils.formatName(name, isConsole), sender);
    }

    public Sender(CommandSender sender) {
        this(sender.getName(), sender, Bukkit.getConsoleSender().equals(sender));
    }

    public boolean isConsole() {
        return Bukkit.getConsoleSender().equals(commandSender) || uuid.equals(BetterPlugin.getBaseConfig().getConsoleUUID());
    }

    public boolean isPlayer() {
        return ! isConsole();
    }

    public boolean sendMessage(String message, boolean format) {
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicString finalMessage = new AtomicString(message);

        if (format) {
            finalMessage.set(MessageUtils.codedString(message));
        }

        getCommandSender().ifPresent(sender -> {
            sender.sendMessage(finalMessage.get());

            success.set(true);
        });

        return success.get();
    }

    public boolean sendMessage(String message) {
        return sendMessage(message, true);
    }

    public Optional<OfflinePlayer> getOfflinePlayer() {
        return SenderUtils.getOfflinePlayer(uuid);
    }

    public Optional<Player> getPlayer() {
        return getOfflinePlayer().map(OfflinePlayer::getPlayer);
    }

    public Optional<CommandSender> getCommandSender() {
        if (isConsole()) {
            return Optional.of(Bukkit.getConsoleSender());
        } else {
            return getPlayer().map(p -> p);
        }
    }
}
