package host.plas.bou.owncmd;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.gui.menus.TaskMenu;
import host.plas.bou.items.ConvertableItemStack;
import host.plas.bou.items.ItemBin;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.scheduling.BaseRunnable;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class DebugCMD extends SimplifiedCommand {
    public DebugCMD() {
        super("boudebug", BukkitOfUtils.getInstance());
    }

    @Override
    public boolean command(CommandContext ctx) {
        if (! ctx.isArgUsable(0)) {
            ctx.sendMessage("&cUsage: /boudebug <action> [args]");
            return false;
        }

        String action = ctx.getStringArg(0);
        action = action.toLowerCase();

        CommandSender sender = ctx.getCommandSender();

        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        switch (action) {
            case "item-nbt":
                if (player == null) {
                    ctx.sendMessage("&cOnly players can use this command.");
                    return false;
                }

                ItemStack itemN = player.getInventory().getItemInMainHand();
                if (itemN.getType() == Material.AIR) {
                    itemN = player.getInventory().getItemInOffHand();
                }

                if (itemN.getType() == Material.AIR) {
                    ctx.sendMessage("&cYou must be holding an item.");
                    return false;
                }

                String nbtN = ItemUtils.getItemNBT(itemN);

                ctx.sendMessage("&bItem NBT&8: &7" + nbtN);
                break;
            case "item-nbt-strict":
                if (player == null) {
                    ctx.sendMessage("&cOnly players can use this command.");
                    return false;
                }

                ItemStack itemNs = player.getInventory().getItemInMainHand();
                if (itemNs.getType() == Material.AIR) {
                    itemNs = player.getInventory().getItemInOffHand();
                }

                if (itemNs.getType() == Material.AIR) {
                    ctx.sendMessage("&cYou must be holding an item.");
                    return false;
                }

                String nbtNs = ItemUtils.getItemNBTStrict(itemNs);

                String messageS = "&bItem NBT&8: &7" + nbtNs + " &e&lCLICK TO COPY";
                messageS = ColorUtils.colorizeHard(messageS);
                messageS = ColorUtils.colorAsString(messageS);
                ComponentBuilder componentBuilderS = new ComponentBuilder(messageS);
                ClickEvent clickEventS = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, nbtNs);
                componentBuilderS.event(clickEventS);

                ctx.sendMessage(messageS, componentBuilderS.create());
                break;
            case "list-bou-plugins":
                StringBuilder sbL = new StringBuilder("&bBOU Plugins&8: &7");

                int maxL = PluginUtils.getLoadedBOUPlugins().size();
                int iL = 0;
                for (BetterPlugin plugin : PluginUtils.getLoadedBOUPlugins()) {
                    if (iL == maxL - 1) {
                        sbL.append(plugin.getColorizedIdentifier());
                    } else {
                        sbL.append(plugin.getColorizedIdentifier()).append("&7").append(", ");
                    }

                    iL ++;
                }

                ctx.sendMessage(sbL.toString());

                break;
            case "store-item":
                if (player == null) {
                    ctx.sendMessage("&cOnly players can use this command.");
                    return false;
                }

                ItemStack itemST = player.getInventory().getItemInMainHand();
                if (itemST.getType() == Material.AIR) {
                    itemST = player.getInventory().getItemInOffHand();
                }

                if (itemST.getType() == Material.AIR) {
                    ctx.sendMessage("&cYou must be holding an item.");
                    return false;
                }

                ConvertableItemStack cItemStackST = new ConvertableItemStack(itemST);
                cItemStackST.prepare();
                cItemStackST.stash();

                ctx.sendMessage("&7Item stored at position&8: &a" + cItemStackST.getStashedId());
                break;
            case "get-item":
                if (player == null) {
                    ctx.sendMessage("&cOnly players can use this command.");
                    return false;
                }

                if (! ctx.isArgUsable(1)) {
                    ctx.sendMessage("&cUsage: /boudebug get-item <id>");
                    return false;
                }

                if (ctx.getIntArg(1).isEmpty()) {
                    ctx.sendMessage("&cThe ID must be an integer.");
                    return false;
                }

                int idG = ctx.getIntArg(1).get();

                if (! ItemBin.has(idG)) {
                    ctx.sendMessage("&cNo item found with that ID.");
                    return false;
                }

                ConvertableItemStack cItemStackG = ItemBin.get(idG).get();
                cItemStackG.prepare();

                Optional<ItemStack> itemOptionalG = cItemStackG.getItemStackOptional();
                if (itemOptionalG.isEmpty()) {
                    ctx.sendMessage("&cItem could not be parsed.");
                    return false;
                }

                ItemStack itemG = itemOptionalG.get();

                if (player.getInventory().firstEmpty() == -1) {
                    ctx.sendMessage("&cYour inventory is full.");
                    return false;
                }

                player.getInventory().addItem(itemG);

                ctx.sendMessage("&7Item added to your inventory&8!");
                break;
            case "make-item":
                if (player == null) {
                    ctx.sendMessage("&cOnly players can use this command.");
                    return false;
                }
                if (! ctx.isArgUsable(1)) {
                    ctx.sendMessage("&cUsage: /boudebug make-item <nbt>");
                    return false;
                }

                String nbt = ctx.concat(1, ctx.getArgs().size());

                Optional<ItemStack> itemOptional = ItemUtils.getItem(nbt);
                if (itemOptional.isEmpty()) {
                    ctx.sendMessage("&cItem could not be parsed.");
                    return false;
                }
                ItemStack item = itemOptional.get();

                if (player.getInventory().firstEmpty() == -1) {
                    ctx.sendMessage("&cYour inventory is full.");
                    return false;
                }

                player.getInventory().addItem(item);

                ctx.sendMessage("&7Item added to your inventory&8!");
                break;
            case "make-item-strict":
                if (player == null) {
                    ctx.sendMessage("&cOnly players can use this command.");
                    return false;
                }
                if (! ctx.isArgUsable(1)) {
                    ctx.sendMessage("&cUsage: /boudebug make-item <nbt>");
                    return false;
                }

                String serialized = ctx.concat(1, ctx.getArgs().size());

                Optional<ItemStack> itemOptionalStrict = ItemUtils.getItemStrict(serialized);
                if (itemOptionalStrict.isEmpty()) {
                    ctx.sendMessage("&cItem could not be parsed.");
                    return false;
                }
                ItemStack itemStrict = itemOptionalStrict.get();

                if (player.getInventory().firstEmpty() == -1) {
                    ctx.sendMessage("&cYour inventory is full.");
                    return false;
                }

                player.getInventory().addItem(itemStrict);

                ctx.sendMessage("&7Item added to your inventory&8!");
                break;
            case "uuid":
                if (! ctx.isArgUsable(1)) {
                    ctx.sendMessage("&cUsage: /boudebug get-uuid <name>");
                    return false;
                }

                String name = ctx.getStringArg(1);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                UUID uuid = offlinePlayer.getUniqueId();

                String message = "&7UUID of &b" + name + "&8: &7" + uuid + " &e&lCLICK TO COPY";
                message = ColorUtils.colorizeHard(message);
                message = ColorUtils.colorAsString(message);
                ComponentBuilder componentBuilder = new ComponentBuilder(message);
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, uuid.toString());
                componentBuilder.event(clickEvent);

                ctx.sendMessage(message, componentBuilder.create());
                break;
            case "up":
                if (player == null) {
                    ctx.sendMessage("&cOnly players can use this command.");
                    return false;
                }

                Location upLoc = LocationUtils.getTopLocation(player.getLocation());
                LocationUtils.teleport(player, upLoc);

                ctx.sendMessage("&7Teleported you up to a space available above you.");
                break;
            case "down":
                if (player == null) {
                    ctx.sendMessage("&cOnly players can use this command.");
                    return false;
                }

                Location downLoc = LocationUtils.searchForTopBlock(player.getLocation(), BlockFace.DOWN, true);
                LocationUtils.teleport(player, downLoc);

                ctx.sendMessage("&7Teleported you down to a space available below you.");
                break;
            case "top":
                if (player == null) {
                    ctx.sendMessage("&cOnly players can use this command.");
                    return false;
                }

                Location topLoc = LocationUtils.getTopMostTopLocation(player.getLocation());
                LocationUtils.teleport(player, topLoc);

                ctx.sendMessage("&7Teleported you to the top-most space available above you.");
                break;
            case "worlds":
                StringBuilder worldsList = new StringBuilder();

                WorldUtils.getWorldNames().forEach(world -> {
                    if (worldsList.length() > 0) {
                        worldsList.append("&7, ");
                    }
                    worldsList.append("&c").append(world);
                });

                if (worldsList.length() > 0) {
                    worldsList.delete(worldsList.length() - "&7, ".length(), worldsList.length()); // Remove "&7, " at the end

                    ctx.sendMessage("&bWorlds&8: &7" + worldsList);
                } else {
                    ctx.sendMessage("&cNo worlds found.");
                }

                ctx.sendMessage("&7Teleported you to the top-most space available above you.");
                break;
            case "tasks":
                if (! ctx.isArgUsable(1)) {
                    ctx.sendMessage("&cUsage: /boudebug tasks <action>");
                    return false;
                }

                String taskAction = ctx.getStringArg(1).toLowerCase();
                switch (taskAction) {
                    case "list":
                        String taskList = TaskManager.listTasks();
                        ctx.sendMessage(taskList);
                        break;
                    case "cancel":
                        if (! ctx.isArgUsable(2)) {
                            ctx.sendMessage("&cUsage: /boudebug tasks cancel <id>");
                            return false;
                        }

                        if (ctx.getIntArg(2).isEmpty()) {
                            ctx.sendMessage("&cThe ID must be an integer.");
                            return false;
                        }

                        int id = ctx.getIntArg(2).get();
                        TaskManager.cancel(id);

                        ctx.sendMessage("&7Task with ID &a" + id + " &7was &ccancelled&8.");
                        break;
                    case "pause":
                        if (! ctx.isArgUsable(2)) {
                            ctx.sendMessage("&cUsage: /boudebug tasks pause <id>");
                            return false;
                        }

                        if (ctx.getIntArg(2).isEmpty()) {
                            ctx.sendMessage("&cThe ID must be an integer.");
                            return false;
                        }

                        int idP = ctx.getIntArg(2).get();
                        BaseRunnable runnable = TaskManager.getRunnable(idP);
                        if (runnable == null) {
                            ctx.sendMessage("&cNo task found with that ID.");
                            return false;
                        }

                        runnable.pause();

                        ctx.sendMessage("&7Task with ID &a" + idP + " &7was &cpaused&8.");
                        break;
                    case "resume":
                        if (! ctx.isArgUsable(2)) {
                            ctx.sendMessage("&cUsage: /boudebug tasks resume <id>");
                            return false;
                        }

                        if (ctx.getIntArg(2).isEmpty()) {
                            ctx.sendMessage("&cThe ID must be an integer.");
                            return false;
                        }

                        int idR = ctx.getIntArg(2).get();
                        BaseRunnable runnableR = TaskManager.getRunnable(idR);
                        if (runnableR == null) {
                            ctx.sendMessage("&cNo task found with that ID.");
                            return false;
                        }

                        runnableR.resume();

                        ctx.sendMessage("&7Task with ID &a" + idR + " &7was &aresumed&8.");
                        break;
                    case "menu":
                        if (player == null) {
                            ctx.sendMessage("&cOnly players can use this command.");
                            return false;
                        }

                        TaskMenu.open(player);
                        ctx.sendMessage("&7Task menu opened.");
                        break;
                }
        }

        return true;
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        ConcurrentSkipListSet<String> completions = new ConcurrentSkipListSet<>();

        if (ctx.getArgs().size() <= 1) {
            completions.addAll(List.of(
                    "item-nbt", "list-bou-plugins", "store-item", "get-item", "make-item", "uuid",
                    "up", "down", "top", "tasks", "item-nbt-strict", "make-item-strict", "worlds"
            ));
        }

        if (ctx.getArgs().size() == 2) {
            if (ctx.getStringArg(0).equalsIgnoreCase("get-item")) {
                completions.addAll(ItemBin.getStashedIdsAsStrings());
            }
            if (ctx.getStringArg(0).equalsIgnoreCase("tasks")) {
                completions.addAll(List.of("list", "cancel", "pause", "resume", "menu", "restart-ticker"));
            }
            if (ctx.getStringArg(0).equalsIgnoreCase("uuid")) {
                Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).forEach(completions::add);
            }
        }

        if (ctx.getArgs().size() == 3) {
            if (ctx.getStringArg(1).equalsIgnoreCase("cancel") ||
                    ctx.getStringArg(1).equalsIgnoreCase("pause") ||
                    ctx.getStringArg(1).equalsIgnoreCase("resume")) {
                completions.addAll(TaskManager.getTaskIdsAsStrings());
            }
        }

        return completions;
    }
}
