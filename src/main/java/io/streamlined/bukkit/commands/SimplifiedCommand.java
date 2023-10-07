package io.streamlined.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentSkipListSet;

public abstract class SimplifiedCommand extends ComplexCommand {
    public SimplifiedCommand(String identifier, JavaPlugin provider) {
        super(identifier, provider);
    }

    @Override
    public boolean command(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return command(new CommandContext(sender, command, label, args));
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return tabComplete(new CommandContext(sender, command, label, args));
    }
}
