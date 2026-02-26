//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.items.InventoryUtils;
import host.plas.bou.items.ItemFactory;
import host.plas.bou.items.retrievables.RetrievableKey;
import java.util.concurrent.ConcurrentSkipListSet;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetItemCMD extends SimplifiedCommand {
    public GetItemCMD() {
        super("item-factory", BukkitOfUtils.getInstance());
    }

    public boolean command(CommandContext ctx) {
        if (!ctx.isArgUsable(1)) {
            ctx.sendMessage("&cUsage: /item-factory <plugin> <key>");
            return false;
        } else {
            Player player = ctx.getPlayerOrNull();
            if (player == null) {
                ctx.sendMessage("&cThis command can only be executed by a player.");
                return false;
            } else {
                String plugin = ctx.getStringArg(0);
                String key = ctx.getStringArg(1);
                RetrievableKey k = RetrievableKey.of(plugin, key);
                ItemStack stack = (ItemStack)ItemFactory.getItem(k).orElse(null);
                if (stack == null) {
                    ctx.sendMessage("&cNo item found for key &b" + k.getIdentifier());
                    return false;
                } else {
                    InventoryUtils.addItemToPlayer(player, stack);
                    ctx.sendMessage("&eGave you item for key &b" + k.getIdentifier());
                    return true;
                }
            }
        }
    }

    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        ConcurrentSkipListSet<String> completions = new ConcurrentSkipListSet<>();
        if (ctx.getArgCount() <= 1) {
            completions.addAll(ItemFactory.getPluginsWithItemsNames());
        }

        if (ctx.getArgCount() == 2) {
            String plugin = ctx.getStringArg(0);
            completions.addAll(ItemFactory.getItemKeysForPlugin(plugin));
        }

        return completions;
    }
}
