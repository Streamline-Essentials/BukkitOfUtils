package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.utils.SenderUtils;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Command that sends a title to a specified player by name or UUID.
 * Supports title and subtitle separated by a newline character, as well as
 * optional fade-in, stay, and fade-out timing parameters.
 */
public class TitleCMD extends SimplifiedCommand {
    /**
     * Constructs the /boutitle command and registers it with the BukkitOfUtils plugin.
     */
    public TitleCMD() {
        super("boutitle", BukkitOfUtils.getInstance());
    }

    /**
     * Executes the boutitle command. Sends the specified title to the player
     * identified by name or UUID in the first argument.
     *
     * @param ctx the command context containing the sender and arguments
     * @return true if the command executed successfully, false if arguments are insufficient
     */
    @Override
    public boolean command(CommandContext ctx) {
        if (! ctx.isArgUsable(1)) {
            ctx.sendMessage("&cUsage: /boutitle <player> [title ...]");
            return false;
        }

        String nameUuid = ctx.getStringArg(0);
        String title = ctx.concatAfter(0);
        SenderUtils.getAsSender(nameUuid).ifPresent(sender -> sender.sendTitle(title));

        return true;
    }

    /**
     * Provides tab-completion suggestions for the boutitle command.
     * Suggests online player names for the first argument and title format hints for the second.
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
            return new ConcurrentSkipListSet<>(List.of("title\\nsubtitle", "-fadeIn=10", "-stay=70", "-fadeOut=20"));
        }

        return new ConcurrentSkipListSet<>();
    }
}
