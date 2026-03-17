package host.plas.bou.commands;

import host.plas.bou.utils.UuidUtils;
import host.plas.bou.utils.obj.ContextedString;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Encapsulates the context of a command execution, including the sender, command,
 * label, and parsed arguments. Provides convenience methods for sending messages
 * and accessing sender information.
 */
@Getter @Setter
public class CommandContext extends ContextedString<CommandArgument> {
    /** Supplier that creates a default (broken/empty) CommandArgument. */
    public static final Supplier<CommandArgument> ARGUMENT_CREATOR = CommandArgument::new;
    /** Function that creates a CommandArgument from an index and arguments array. */
    public static final BiFunction<Integer, String[], CommandArgument> ARGUMENT_CREATOR_INDEXED = (i, args) -> new CommandArgument(i, args[i]);

    /**
     * The Bukkit command being executed.
     *
     * @param command the command to set
     * @return the command
     */
    private Command command;
    /**
     * The alias used to invoke the command.
     *
     * @param label the label to set
     * @return the label
     */
    private String label;

    /**
     * The wrapped sender of the command.
     *
     * @param sender the sender to set
     * @return the sender
     */
    private Sender sender;
    /**
     * The raw Bukkit command sender.
     *
     * @param commandSender the command sender to set
     * @return the command sender
     */
    private CommandSender commandSender;

    /**
     * Constructs a CommandContext from the given sender, command, label, and arguments.
     *
     * @param sender the command sender
     * @param command the Bukkit command
     * @param label the alias used to invoke the command
     * @param args the command arguments
     */
    public CommandContext(CommandSender sender, Command command, String label, String... args) {
        super(ARGUMENT_CREATOR, ARGUMENT_CREATOR_INDEXED, args);

        this.command = command;
        this.label = label;

        this.sender = new Sender(sender);
        this.commandSender = sender;
    }

    /**
     * Gets the sender as a Player if the sender is a player.
     *
     * @return an Optional containing the Player, or empty if the sender is not a player
     */
    public Optional<Player> getPlayer() {
        if (isPlayer()) return Optional.of((Player) commandSender);
        else return Optional.empty();
    }

    /**
     * Gets the UUID of the sender.
     *
     * @return the sender's UUID string
     */
    public String getUuid() {
        return sender.getUuid();
    }

    /**
     * Checks whether the sender is the console.
     *
     * @return true if the sender is the console
     */
    public boolean isConsole() {
        return sender.isConsole();
    }

    /**
     * Checks whether the sender is a player.
     *
     * @return true if the sender is a player
     */
    public boolean isPlayer() {
        return ! isConsole();
    }

    /**
     * Sends a message to the sender with optional formatting.
     *
     * @param message the message to send
     * @param format whether to apply color formatting
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(String message, boolean format) {
        return sender.sendMessage(message, format);
    }

    /**
     * Sends a formatted message to the sender.
     *
     * @param message the message to send
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(String message) {
        return sender.sendMessage(message);
    }

    /**
     * Sends BaseComponent messages to the sender.
     *
     * @param messages the base components to send
     * @return true if the messages were sent successfully
     */
    public boolean sendMessage(BaseComponent... messages) {
        return sender.sendMessage(messages);
    }

    /**
     * Sends BaseComponent messages to the sender, with a plain text fallback.
     *
     * @param alternate the fallback plain text message if component sending fails
     * @param messages the base components to send
     * @return true if the messages were sent successfully
     */
    public boolean sendMessage(String alternate, BaseComponent... messages) {
        return sender.sendMessage(alternate, messages);
    }

    /**
     * Sends a message built from a ComponentBuilder to the sender.
     *
     * @param builder the component builder
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(ComponentBuilder builder) {
        return sender.sendMessage(builder);
    }

    /**
     * Sends a message built from a ComponentBuilder to the sender, with a plain text fallback.
     *
     * @param alternate the fallback plain text message if component sending fails
     * @param builder the component builder
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(String alternate, ComponentBuilder builder) {
        return sender.sendMessage(alternate, builder);
    }

    /**
     * Sends a title to the sender by parsing a string with optional parameters
     * for fade-in, stay, and fade-out durations.
     *
     * @param toParse the title string to parse
     * @return true if the title was sent successfully
     */
    public boolean sendTitle(String toParse) {
        return sender.sendTitle(toParse);
    }

    /**
     * Sends a title and subtitle to the sender with specified timing.
     *
     * @param title the title text
     * @param subtitle the subtitle text
     * @param fadeIn the fade-in duration in ticks
     * @param stay the stay duration in ticks
     * @param fadeOut the fade-out duration in ticks
     * @return true if the title was sent successfully
     */
    public boolean sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return sender.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Gets the sender as a Player, or null if the sender is not a player.
     *
     * @return the Player instance, or null
     */
    public Player getPlayerOrNull() {
        return getPlayer().orElse(null);
    }

    /**
     * Gets the full command string including the command name and all arguments.
     *
     * @return the full command string
     */
    public String getFullCommand() {
        return command.getName() + " " + getArgsAsString();
    }

    /**
     * Gets all arguments joined as a single space-separated string.
     *
     * @return the arguments as a string
     */
    public String getArgsAsString() {
        return String.join(" ", getArgsAsStringArray());
    }

    /**
     * Gets all arguments as an array of strings.
     *
     * @return the arguments as a string array
     */
    public String[] getArgsAsStringArray() {
        String[] args = new String[getArgs().size()];
        for (int i = 0; i < getArgs().size(); i++) {
            args[i] = getArg(i).getContent();
        }
        return args;
    }

    /**
     * Creates a set of CommandArgument instances from the given string arguments.
     *
     * @param args the string arguments to parse
     * @return a set of CommandArgument instances
     */
    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String... args) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED, args);
    }

    /**
     * Creates a set of CommandArgument instances by splitting a string with the given separator.
     *
     * @param string the string to split
     * @param separator the separator to split on
     * @return a set of CommandArgument instances
     */
    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String string, String separator) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED, string, separator);
    }
}
