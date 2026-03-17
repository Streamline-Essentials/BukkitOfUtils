package host.plas.bou.firestring;

import host.plas.bou.commands.Sender;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.SenderUtils;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Optional;

/**
 * Enum containing built-in {@link FireString} consumers for common server operations
 * such as executing commands, sending messages, titles, and broadcasts.
 */
@Getter
public enum BuiltIn {
    /** Executes a command as the console sender. */
    COMMAND_AS_CONSOLE("console", string -> {
        Bukkit.dispatchCommand(SenderUtils.getConsoleSender(), string);
    }),
    /** Executes a command as a specific player. */
    COMMAND_AS_PLAYER("player", string -> {
        String[] split = string.split(" ", 2);
        String name = split[0];
        String command = split[1];

        Optional<Sender> sender = SenderUtils.getAsSender(name);
        sender.ifPresent(value -> value.executeCommand(command));
    }),
    /** Sends a chat message as the console sender. */
    CHAT_AS_CONSOLE("consolechat", string -> {
        TaskManager.use(SenderUtils.getConsoleAsSender(), sender -> sender.chatAs(string));
    }),
    /** Sends a chat message as a specific player. */
    CHAT_AS_PLAYER("playerchat", string -> {
        String[] split = string.split(" ", 2);
        String name = split[0];
        String message = split[1];

        Optional<Sender> sender = SenderUtils.getAsSender(name);
        sender.ifPresent(value -> value.chatAs(message));
    }),
    /** Sends a private message to a specific player. */
    MESSAGE_PLAYER("message", string -> {
        String[] split = string.split(" ", 2);
        String name = split[0];
        String message = split[1];

        Optional<Sender> sender = SenderUtils.getAsSender(name);
        sender.ifPresent(value -> value.sendMessage(message));
    }),
    /** Sends a title to a specific player. */
    TITLE_PLAYER("title", string -> {
        String[] split = string.split(" ", 2);
        String name = split[0];
        String title = split[1];

        Optional<Sender> sender = SenderUtils.getAsSender(name);
        sender.ifPresent(value -> value.sendTitle(title));
    }),
    /** Broadcasts a message to all online players and the console. */
    BROADCAST_MESSAGE("broadcast", string -> {
        Bukkit.getOnlinePlayers().forEach(player -> {
            SenderUtils.getAsSender(player.getUniqueId().toString()).ifPresent(sender -> sender.sendMessage(string));
        });
        SenderUtils.getAsSender(BaseManager.getBaseConfig().getConsoleUUID()).ifPresent(sender -> sender.sendMessage(string));
    }),
    /** Broadcasts a title to all online players. */
    BROADCAST_TITLE("broadcasttitle", string -> {
        Bukkit.getOnlinePlayers().forEach(player -> {
            SenderUtils.getAsSender(player.getUniqueId().toString()).ifPresent(sender -> sender.sendTitle(string));
        });
    }),
    ;

    /**
     * The string identifier used to match this built-in action.
     * @return the string identifier
     */
    private final String identifier;
    /**
     * The consumer that handles the fire string input for this action.
     * @return the fire string consumer
     */
    private final FireStringConsumer consumer;

    /**
     * Constructs a new BuiltIn fire string action.
     *
     * @param identifier the string identifier used to match this action
     * @param consumer   the consumer that handles the fire string input
     */
    BuiltIn(String identifier, FireStringConsumer consumer) {
        this.identifier = identifier;
        this.consumer = consumer;
    }
}
