package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.utils.SenderUtils;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Command that sends a chat message to a specified player by name or UUID.
 * Usage: /boumessage [player] [message ...]
 */
public class MessageCMD extends SimplifiedCommand {
    /**
     * Constructs the /boumessage command and registers it with the BukkitOfUtils plugin.
     */
    public MessageCMD() {
        super("boumessage", BukkitOfUtils.getInstance());
    }

    /**
     * Executes the boumessage command. Sends the concatenated message arguments
     * to the player identified by name or UUID in the first argument.
     *
     * @param ctx the command context containing the sender and arguments
     * @return true if the command executed successfully, false if arguments are insufficient
     */
    @Override
    public boolean command(CommandContext ctx) {
        if (! ctx.isArgUsable(1)) {
            ctx.sendMessage("&cUsage: /boumessage <player> [message ...]");
            return false;
        }

        String nameUuid = ctx.getStringArg(0);
        String message = ctx.concatAfter(0);
        SenderUtils.getAsSender(nameUuid).ifPresent(sender -> sender.sendMessage(message));

        return true;
    }

    /**
     * Provides tab-completion suggestions for the boumessage command.
     * Suggests online player names for the first argument and a placeholder for the message.
     *
     * @param ctx the command context containing the current arguments
     * @return a sorted set of tab-completion suggestions
     */
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
