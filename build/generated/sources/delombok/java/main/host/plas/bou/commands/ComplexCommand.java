package host.plas.bou.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class ComplexCommand extends BukkitCommand implements BetterCommand {
    private String identifier;
    private String commandName;
    private JavaPlugin provider;
    private boolean registered;
    private CommandExecution executionHandler;
    private CommandTabCompleter tabCompleter;
    private String basePermission;

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

    public ComplexCommand(String commandName, JavaPlugin provider, String basePermission) {
        this(commandName, provider.getName().toLowerCase(), provider, basePermission);
    }

    public ComplexCommand(String commandName, String label, JavaPlugin provider) {
        this(commandName, label, provider, CommandBuilder.NULL);
    }

    public ComplexCommand(String commandName, JavaPlugin provider) {
        this(commandName, provider, CommandBuilder.NULL);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return BetterCommand.super.execute(sender, label, args);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public JavaPlugin getProvider() {
        return this.provider;
    }

    public boolean isRegistered() {
        return this.registered;
    }

    public CommandExecution getExecutionHandler() {
        return this.executionHandler;
    }

    public CommandTabCompleter getTabCompleter() {
        return this.tabCompleter;
    }

    public String getBasePermission() {
        return this.basePermission;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public void setCommandName(final String commandName) {
        this.commandName = commandName;
    }

    public void setProvider(final JavaPlugin provider) {
        this.provider = provider;
    }

    public void setRegistered(final boolean registered) {
        this.registered = registered;
    }

    public void setExecutionHandler(final CommandExecution executionHandler) {
        this.executionHandler = executionHandler;
    }

    public void setTabCompleter(final CommandTabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
    }

    public void setBasePermission(final String basePermission) {
        this.basePermission = basePermission;
    }
}
