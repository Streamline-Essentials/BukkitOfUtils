package host.plas.bou.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentSkipListSet;

public abstract class SimplifiedCommand extends ComplexCommand {
    public SimplifiedCommand(String commandName, JavaPlugin provider) {
        super(commandName, provider);
    }

    public SimplifiedCommand(String commandName, String label, JavaPlugin provider) {
        super(commandName, label, provider);
    }

    @Override
    public boolean command(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return command(new CommandContext(sender, command, label, args));
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        return new ConcurrentSkipListSet<>();
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return tabComplete(new CommandContext(sender, command, label, args));
    }
}
