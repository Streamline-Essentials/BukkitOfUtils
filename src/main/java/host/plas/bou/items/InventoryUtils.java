package host.plas.bou.items;

import host.plas.bou.gui.InventorySheet;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Utility class for inventory and item stack manipulation, providing methods for
 * slot lookups, item amount management, inventory clearing, and item modifications.
 */
public class InventoryUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private InventoryUtils() {
        // utility class
    }

    /**
     * Gets the inventory slot index corresponding to an equipment slot for a player.
     *
     * @param player the player whose inventory to reference
     * @param slot   the equipment slot to look up
     * @return the inventory slot index, or -1 if the slot is not recognized
     */
    public static int getSlotFromEquipment(Player player, EquipmentSlot slot) {
        int i = -1;

        switch (slot) {
            case HAND:
                i = player.getInventory().getHeldItemSlot();
                break;
            case OFF_HAND:
                i = 40;
                break;
            case HEAD:
                i = 39;
                break;
            case CHEST:
                i = 38;
                break;
            case LEGS:
                i = 37;
                break;
            case FEET:
                i = 36;
                break;
        }

        return i;
    }

    /**
     * Sets the amount of an item stack in a player's inventory at the given slot.
     * Handles amounts greater than 64 by splitting into multiple stacks,
     * and removes the item if the amount is zero or less.
     *
     * @param player the player whose inventory to modify
     * @param stack  the item stack to adjust
     * @param slot   the inventory slot index
     * @param amount the desired total amount
     */
    public static void setItemAmount(Player player, ItemStack stack, int slot, int amount) {
        if (amount > 64) {
            stack.setAmount(64);
            while (amount > 64) {
                amount -= 64;
                ItemStack clone = stack.clone();
                clone.setAmount(amount);
                player.getInventory().addItem(clone);
            }
        } else if (amount <= 0) {
            player.getInventory().setItem(slot, new ItemStack(Material.AIR));
        } else {
            stack.setAmount(amount);
            player.getInventory().setItem(slot, stack);
        }
    }

    /**
     * Clears all items from a player's inventory, replacing them with air.
     *
     * @param player the player whose inventory to clear
     */
    public static void clearInventory(Player player) {
        final ItemStack air = new ItemStack(Material.AIR);
        getInventoryContents(player).forEach((slot, stack) -> {
            if (stack == null) return;
            if (stack.getType() == Material.AIR) return;

            player.getInventory().setItem(slot, air);
        });
    }

    /**
     * Returns the contents of a player's inventory as a map of slot indices to item stacks.
     *
     * @param player the player whose inventory to read
     * @return a sorted map of slot indices to item stacks (air for empty slots)
     */
    public static ConcurrentSkipListMap<Integer, ItemStack> getInventoryContents(Player player) {
        return getInventoryContents(player.getInventory());
    }

    /**
     * Returns the contents of an inventory as a map of slot indices to item stacks.
     *
     * @param inventory the inventory to read
     * @return a sorted map of slot indices to item stacks (air for null slots)
     */
    public static ConcurrentSkipListMap<Integer, ItemStack> getInventoryContents(Inventory inventory) {
        ConcurrentSkipListMap<Integer, ItemStack> items = new ConcurrentSkipListMap<>();

        int i = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack == null) items.put(i, new ItemStack(Material.AIR));
            else items.put(i, stack);

            i ++;
        }

        return items;
    }

    /**
     * Clears all items from an inventory, replacing non-air items with air.
     *
     * @param inventory the inventory to clear
     */
    public static void clearInventory(Inventory inventory) {
        final ItemStack air = new ItemStack(Material.AIR);
        getInventoryContents(inventory).forEach((slot, stack) -> {
            if (stack == null) return;
            if (stack.getType() == Material.AIR) return;

            inventory.setItem(slot, air);
        });
    }

    /**
     * Sets the contents of an inventory from a map of slot indices to item stacks.
     * Skips null items and air items.
     *
     * @param inventory the inventory to populate
     * @param items     a map of slot indices to item stacks
     */
    public static void setInventoryContents(Inventory inventory, ConcurrentSkipListMap<Integer, ItemStack> items) {
        items.forEach((slot, stack) -> {
            if (stack == null) return;
            if (stack.getType() == Material.AIR) return;

            inventory.setItem(slot, stack);
        });
    }

    /**
     * Replaces a player's inventory contents with those from another inventory.
     * Clears the player's inventory first, then copies valid items.
     *
     * @param inventory the source inventory to copy from
     * @param player    the player whose inventory to replace
     */
    public static void setInventory(Inventory inventory, Player player) {
        clearInventory(player);

        getInventoryContents(inventory).forEach((slot, stack) -> {
            if (slot < 0) return;
            if (stack == null) return;
            if (stack.getType() == Material.AIR) return;
            if (slot >= inventory.getSize()) return; // >= because of 0 indexing

            player.getInventory().setItem(slot, stack);
        });
    }

    /**
     * Counts the total amount of items in an inventory that match the given predicate.
     *
     * @param inventory the inventory to search
     * @param predicate the condition to match items against
     * @return the total count of matching items
     */
    public static int getItemAmount(Inventory inventory, Predicate<ItemStack> predicate) {
        AtomicInteger amount = new AtomicInteger(0);
        getInventoryContents(inventory).forEach((slot, stack) -> {
            if (stack == null) return;

            if (predicate.test(stack)) {
                amount.getAndAdd(stack.getAmount());
            }
        });
        return amount.get();
    }

    /**
     * Resets an item stack's durability to maximum (zero damage).
     * Does nothing if the item is null, air, or not damageable.
     *
     * @param stack the item stack to repair
     */
    public static void setItemMaxDurability(ItemStack stack) {
        if (stack == null) return;
        if (stack.getType() == Material.AIR) return;
        if (! (stack instanceof Damageable)) return;
        Damageable damageable = (Damageable) stack.getItemMeta();
        if (damageable == null) return;

        int maxDurability = stack.getType().getMaxDurability();

        if (maxDurability > 0) {
            damageable.setDamage(0); // 0 is the max durability (0 damage)
            stack.setItemMeta((ItemMeta) damageable);
        }
    }

    /**
     * Decreases the amount of an item stack in a player's inventory by the given amount.
     *
     * @param player the player whose inventory to modify
     * @param stack  the item stack to decrement
     * @param slot   the inventory slot index
     * @param amount the amount to subtract
     */
    public static void decrementItemAmount(Player player, ItemStack stack, int slot, int amount) {
        int currentAmount = stack.getAmount();
        int newAmount = currentAmount - amount;
        setItemAmount(player, stack, slot, newAmount);
    }

    /**
     * Increases the amount of an item stack in a player's inventory by the given amount.
     *
     * @param player the player whose inventory to modify
     * @param stack  the item stack to increment
     * @param slot   the inventory slot index
     * @param amount the amount to add
     */
    public static void incrementItemAmount(Player player, ItemStack stack, int slot, int amount) {
        int currentAmount = stack.getAmount();
        int newAmount = currentAmount + amount;
        setItemAmount(player, stack, slot, newAmount);
    }

    /**
     * Finds the first empty slot in an inventory sheet.
     *
     * @param sheet the inventory sheet to search
     * @return the index of the first empty slot, or -1 if no empty slot exists
     */
    public static int getFistEmptySlot(InventorySheet sheet) {
        AtomicInteger slot = new AtomicInteger(0);
        AtomicBoolean found = new AtomicBoolean(false);
        sheet.getSlots().forEach(value -> {
            if (found.get()) return;
            int i = slot.incrementAndGet();
            if (value == null) {
                found.set(true);
            }
        });

        if (! found.get()) {
            return -1;
        }

        return slot.get();
    }

    /**
     * Sets or removes a glowing effect on an item stack by adding or removing
     * the DIG_SPEED enchantment with hidden enchant flags.
     *
     * @param stack   the item stack to modify
     * @param glowing true to add the glow effect, false to remove it
     */
    public static void setGlowing(ItemStack stack, boolean glowing) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        if (glowing) {
            meta.addEnchant(Enchantment.DIG_SPEED, 1, true);
        } else {
            meta.getEnchants().forEach((enchantment, integer) -> {
                if (enchantment == Enchantment.DIG_SPEED) {
                    meta.removeEnchant(enchantment);
                }
            });
        }

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        stack.setItemMeta(meta);
    }

    /**
     * Adds a glowing effect to an item stack.
     *
     * @param stack the item stack to make glow
     */
    public static void setGlowing(ItemStack stack) {
        setGlowing(stack, true);
    }

    /**
     * Removes all persistent data container keys belonging to a specific plugin from an item stack.
     *
     * @param stack  the item stack to strip keys from
     * @param plugin the plugin whose keys should be removed
     */
    public static void stripPluginKeys(ItemStack stack, JavaPlugin plugin) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().getKeys().forEach(key -> {
            if (key.getNamespace().equalsIgnoreCase(plugin.getName())) {
                meta.getPersistentDataContainer().remove(key);
            }
        });

        stack.setItemMeta(meta);
    }

    /**
     * Adds an item to a player's inventory. If the inventory is full, drops the item
     * at the player's location. Prefers placing items in the main hand slot if empty.
     *
     * @param player the player to give the item to
     * @param stack  the item stack to add
     */
    public static void addItemToPlayer(Player player, ItemStack stack) {
        addItemToPlayer(player, stack, true);
    }

    /**
     * Adds an item to a player's inventory. If the inventory is full and dropIfFull
     * is true, drops the item at the player's location. Prefers placing items in
     * the main hand slot if empty.
     *
     * @param player     the player to give the item to
     * @param stack      the item stack to add
     * @param dropIfFull whether to drop the item on the ground if the inventory is full
     */
    public static void addItemToPlayer(Player player, ItemStack stack, boolean dropIfFull) {
        if (player != null) {
            if (stack != null) {
                PlayerInventory inventory = player.getInventory();
                if (inventory.firstEmpty() == -1) {
                    if (dropIfFull) {
                        player.getWorld().dropItemNaturally(player.getLocation(), stack);
                    }
                } else {
                    int mainHandSlot = inventory.getHeldItemSlot();
                    if (inventory.getItemInMainHand() != null && inventory.getItemInMainHand().getType() != Material.AIR) {
                        inventory.addItem(new ItemStack[]{stack});
                    } else {
                        inventory.setItem(mainHandSlot, stack);
                    }
                }

            }
        }
    }
}
