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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Command that retrieves an item from the ItemFactory by plugin name and key,
 * then gives it to the executing player. Uses the RetrievableKey system to look up registered items.
 */
public class GetItemCMD extends SimplifiedCommand {
    /**
     * The command usage string.
     */
    public static final String USAGE = "/itemfactory <plugin> <item> (count)";

    /**
     * The largest count that can be given at once: a full 36-slot inventory of 64-stacks.
     */
    public static final int MAX_COUNT = 36 * 64;

    /**
     * Constructs the /itemfactory command and registers it with the BukkitOfUtils plugin.
     */
    public GetItemCMD() {
        super("item-factory", BukkitOfUtils.getInstance());
    }

    /**
     * Executes the item-factory command. Looks up an item by the given plugin name and key,
     * then adds it to the executing player's inventory.
     *
     * @param ctx the command context containing the sender and arguments
     * @return true if the item was found and given to the player, false otherwise
     */
    public boolean command(CommandContext ctx) {
        if (!ctx.isArgUsable(1)) {
            ctx.sendMessage("&cUsage: " + USAGE);
            return false;
        } else {
            Player player = ctx.getPlayerOrNull();
            if (player == null) {
                ctx.sendMessage("&cThis command can only be executed by a player.");
                return false;
            } else {
                String plugin = ctx.getStringArg(0);
                String key = ctx.getStringArg(1);

                Integer count = null;
                if (ctx.isArgUsable(2)) {
                    try {
                        count = Integer.parseInt(ctx.getStringArg(2));
                    } catch (NumberFormatException e) {
                        count = -1;
                    }
                    if (count < 1 || count > MAX_COUNT) {
                        ctx.sendMessage("&cCount must be a whole number from &b1 &cto &b" + MAX_COUNT + "&c.");
                        return false;
                    }
                }

                RetrievableKey k = RetrievableKey.of(plugin, key);
                ItemStack stack = (ItemStack)ItemFactory.getItem(k).orElse(null);
                if (stack == null) {
                    ctx.sendMessage("&cNo item found for key &b" + k.getIdentifier());
                    return false;
                } else if (count == null) {
                    InventoryUtils.addItemToPlayer(player, stack);
                    ctx.sendMessage("&eGave you item for key &b" + k.getIdentifier());
                    return true;
                } else {
                    giveCount(player, stack, count);
                    ctx.sendMessage("&eGave you &b" + count + "x &eitem for key &b" + k.getIdentifier());
                    return true;
                }
            }
        }
    }

    /**
     * Gives the player exactly {@code count} of the given item, split into stacks no larger than the
     * item's max stack size. Anything that does not fit in the inventory is dropped at the player.
     *
     * @param player the player to give the items to
     * @param stack  the item to give; its own amount is ignored
     * @param count  the total number of items to give
     */
    private static void giveCount(Player player, ItemStack stack, int count) {
        int maxStack = Math.max(1, stack.getMaxStackSize());
        List<ItemStack> stacks = new ArrayList<>();
        for (int remaining = count; remaining > 0; remaining -= maxStack) {
            ItemStack part = stack.clone();
            part.setAmount(Math.min(remaining, maxStack));
            stacks.add(part);
        }

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(stacks.toArray(new ItemStack[0]));
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    /**
     * Provides tab-completion suggestions for the item-factory command.
     * Suggests plugin names for the first argument and item keys for the second argument.
     *
     * @param ctx the command context containing the current arguments
     * @return a sorted set of tab-completion suggestions
     */
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        ConcurrentSkipListSet<String> completions = new ConcurrentSkipListSet<>();
        if (ctx.getArgCount() <= 1) {
            completions.addAll(ItemFactory.getPluginsWithItemsNames());
        }

        if (ctx.getArgCount() == 2) {
            String plugin = ctx.getStringArg(0);
            completions.addAll(ItemFactory.getItemKeysForPlugin(plugin));
        }

        if (ctx.getArgCount() == 3) {
            completions.add("1");
            completions.add("16");
            completions.add("32");
            completions.add("64");
        }

        return completions;
    }
}
