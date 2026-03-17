package host.plas.bou.items.retrievables;

import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

/**
 * Represents an item that can be retrieved on demand.
 * Extends {@link Supplier} to provide an {@link ItemStack} when called.
 */
public interface RetrievableItem extends Supplier<ItemStack> {
}
