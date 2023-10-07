package io.streamlined.bukkit.commands;

import io.streamlined.bukkit.BukkitBase;
import io.streamlined.bukkit.MessageUtils;
import io.streamlined.bukkit.utils.SenderUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tv.quaint.objects.AtomicString;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter @Setter
public class Sender {
    private String uuid;
    private String name;

    public Sender(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public Sender(String name, boolean isConsole) {
        this(SenderUtils.formatUuid(name, isConsole), SenderUtils.formatName(name, isConsole));
    }

    public Sender(CommandSender sender) {
        this(sender.getName(), sender instanceof Player);
    }

    public boolean isConsole() {
        return Objects.equals(uuid, BukkitBase.getBaseConfig().getConsoleUUID());
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
