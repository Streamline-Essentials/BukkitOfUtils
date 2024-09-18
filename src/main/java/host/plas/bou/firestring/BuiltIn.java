package host.plas.bou.firestring;

import host.plas.bou.commands.Sender;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.SenderUtils;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Optional;

@Getter
public enum BuiltIn {
    COMMAND_AS_CONSOLE("console", string -> {
        Bukkit.dispatchCommand(SenderUtils.getConsoleSender(), string);
    }),
    MESSAGE_PLAYER("message", string -> {
        String[] split = string.split(" ", 2);
        String name = split[0];
        String message = split[1];

        Optional<Sender> sender = SenderUtils.getAsSender(name);
        sender.ifPresent(value -> value.sendMessage(message));
    }),
    TITLE_PLAYER("title", string -> {
        String[] split = string.split(" ", 2);
        String name = split[0];
        String title = split[1];

        Optional<Sender> sender = SenderUtils.getAsSender(name);
        sender.ifPresent(value -> value.sendTitle(title));
    }),
    BROADCAST_MESSAGE("broadcast", string -> {
        Bukkit.getOnlinePlayers().forEach(player -> {
            SenderUtils.getAsSender(player.getUniqueId().toString()).ifPresent(sender -> sender.sendMessage(string));
        });
        SenderUtils.getAsSender(BaseManager.getBaseConfig().getConsoleUUID()).ifPresent(sender -> sender.sendMessage(string));
    }),
    BROADCAST_TITLE("broadcasttitle", string -> {
        Bukkit.getOnlinePlayers().forEach(player -> {
            SenderUtils.getAsSender(player.getUniqueId().toString()).ifPresent(sender -> sender.sendTitle(string));
        });
    }),
    ;

    private final String identifier;
    private final FireStringConsumer consumer;

    BuiltIn(String identifier, FireStringConsumer consumer) {
        this.identifier = identifier;
        this.consumer = consumer;
    }
}
