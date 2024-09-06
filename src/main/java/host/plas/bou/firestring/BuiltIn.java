package host.plas.bou.firestring;

import host.plas.bou.commands.Sender;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.SenderUtils;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Optional;

@Getter
public enum BuiltIn {
    COMMAND_AS_CONSOLE(new FireStringThing("console", string -> {
        Bukkit.dispatchCommand(SenderUtils.getConsoleSender(), string);
    }, false)),
    MESSAGE_PLAYER(new FireStringThing("message", string -> {
        String[] split = string.split(" ", 2);
        String name = split[0];
        String message = split[1];

        Optional<Sender> sender = SenderUtils.getAsSender(name);
        sender.ifPresent(value -> value.sendMessage(message));
    }, false)),
    TITLE_PLAYER(new FireStringThing("title", string -> {
        String[] split = string.split(" ", 2);
        String name = split[0];
        String title = split[1];

        Optional<Sender> sender = SenderUtils.getAsSender(name);
        sender.ifPresent(value -> value.sendTitle(title));
    }, false)),
    BROADCAST_MESSAGE(new FireStringThing("broadcast", string -> {
        Bukkit.getOnlinePlayers().forEach(player -> {
            SenderUtils.getAsSender(player.getUniqueId().toString()).ifPresent(sender -> sender.sendMessage(string));
        });
        SenderUtils.getAsSender(BaseManager.getBaseConfig().getConsoleUUID()).ifPresent(sender -> sender.sendMessage(string));
    }, false)),
    BROADCAST_TITLE(new FireStringThing("broadcasttitle", string -> {
        Bukkit.getOnlinePlayers().forEach(player -> {
            SenderUtils.getAsSender(player.getUniqueId().toString()).ifPresent(sender -> sender.sendTitle(string));
        });
    }, false)),
    ;

    private final FireStringThing fireString;

    BuiltIn(FireStringThing fireString) {
        this.fireString = fireString;
    }
}
