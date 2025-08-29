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

    public ComplexCommand(String commandName, String label, JavaPlugin provider) {
        super(commandName);
        this.identifier = CommandHandler.getIdentifier(commandName, provider);
        this.commandName = commandName;

        this.setLabel(label);

        this.provider = provider;

        this.executionHandler = CommandExecution.emptyFalse();
        this.tabCompleter = CommandTabCompleter.empty();

        registerAndSet();
    }

    public ComplexCommand(String commandName, JavaPlugin provider) {
        this(commandName, provider.getName().toLowerCase(), provider);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return BetterCommand.super.execute(sender, label, args);
    }
}
