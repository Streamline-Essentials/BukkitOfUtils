package host.plas.bou.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A simplified abstract command that extends ComplexCommand and provides
 * default implementations for command execution and tab completion delegation.
 * Subclasses only need to override the context-based command method.
 */
public abstract class SimplifiedCommand extends ComplexCommand {
    /**
     * Constructs a SimplifiedCommand with the given name and provider, using default label and permission.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     */
    public SimplifiedCommand(String commandName, JavaPlugin provider) {
        super(commandName, provider);
    }

    /**
     * Constructs a SimplifiedCommand with the given name, label, and provider.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     */
    public SimplifiedCommand(String commandName, String label, JavaPlugin provider) {
        super(commandName, label, provider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean command(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return command(new CommandContext(sender, command, label, args));
    }

    /**
     * Returns an empty set of tab completion suggestions. Override to provide suggestions.
     *
     * @param ctx the command context
     * @return an empty set of suggestions
     */
    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        return new ConcurrentSkipListSet<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentSkipListSet<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return tabComplete(new CommandContext(sender, command, label, args));
    }
}
