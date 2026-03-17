package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.helpful.HelpfulPlugin;
import host.plas.bou.utils.PluginUtils;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.entity.Player;

/**
 * Command that displays help information for BukkitOfUtils or any registered BOU plugin.
 * If a player executes this command, a GUI-based help menu is shown; otherwise, text help is sent.
 */
public class BouHelpCMD extends SimplifiedCommand {
    /**
     * Constructs the /bouhelp command and registers it with the BukkitOfUtils plugin.
     */
    public BouHelpCMD() {
        super("bouhelp", BukkitOfUtils.getInstance());
    }

    /**
     * Executes the bouhelp command. When no argument is given, shows help for BukkitOfUtils itself.
     * When a plugin name is provided as an argument, shows help for that specific plugin.
     *
     * @param ctx the command context containing the sender and arguments
     * @return true if the command executed successfully, false otherwise
     */
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

    /**
     * Sends help information for the given plugin to the command sender.
     * If the sender is a player, opens a GUI help menu; otherwise, sends text-based help.
     *
     * @param plugin the helpful plugin whose help information should be displayed
     * @param ctx the command context containing the sender
     */
    public static void parseHelpful(HelpfulPlugin plugin, CommandContext ctx) {
        Player player = ctx.getPlayer().orElse(null);
        if (player == null) {
            plugin.sendHelpful(ctx.getCommandSender());
        } else {
            plugin.sendHelpfulGui(player);
        }
    }

    /**
     * Provides tab-completion suggestions for the bouhelp command.
     * Suggests registered helpful plugin identifiers when completing the first argument.
     *
     * @param ctx the command context containing the current arguments
     * @return a sorted set of tab-completion suggestions
     */
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
