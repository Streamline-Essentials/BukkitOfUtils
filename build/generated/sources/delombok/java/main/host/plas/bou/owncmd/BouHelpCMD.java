package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.helpful.HelpfulPlugin;
import host.plas.bou.utils.PluginUtils;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.entity.Player;

public class BouHelpCMD extends SimplifiedCommand {
    public BouHelpCMD() {
        super("bouhelp", BukkitOfUtils.getInstance());
    }

    public boolean command(CommandContext ctx) {
        if (!ctx.isArgUsable(0)) {
            parseHelpful(BukkitOfUtils.getInstance(), ctx);
            return true;
        } else {
            String subCommand = ctx.getStringArg(0).toLowerCase();
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            PluginUtils.parseHelpfulPlugin(subCommand).ifPresentOrElse((plugin) -> {
                parseHelpful(plugin, ctx);
                atomicBoolean.set(true);
            }, () -> {
                ctx.sendMessage("&cNo info found for a plugin with the name &e" + subCommand + "&c.");
                atomicBoolean.set(false);
            });
            return atomicBoolean.get();
        }
    }

    public static void parseHelpful(HelpfulPlugin plugin, CommandContext ctx) {
        Player player = ctx.getPlayer().orElse(null);
        if (player == null) {
            plugin.sendHelpful(ctx.getCommandSender());
        } else {
            plugin.sendHelpfulGui(player);
        }
    }

    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        ConcurrentSkipListSet<String> completions = new ConcurrentSkipListSet<>();
        if (ctx.getArgCount() <= 1) {
            for(HelpfulPlugin plugin : PluginUtils.getHelpfulPlugins()) {
                completions.add(plugin.getIdentifier());
            }

            completions.add("bou");
        }

        return completions;
    }
}
