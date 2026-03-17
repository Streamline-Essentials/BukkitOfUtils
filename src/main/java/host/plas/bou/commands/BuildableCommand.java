package host.plas.bou.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A command implementation that can be built and registered dynamically at runtime
 * without requiring a plugin.yml entry. Delegates execution and tab completion
 * to configurable handler functions.
 */
@Getter @Setter
public class BuildableCommand extends BukkitCommand implements BetterCommand {
    private String identifier;
    private String commandName;
    private JavaPlugin provider;
    private boolean registered;
    /**
     * The handler invoked when this command is executed.
     * @param executionHandler the execution handler to set
     * @return the execution handler
     */
    private CommandExecution executionHandler;
    /**
     * The handler invoked to provide tab-completion suggestions.
     * @param tabCompleter the tab completer to set
     * @return the tab completer
     */
    private CommandTabCompleter tabCompleter;
    private String basePermission;

    /**
     * Constructs a BuildableCommand with all configurable parameters and registers it.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required to use this command
     * @param description the command description
     * @param usage the command usage string
     * @param aliases the set of command aliases
     */
    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage, ConcurrentSkipListSet<String> aliases) {
        super(commandName, description, usage, new ArrayList<>(aliases));
        this.identifier = CommandHandler.getIdentifier(commandName, provider);
        this.commandName = commandName;
        this.provider = provider;
        this.executionHandler = executionHandler;
        this.tabCompleter = tabCompleter;
        this.basePermission = basePermission;

        setLabel(label);

        registerAndSet();
    }

    /**
     * Constructs a BuildableCommand with aliases specified as varargs.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     * @param description the command description
     * @param usage the command usage string
     * @param aliases the command aliases
     */
    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage, String... aliases) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, usage, new ConcurrentSkipListSet<>(java.util.Arrays.asList(aliases)));
    }

    /**
     * Constructs a BuildableCommand using the provider name as the label.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     * @param description the command description
     * @param usage the command usage string
     * @param aliases the command aliases
     */
    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage, String... aliases) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, tabCompleter, basePermission, description, usage, aliases);
    }

    /**
     * Constructs a BuildableCommand with no aliases.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     * @param description the command description
     * @param usage the command usage string
     */
    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, usage, new ConcurrentSkipListSet<>());
    }

    /**
     * Constructs a BuildableCommand with no aliases, using the provider name as the label.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     * @param description the command description
     * @param usage the command usage string
     */
    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, tabCompleter, basePermission, description, usage);
    }

    /**
     * Constructs a BuildableCommand with default usage string.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     * @param description the command description
     */
    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, CommandBuilder.NOT_DEFINED);
    }

    /**
     * Constructs a BuildableCommand with default usage string, using the provider name as the label.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     * @param description the command description
     */
    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, tabCompleter, basePermission, description);
    }

    /**
     * Constructs a BuildableCommand with default description and usage.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, CommandBuilder.NOT_DEFINED);
    }

    /**
     * Constructs a BuildableCommand with default description and usage, using the provider name as the label.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, tabCompleter, basePermission);
    }

    /**
     * Constructs a BuildableCommand with an execution handler and no tab completer.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, String basePermission) {
        this(commandName, label, provider, executionHandler, CommandTabCompleter.empty(), basePermission);
    }

    /**
     * Constructs a BuildableCommand with an execution handler, no tab completer, using the provider name as the label.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, CommandTabCompleter.empty(), basePermission);
    }

    /**
     * Constructs a BuildableCommand with a boolean result and a tab completer.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param bool if true, the execution handler always returns true; otherwise false
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, String label, JavaPlugin provider, boolean bool, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, bool ? CommandExecution.emptyTrue() : CommandExecution.emptyFalse(), tabCompleter, basePermission);
    }

    /**
     * Constructs a BuildableCommand with a boolean result and a tab completer, using the provider name as the label.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     * @param bool if true, the execution handler always returns true; otherwise false
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, JavaPlugin provider, boolean bool, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, bool ? CommandExecution.emptyTrue() : CommandExecution.emptyFalse(), tabCompleter, basePermission);
    }

    /**
     * Constructs a BuildableCommand that always succeeds, with a tab completer.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, true, tabCompleter, basePermission);
    }

    /**
     * Constructs a BuildableCommand that always succeeds, with a tab completer, using the provider name as the label.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, JavaPlugin provider, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, true, tabCompleter, basePermission);
    }

    /**
     * Constructs a BuildableCommand that always succeeds, with no tab completer.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, String label, JavaPlugin provider, String basePermission) {
        this(commandName, label, provider, true, CommandTabCompleter.empty(), basePermission);
    }

    /**
     * Constructs a BuildableCommand that always succeeds, with no tab completer, using the provider name as the label.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     * @param basePermission the base permission required
     */
    public BuildableCommand(String commandName, JavaPlugin provider, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, true, CommandTabCompleter.empty(), basePermission);
    }

    /**
     * Constructs a BuildableCommand with a null permission sentinel.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     */
    public BuildableCommand(String commandName, JavaPlugin provider) {
        this(commandName, provider, CommandBuilder.NULL);
    }

    /**
     * Constructs a BuildableCommand from a CommandBuilder.
     *
     * @param builder the command builder containing all configuration
     */
    public BuildableCommand(CommandBuilder builder) {
        this(builder.getCommandName(), builder.getLabel(), builder.getProvider(), builder.getExecutionHandler(), builder.getTabCompleter(),
                builder.getBasePermission(), builder.getDescription(), builder.getUsage(), builder.getAliases());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return BetterCommand.super.execute(sender, label, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return command(new CommandContext(sender, command, label, args));
    }

    /**
     * Executes the command using the configured execution handler, or falls back to the default implementation.
     *
     * @param ctx the command context
     * @return true if the command executed successfully
     */
    @Override
    public boolean command(CommandContext ctx) {
        CommandExecution execution = getExecutionHandler();
        if (execution == null || execution.isEmpty()) {
            return BetterCommand.super.command(ctx);
        } else {
            return execution.apply(ctx);
        }
    }

    /**
     * Provides tab completion using the configured tab completer, or falls back to the default implementation.
     *
     * @param ctx the command context
     * @return a set of tab completion suggestions
     */
    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        CommandTabCompleter execution = getTabCompleter();
        if (execution == null || execution.isEmpty()) {
            return BetterCommand.super.tabComplete(ctx);
        } else {
            return execution.apply(ctx);
        }
    }

    /**
     * Returns true since BuildableCommand instances are always buildable.
     *
     * @return true
     */
    @Override
    public boolean isBuildable() {
        return true;
    }
}
