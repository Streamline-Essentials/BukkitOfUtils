package host.plas.bou.items;

import host.plas.bou.gui.InventorySheet;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class InventoryUtils {
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

    public static void clearInventory(Player player) {
        final ItemStack air = new ItemStack(Material.AIR);
        getInventoryContents(player).forEach((slot, stack) -> {
            if (stack == null) return;
            if (stack.getType() == Material.AIR) return;

            player.getInventory().setItem(slot, air);
        });
    }

    public static ConcurrentSkipListMap<Integer, ItemStack> getInventoryContents(Player player) {
        return getInventoryContents(player.getInventory());
    }

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

    public static void clearInventory(Inventory inventory) {
        final ItemStack air = new ItemStack(Material.AIR);
        getInventoryContents(inventory).forEach((slot, stack) -> {
            if (stack == null) return;
            if (stack.getType() == Material.AIR) return;

            inventory.setItem(slot, air);
        });
    }

    public static void setInventoryContents(Inventory inventory, ConcurrentSkipListMap<Integer, ItemStack> items) {
        items.forEach((slot, stack) -> {
            if (stack == null) return;
            if (stack.getType() == Material.AIR) return;

            inventory.setItem(slot, stack);
        });
    }

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

    public static void decrementItemAmount(Player player, ItemStack stack, int slot, int amount) {
        int currentAmount = stack.getAmount();
        int newAmount = currentAmount - amount;
        setItemAmount(player, stack, slot, newAmount);
    }

    public static void incrementItemAmount(Player player, ItemStack stack, int slot, int amount) {
        int currentAmount = stack.getAmount();
        int newAmount = currentAmount + amount;
        setItemAmount(player, stack, slot, newAmount);
    }

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

    public static void setGlowing(ItemStack stack) {
        setGlowing(stack, true);
    }

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
}
