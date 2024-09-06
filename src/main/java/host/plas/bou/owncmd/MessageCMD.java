package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.Sender;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.utils.ClassHelper;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.utils.SenderUtils;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class MessageCMD extends SimplifiedCommand {
    public MessageCMD() {
        super("boumessage", BukkitOfUtils.getInstance());
    }

    @Override
    public boolean command(CommandContext ctx) {
        if (ctx.isArgUsable(1)) {
            ctx.sendMessage("&cUsage: /boumessage <player> [message ...]");
            return false;
        }

        String nameUuid = ctx.getStringArg(0);
        String message = ctx.concatAfter(0);
        SenderUtils.getAsSender(nameUuid).ifPresent(sender -> sender.sendMessage(message));

        return true;
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        if (ctx.getArgs().size() <= 1) {
            return EntityUtils.getOnlinePlayerNames();
        }

        if (ctx.getArgs().size() == 2) {
            return new ConcurrentSkipListSet<>(List.of("message..."));
        }

        return new ConcurrentSkipListSet<>();
    }
}
