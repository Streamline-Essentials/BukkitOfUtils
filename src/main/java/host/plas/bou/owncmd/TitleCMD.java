package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.utils.SenderUtils;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class TitleCMD extends SimplifiedCommand {
    public TitleCMD() {
        super("boutitle", BukkitOfUtils.getInstance());
    }

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
