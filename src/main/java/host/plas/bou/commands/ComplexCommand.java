package host.plas.bou.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
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
}
