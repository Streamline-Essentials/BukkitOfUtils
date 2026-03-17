package host.plas.bou.commands;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A builder class for constructing BuildableCommand instances with a fluent API.
 * Provides chainable setter methods and multiple constructor overloads for convenience.
 */
@Getter
public class CommandBuilder {
    /** Default placeholder string for undefined values. */
    public static final String NOT_DEFINED = "Not defined.";
    /** Sentinel string indicating a null permission. */
    public static final String NULL = "--NULL--";

    /**
     * The name of the command.
     *
     * @param commandName the command name to set
     * @return the command name
     */
    private String commandName;
    /**
     * The label for the command.
     *
     * @param label the label to set
     * @return the label
     */
    private String label;
    /**
     * The plugin providing this command.
     *
     * @param provider the plugin provider to set
     * @return the plugin provider
     */
    private JavaPlugin provider;
    /**
     * The handler for command execution.
     *
     * @param executionHandler the execution handler to set
     * @return the execution handler
     */
    private CommandExecution executionHandler;
    /**
     * The handler for tab completion.
     *
     * @param tabCompleter the tab completer to set
     * @return the tab completer
     */
    private CommandTabCompleter tabCompleter;

    /**
     * The command description.
     *
     * @param description the description to set
     * @return the description
     */
    private String description;
    /**
     * The command usage string.
     *
     * @param usage the usage string to set
     * @return the usage string
     */
    private String usage;
    /**
     * The set of command aliases.
     *
     * @param aliases the aliases to set
     * @return the set of aliases
     */
    private ConcurrentSkipListSet<String> aliases;

    /**
     * The base permission required for the command.
     *
     * @param basePermission the base permission to set
     * @return the base permission
     */
    private String basePermission;

    /**
     * Constructs a CommandBuilder with all parameters specified.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     * @param description the command description
     * @param usage the command usage string
     * @param aliases the set of command aliases
     */
    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage, ConcurrentSkipListSet<String> aliases) {
        this.commandName = commandName;
        this.label = label;
        this.provider = provider;
        this.executionHandler = executionHandler;
        this.tabCompleter = tabCompleter;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
        this.basePermission = basePermission;
    }

    /**
     * Constructs a CommandBuilder with no aliases.
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
    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, usage, new ConcurrentSkipListSet<>());
    }

    /**
     * Constructs a CommandBuilder with default usage.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     * @param description the command description
     */
    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, NOT_DEFINED);
    }

    /**
     * Constructs a CommandBuilder with default description and usage.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     */
    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, NOT_DEFINED);
    }

    /**
     * Constructs a CommandBuilder with an execution handler and an empty tab completer.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param executionHandler the handler for command execution
     * @param basePermission the base permission required
     */
    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, String basePermission) {
        this(commandName, label, provider, executionHandler, CommandTabCompleter.empty(), basePermission);
    }

    /**
     * Constructs a CommandBuilder with a boolean execution result and a tab completer.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param bool if true, the execution handler always returns true; otherwise false
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     */
    public CommandBuilder(String commandName, String label, JavaPlugin provider, boolean bool, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, bool ? CommandExecution.emptyTrue() : CommandExecution.emptyFalse(), tabCompleter, basePermission);
    }

    /**
     * Constructs a CommandBuilder that always succeeds, with a tab completer.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param tabCompleter the handler for tab completion
     * @param basePermission the base permission required
     */
    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, true, tabCompleter, basePermission);
    }

    /**
     * Constructs a CommandBuilder that always succeeds, with no tab completer.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     * @param basePermission the base permission required
     */
    public CommandBuilder(String commandName, String label, JavaPlugin provider, String basePermission) {
        this(commandName, label, provider, true, CommandTabCompleter.empty(), basePermission);
    }

    /**
     * Constructs a CommandBuilder with a null permission sentinel.
     *
     * @param commandName the name of the command
     * @param label the label for the command
     * @param provider the plugin providing this command
     */
    public CommandBuilder(String commandName, String label, JavaPlugin provider) {
        this(commandName, label, provider, NULL);
    }

    /**
     * Constructs a CommandBuilder using the provider name as the label and a null permission sentinel.
     *
     * @param commandName the name of the command
     * @param provider the plugin providing this command
     */
    public CommandBuilder(String commandName, JavaPlugin provider) {
        this(commandName, provider.getName().toLowerCase(), provider, NULL);
    }

    /**
     * Sets the command name.
     *
     * @param commandName the command name to set
     * @return this builder for chaining
     */
    public CommandBuilder setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    /**
     * Sets the command label.
     *
     * @param label the label to set
     * @return this builder for chaining
     */
    public CommandBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Sets the plugin provider.
     *
     * @param provider the plugin provider to set
     * @return this builder for chaining
     */
    public CommandBuilder setProvider(JavaPlugin provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Sets the command execution handler.
     *
     * @param executionHandler the execution handler to set
     * @return this builder for chaining
     */
    public CommandBuilder setExecutionHandler(CommandExecution executionHandler) {
        this.executionHandler = executionHandler;
        return this;
    }

    /**
     * Sets the tab completer handler.
     *
     * @param tabCompleter the tab completer to set
     * @return this builder for chaining
     */
    public CommandBuilder setTabCompleter(CommandTabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }

    /**
     * Sets the command description.
     *
     * @param description the description to set
     * @return this builder for chaining
     */
    public CommandBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the command usage string.
     *
     * @param usage the usage string to set
     * @return this builder for chaining
     */
    public CommandBuilder setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    /**
     * Sets the command aliases from a ConcurrentSkipListSet.
     *
     * @param aliases the set of aliases to set
     * @return this builder for chaining
     */
    public CommandBuilder setAliases(ConcurrentSkipListSet<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * Sets the command aliases from a varargs array, replacing any existing aliases.
     *
     * @param aliases the aliases to set
     * @return this builder for chaining
     */
    public CommandBuilder setAliases(String... aliases) {
        if (this.aliases == null) this.aliases = new ConcurrentSkipListSet<>();
        this.aliases.clear();
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    /**
     * Adds additional aliases to the existing set.
     *
     * @param aliases the aliases to add
     * @return this builder for chaining
     */
    public CommandBuilder addAliases(String... aliases) {
        if (this.aliases == null) this.aliases = new ConcurrentSkipListSet<>();
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    /**
     * Removes the specified aliases from the existing set.
     *
     * @param aliases the aliases to remove
     * @return this builder for chaining
     */
    public CommandBuilder removeAliases(String... aliases) {
        if (this.aliases == null) this.aliases = new ConcurrentSkipListSet<>();
        Arrays.asList(aliases).forEach(this.aliases::remove);
        return this;
    }

    /**
     * Sets the base permission required for the command.
     *
     * @param basePermission the base permission to set
     * @return this builder for chaining
     */
    public CommandBuilder setBasePermission(String basePermission) {
        this.basePermission = basePermission;
        return this;
    }

    /**
     * Builds and returns a new BuildableCommand from this builder's configuration.
     *
     * @return a new BuildableCommand instance
     */
    public BuildableCommand build() {
        return new BuildableCommand(this);
    }
}
