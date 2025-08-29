package host.plas.bou.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
public class BuildableCommand extends BukkitCommand implements BetterCommand {
    private String identifier;
    private String commandName;
    private JavaPlugin provider;
    private boolean registered;
    private CommandExecution executionHandler;
    private CommandTabCompleter tabCompleter;
    private String basePermission;

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

    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage, String... aliases) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, usage, new ConcurrentSkipListSet<>(java.util.Arrays.asList(aliases)));
    }

    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage, String... aliases) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, tabCompleter, basePermission, description, usage, aliases);
    }

    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, usage, new ConcurrentSkipListSet<>());
    }

    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description, String usage) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, tabCompleter, basePermission, description, usage);
    }

    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, description, CommandBuilder.NOT_DEFINED);
    }

    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission, String description) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, tabCompleter, basePermission, description);
    }

    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, executionHandler, tabCompleter, basePermission, CommandBuilder.NOT_DEFINED);
    }

    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, tabCompleter, basePermission);
    }

    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandExecution executionHandler, String basePermission) {
        this(commandName, label, provider, executionHandler, CommandTabCompleter.empty(), basePermission);
    }

    public BuildableCommand(String commandName, JavaPlugin provider, CommandExecution executionHandler, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, executionHandler, CommandTabCompleter.empty(), basePermission);
    }

    public BuildableCommand(String commandName, String label, JavaPlugin provider, boolean bool, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, bool ? CommandExecution.emptyTrue() : CommandExecution.emptyFalse(), tabCompleter, basePermission);
    }

    public BuildableCommand(String commandName, JavaPlugin provider, boolean bool, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, bool ? CommandExecution.emptyTrue() : CommandExecution.emptyFalse(), tabCompleter, basePermission);
    }

    public BuildableCommand(String commandName, String label, JavaPlugin provider, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, label, provider, true, tabCompleter, basePermission);
    }

    public BuildableCommand(String commandName, JavaPlugin provider, CommandTabCompleter tabCompleter, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, true, tabCompleter, basePermission);
    }

    public BuildableCommand(String commandName, String label, JavaPlugin provider, String basePermission) {
        this(commandName, label, provider, true, CommandTabCompleter.empty(), basePermission);
    }

    public BuildableCommand(String commandName, JavaPlugin provider, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, true, CommandTabCompleter.empty(), basePermission);
    }

    public BuildableCommand(String commandName, JavaPlugin provider) {
        this(commandName, provider, CommandBuilder.NULL);
    }

    public BuildableCommand(CommandBuilder builder) {
        this(builder.getCommandName(), builder.getLabel(), builder.getProvider(), builder.getExecutionHandler(), builder.getTabCompleter(),
                builder.getBasePermission(), builder.getDescription(), builder.getUsage(), builder.getAliases());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return BetterCommand.super.execute(sender, label, args);
    }

    @Override
    public boolean command(CommandContext ctx) {
        return executionHandler.apply(ctx);
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        return tabCompleter.apply(ctx);
    }
}