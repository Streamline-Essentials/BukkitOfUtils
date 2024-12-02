package host.plas.bou.commands;

import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.obj.ContextedString;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Getter @Setter
public class CommandContext extends ContextedString<CommandArgument> {
    public static final Supplier<CommandArgument> ARGUMENT_CREATOR = CommandArgument::new;
    public static final BiFunction<Integer, String[], CommandArgument> ARGUMENT_CREATOR_INDEXED = (i, args) -> new CommandArgument(i, args[i]);

    private Command command;
    private String label;

    private Sender sender;
    private CommandSender commandSender;

    public CommandContext(CommandSender sender, Command command, String label, String... args) {
        super(ARGUMENT_CREATOR, ARGUMENT_CREATOR_INDEXED, args);

        this.command = command;
        this.label = label;

        this.sender = new Sender(sender);
        this.commandSender = sender;
    }

    public Optional<Player> getPlayer() {
        if (isPlayer()) return Optional.of((Player) commandSender);
        else return Optional.empty();
    }

    public boolean isConsole() {
        return sender.isConsole();
    }

    public boolean isPlayer() {
        return ! isConsole();
    }

    public boolean sendMessage(String message, boolean format) {
        return sender.sendMessage(message, format);
    }

    public boolean sendMessage(String message) {
        return sender.sendMessage(message);
    }

    public Player getPlayerOrNull() {
        return getPlayer().orElse(null);
    }

    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String... args) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED, args);
    }

    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String string, String separator) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED, string, separator);
    }
}
