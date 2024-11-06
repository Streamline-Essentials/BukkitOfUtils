package host.plas.bou.owncmd;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.items.ConvertableItemStack;
import host.plas.bou.items.ItemBin;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.utils.PluginUtils;
import host.plas.bou.utils.SenderUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
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
                if (itemN.getType().isAir()) {
                    itemN = player.getInventory().getItemInOffHand();
                }

                if (itemN.getType().isAir()) {
                    ctx.sendMessage("&cYou must be holding an item.");
                    return false;
                }

                String nbtN = ItemUtils.getItemNBT(itemN);

                ctx.sendMessage("&bItem NBT&8: &7" + nbtN);
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
                if (itemST.getType().isAir()) {
                    itemST = player.getInventory().getItemInOffHand();
                }

                if (itemST.getType().isAir()) {
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
        }

        return true;
    }

    @Override
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        if (ctx.getArgs().size() <= 1) {
            return new ConcurrentSkipListSet<>(List.of("item-nbt", "list-bou-plugins", "store-item", "get-item"));
        }

        if (ctx.getArgs().size() == 2) {
            if (ctx.getStringArg(0).equalsIgnoreCase("get-item")) {
                return ItemBin.getStashedIdsAsStrings();
            }
        }

        return new ConcurrentSkipListSet<>();
    }
}
