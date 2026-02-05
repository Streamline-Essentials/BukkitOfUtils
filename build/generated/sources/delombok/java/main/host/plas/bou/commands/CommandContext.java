package host.plas.bou.commands;

import host.plas.bou.utils.UuidUtils;
import host.plas.bou.utils.obj.ContextedString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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

    public String getUuid() {
        return sender.getUuid();
    }

    public boolean isConsole() {
        return sender.isConsole();
    }

    public boolean isPlayer() {
        return !isConsole();
    }

    public boolean sendMessage(String message, boolean format) {
        return sender.sendMessage(message, format);
    }

    public boolean sendMessage(String message) {
        return sender.sendMessage(message);
    }

    public boolean sendMessage(BaseComponent... messages) {
        return sender.sendMessage(messages);
    }

    public boolean sendMessage(String alternate, BaseComponent... messages) {
        return sender.sendMessage(alternate, messages);
    }

    public boolean sendMessage(ComponentBuilder builder) {
        return sender.sendMessage(builder);
    }

    public boolean sendMessage(String alternate, ComponentBuilder builder) {
        return sender.sendMessage(alternate, builder);
    }

    public boolean sendTitle(String toParse) {
        return sender.sendTitle(toParse);
    }

    public boolean sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return sender.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public Player getPlayerOrNull() {
        return getPlayer().orElse(null);
    }

    public String getFullCommand() {
        return command.getName() + " " + getArgsAsString();
    }

    public String getArgsAsString() {
        return String.join(" ", getArgsAsStringArray());
    }

    public String[] getArgsAsStringArray() {
        String[] args = new String[getArgs().size()];
        for (int i = 0; i < getArgs().size(); i++) {
            args[i] = getArg(i).getContent();
        }
        return args;
    }

    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String... args) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED, args);
    }

    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String string, String separator) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED, string, separator);
    }

    public Command getCommand() {
        return this.command;
    }

    public String getLabel() {
        return this.label;
    }

    public Sender getSender() {
        return this.sender;
    }

    public CommandSender getCommandSender() {
        return this.commandSender;
    }

    public void setCommand(final Command command) {
        this.command = command;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public void setSender(final Sender sender) {
        this.sender = sender;
    }

    public void setCommandSender(final CommandSender commandSender) {
        this.commandSender = commandSender;
    }
}
