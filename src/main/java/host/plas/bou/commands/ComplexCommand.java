package host.plas.bou.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract command class that extends BukkitCommand and implements BetterCommand,
 * providing a base for complex commands that are registered dynamically via the command map.
 * Subclasses should override the command methods to implement their behavior.
 */
@Getter @Setter
public abstract class ComplexCommand extends BukkitCommand implements BetterCommand {
    private String identifier;
    private String commandName;
    private JavaPlugin provider;
    private boolean registered;
    /**
     * The execution handler invoked when this command is run.
     *
     * @param executionHandler the execution handler to set
     * @return the execution handler
     */
    private CommandExecution executionHandler;
    /**
     * The tab completer invoked for tab completion of this command.
     *
     * @param tabCompleter the tab completer to set
     * @return the tab completer
     */
    private CommandTabCompleter tabCompleter;
    private String basePermission;

    /**
     * Constructs a ComplexCommand with a specified name, label, provider, and permission, then registers it.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param basePermission the base permission required to use this command
     */
    public ComplexCommand(String commandName, String label, JavaPlugin provider, String basePermission) {
        super(commandName);
        this.identifier = CommandHandler.getIdentifier(commandName, provider);
        this.commandName = commandName;

        this.setLabel(label);

        this.provider = provider;

        this.executionHandler = CommandExecution.emptyFalse();
        this.tabCompleter = CommandTabCompleter.empty();

        this.basePermission = basePermission;

        registerAndSet();
    }

    /**
     * Constructs a ComplexCommand using the provider name as the label.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     * @param basePermission the base permission required
     */
    public ComplexCommand(String commandName, JavaPlugin provider, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, basePermission);
    }

    /**
     * Constructs a ComplexCommand with a null permission sentinel.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     */
    public ComplexCommand(String commandName, String label, JavaPlugin provider) {
        this(commandName, label, provider, CommandBuilder.NULL);
    }

    /**
     * Constructs a ComplexCommand using the provider name as the label and a null permission sentinel.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     */
    public ComplexCommand(String commandName, JavaPlugin provider) {
        this(commandName, provider, CommandBuilder.NULL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return BetterCommand.super.execute(sender, label, args);
    }
}
