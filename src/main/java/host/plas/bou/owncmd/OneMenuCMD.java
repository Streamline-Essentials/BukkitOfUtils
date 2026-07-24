package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandBuilder;
import host.plas.bou.commands.CommandResult;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.onemenu.OneMenuManager;
import org.bukkit.entity.Player;

/**
 * Registers {@code /onemenu} (optional alias from config) when OneMenu is enabled.
 */
public final class OneMenuCMD {
    private OneMenuCMD() {
    }

    public static void registerIfEnabled() {
        if (!OneMenuManager.isEnabled()) {
            return;
        }

        OneMenuManager.ensureDefaults();

        CommandBuilder builder = new CommandBuilder("onemenu", BukkitOfUtils.getInstance())
                .setBasePermission("bou.onemenu")
                .setDescription("Open the configurable OneMenu GUI.")
                .setUsage("/onemenu")
                .setExecutionHandler(ctx -> {
                    if (!ctx.getCommandSender().hasPermission("bou.onemenu")
                            && !ctx.getCommandSender().isOp()) {
                        ctx.sendMessage("&cYou do not have permission to use this command.");
                        return CommandResult.FAILURE;
                    }
                    Player player = ctx.getPlayer().orElse(null);
                    if (player == null) {
                        ctx.sendMessage("&cOnly players can open OneMenu.");
                        return CommandResult.FAILURE;
                    }
                    OneMenuManager.openOneMenu(player);
                    return CommandResult.SUCCESS;
                });

        String alias = BaseManager.getBaseConfig().getOneMenuCommandAlias();
        if (alias != null && !alias.isBlank()) {
            builder.addAliases(alias.trim());
        }

        builder.build();
        BukkitOfUtils.getInstance().logInfo("OneMenu enabled (/onemenu"
                + (alias != null && !alias.isBlank() ? ", /" + alias.trim() : "")
                + ").");
    }
}
