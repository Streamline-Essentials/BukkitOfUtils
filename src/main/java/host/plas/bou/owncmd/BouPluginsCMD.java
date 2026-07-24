package host.plas.bou.owncmd;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandBuilder;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.CommandResult;
import host.plas.bou.drakapi.VersionCheckResult;
import host.plas.bou.drakapi.VersionChecker;
import host.plas.bou.gui.menus.BouPluginInfoMenu;
import host.plas.bou.gui.menus.BouPluginsMenu;
import host.plas.bou.utils.PluginLifecycleHelper;
import host.plas.bou.utils.PluginUtils;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Command to manage BetterPlugins that implement BOU: {@code /bouplugins} ({@code /boup}).
 */
public final class BouPluginsCMD {
    private BouPluginsCMD() {
    }

    public static void register() {
        new CommandBuilder("bouplugins", BukkitOfUtils.getInstance())
                .addAliases("boup")
                .setBasePermission("bou.plugins")
                .setDescription("Manage BetterPlugins using BukkitOfUtils.")
                .setUsage("/boup <list|enable|disable|info|unload|load|menu> [plugin]")
                .setExecutionHandler(BouPluginsCMD::execute)
                .setTabCompleter(BouPluginsCMD::tabComplete)
                .build();
    }

    private static final String[] SUBCOMMANDS = {
            "list", "enable", "disable", "info", "unload", "load", "menu"
    };

    private static final String[] PLUGIN_ARG_SUBCOMMANDS = {
            "enable", "disable", "info", "unload", "menu"
    };

    private static boolean execute(CommandContext ctx) {
        if (!ctx.getCommandSender().hasPermission("bou.plugins")
                && !ctx.getCommandSender().isOp()) {
            ctx.sendMessage("&cYou do not have permission to use this command.");
            return CommandResult.FAILURE;
        }

        if (!ctx.isArgUsable(0)) {
            sendUsage(ctx);
            return CommandResult.FAILURE;
        }

        String sub = ctx.getStringArg(0).toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list":
                return list(ctx);
            case "info":
                return info(ctx);
            case "enable":
                return lifecycle(ctx, "enable");
            case "disable":
                return lifecycle(ctx, "disable");
            case "unload":
                return lifecycle(ctx, "unload");
            case "load":
                return load(ctx);
            case "menu":
                return menu(ctx);
            default:
                sendUsage(ctx);
                return CommandResult.FAILURE;
        }
    }

    private static void sendUsage(CommandContext ctx) {
        ctx.sendMessage("&e/boup list");
        ctx.sendMessage("&e/boup <enable|disable|info|unload|load> <plugin>");
        ctx.sendMessage("&e/boup menu [plugin]");
    }

    private static boolean list(CommandContext ctx) {
        ConcurrentSkipListSet<BetterPlugin> plugins = PluginUtils.getAllBOUPlugins();
        if (plugins.isEmpty()) {
            ctx.sendMessage("&cNo BetterPlugins are registered.");
            return CommandResult.SUCCESS;
        }
        ctx.sendMessage("&7Registered BetterPlugins &8(&a" + plugins.size() + "&8)&7:");
        for (BetterPlugin plugin : plugins) {
            ctx.sendMessage("&7- " + plugin.getColorizedIdentifier()
                    + " &7v&b" + plugin.getDescription().getVersion()
                    + " &8| &7uptime &b" + plugin.getFormattedUptime());
        }
        return CommandResult.SUCCESS;
    }

    private static boolean info(CommandContext ctx) {
        Optional<BetterPlugin> pluginOpt = requirePlugin(ctx, 1);
        if (!pluginOpt.isPresent()) return CommandResult.FAILURE;
        BetterPlugin plugin = pluginOpt.get();
        for (String line : plugin.getAsInfoComponent().split("\n")) {
            ctx.sendMessage(line);
        }

        String modrinthId = VersionChecker.resolveModrinthId(plugin);
        if (modrinthId == null || modrinthId.isBlank()) {
            ctx.sendMessage("  &f> &eModrinth&7: &cnot configured");
            return CommandResult.SUCCESS;
        }
        VersionCheckResult cached = VersionChecker.getCached(modrinthId);
        if (cached != null && cached.isSuccess()) {
            ctx.sendMessage("  &f> &eModrinth status&7: &e" + cached.getStatus().name()
                    + " &7(latest: &a" + cached.getLatestVersion() + "&7)");
        } else {
            ctx.sendMessage("  &f> &eModrinth&7: &eChecking...");
            VersionChecker.checkThen(plugin, result -> {
                if (result == null || !result.isSuccess()) {
                    ctx.sendMessage("  &f> &eModrinth&7: &c" + (result == null || result.getMessage() == null
                            ? "check failed" : result.getMessage()));
                    return;
                }
                ctx.sendMessage("  &f> &eModrinth status&7: &e" + result.getStatus().name()
                        + " &7(latest: &a" + result.getLatestVersion() + "&7)");
            });
        }
        return CommandResult.SUCCESS;
    }

    private static boolean lifecycle(CommandContext ctx, String action) {
        if (!ctx.isArgUsable(1)) {
            ctx.sendMessage("&cUsage: &e/boup " + action + " <plugin>");
            return CommandResult.FAILURE;
        }
        String name = ctx.getStringArg(1);
        PluginLifecycleHelper.Result result;
        switch (action) {
            case "enable":
                result = PluginLifecycleHelper.enable(name);
                break;
            case "disable":
                result = PluginLifecycleHelper.disable(name);
                break;
            case "unload":
                result = PluginLifecycleHelper.unload(name);
                break;
            default:
                result = PluginLifecycleHelper.Result.fail("Unknown action.");
                break;
        }
        ctx.sendMessage((result.isSuccess() ? "&a" : "&c") + result.getMessage());
        return result.isSuccess() ? CommandResult.SUCCESS : CommandResult.FAILURE;
    }

    private static boolean load(CommandContext ctx) {
        if (!ctx.isArgUsable(1)) {
            ctx.sendMessage("&cUsage: &e/boup load <plugin>");
            return CommandResult.FAILURE;
        }
        PluginLifecycleHelper.Result result = PluginLifecycleHelper.load(ctx.getStringArg(1));
        ctx.sendMessage((result.isSuccess() ? "&a" : "&c") + result.getMessage());
        return result.isSuccess() ? CommandResult.SUCCESS : CommandResult.FAILURE;
    }

    private static boolean menu(CommandContext ctx) {
        Optional<Player> playerOpt = ctx.getPlayer();
        if (!playerOpt.isPresent()) {
            ctx.sendMessage("&cOnly players can open the plugin menu.");
            return CommandResult.FAILURE;
        }
        Player player = playerOpt.get();
        if (!ctx.isArgUsable(1)) {
            BouPluginsMenu.open(player);
            return CommandResult.SUCCESS;
        }
        Optional<BetterPlugin> pluginOpt = requirePlugin(ctx, 1);
        if (!pluginOpt.isPresent()) return CommandResult.FAILURE;
        BouPluginInfoMenu.open(player, pluginOpt.get());
        return CommandResult.SUCCESS;
    }

    private static Optional<BetterPlugin> requirePlugin(CommandContext ctx, int argIndex) {
        if (!ctx.isArgUsable(argIndex)) {
            ctx.sendMessage("&cPlease specify a plugin name.");
            return Optional.empty();
        }
        String name = ctx.getStringArg(argIndex);
        Optional<BetterPlugin> plugin = PluginUtils.getPluginIgnoreCase(name);
        if (!plugin.isPresent()) {
            ctx.sendMessage("&cNo BetterPlugin found named &e" + name + "&c.");
        }
        return plugin;
    }

    private static ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        ConcurrentSkipListSet<String> out = new ConcurrentSkipListSet<>(String.CASE_INSENSITIVE_ORDER);
        int count = ctx.getArgCount();
        if (count <= 1) {
            out.addAll(Arrays.asList(SUBCOMMANDS));
            return out;
        }
        if (count == 2) {
            String sub = ctx.getStringArg(0).toLowerCase(Locale.ROOT);
            if (sub.equals("load")) {
                out.addAll(PluginLifecycleHelper.listPluginJarNames());
            } else if (Arrays.asList(PLUGIN_ARG_SUBCOMMANDS).contains(sub)) {
                for (BetterPlugin plugin : PluginUtils.getAllBOUPlugins()) {
                    out.add(plugin.getName());
                }
            }
        }
        return out;
    }
}
