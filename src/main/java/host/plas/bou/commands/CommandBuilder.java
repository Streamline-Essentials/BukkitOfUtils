package host.plas.bou.commands;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public class CommandBuilder {
    public static final String NOT_DEFINED = "Not defined.";

    private String commandName;
    private String label;
    private JavaPlugin provider;
    private CommandExecution executionHandler;
    private CommandTabCompleter tabCompleter;

    private String description;
    private String usage;
    private ConcurrentSkipListSet<String> aliases;

    private String basePermission;

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

    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, usage, new ConcurrentSkipListSet<>());
    }

    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, NOT_DEFINED);
    }

    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, NOT_DEFINED);
    }

    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, String basePermission) {
        this(commandName, label, provider, executionHandler, CommandTabCompleter.empty(), basePermission);
    }

    public CommandBuilder(String commandName, String label, JavaPlugin provider, boolean bool, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, bool ? CommandExecution.emptyTrue() : CommandExecution.emptyFalse(), tabCompleter, basePermission);
    }

    public CommandBuilder(String commandName, String label, JavaPlugin provider, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, true, tabCompleter, basePermission);
    }

    public CommandBuilder(String commandName, String label, JavaPlugin provider, String basePermission) {
        this(commandName, label, provider, true, CommandTabCompleter.empty(), basePermission);
    }

    public CommandBuilder setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    public CommandBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public CommandBuilder setProvider(JavaPlugin provider) {
        this.provider = provider;
        return this;
    }

    public CommandBuilder setExecutionHandler(CommandExecution executionHandler) {
        this.executionHandler = executionHandler;
        return this;
    }

    public CommandBuilder setTabCompleter(CommandTabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }

    public CommandBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandBuilder setAliases(ConcurrentSkipListSet<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    public CommandBuilder setAliases(String... aliases) {
        if (this.aliases == null) this.aliases = new ConcurrentSkipListSet<>();
        this.aliases.clear();
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public CommandBuilder addAliases(String... aliases) {
        if (this.aliases == null) this.aliases = new ConcurrentSkipListSet<>();
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public CommandBuilder removeAliases(String... aliases) {
        if (this.aliases == null) this.aliases = new ConcurrentSkipListSet<>();
        Arrays.asList(aliases).forEach(this.aliases::remove);
        return this;
    }

    public CommandBuilder setBasePermission(String basePermission) {
        this.basePermission = basePermission;
        return this;
    }

    public BuildableCommand build() {
        return new BuildableCommand(this);
    }
}
