package host.plas.bou.items.droppable;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.ColorUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

/**
 * Utility class for reading droppable item configurations and creating
 * special item stacks such as potions and enchanted books.
 */
public class DroppableItemUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DroppableItemUtils() {
        // utility class
    }

    /**
     * Reads a droppable item configuration and converts it into an item stack.
     * Supports both direct item types (by material name or NBT string) and keyed types
     * such as potions and enchanted books.
     *
     * @param item the droppable item configuration to read
     * @return an Optional containing the resulting item stack, or empty if parsing fails
     */
    public static Optional<ItemStack> readDroppableItem(DroppableItem item) {
        DroppableItemType type = item.getType();
        String itemString = item.getItemString();
        int amount = item.pollRandomAmount();

        if (type == DroppableItemType.ITEM) {
            ItemStack stack = null;
            if (itemString.contains("{") && itemString.contains("}")) {
                try {
                    Optional<ItemStack> optional = ItemUtils.getItem(itemString);
                    if (optional.isPresent()) {
                        stack = optional.get();
                        stack.setAmount(amount);
                    }
                } catch (Throwable e) {
                    BukkitOfUtils.getInstance().logWarning("Failed to parse item: " + itemString, e);
                }
            } else {
                try {
                    stack = new ItemStack(Material.valueOf(itemString));
                    stack.setAmount(amount);
                } catch (Throwable e) {
                    BukkitOfUtils.getInstance().logWarning("Failed to parse item: " + itemString, e);
                }
            }

            return Optional.ofNullable(stack);
        } else {
            try {
                String[] split = itemString.split(":");
                DroppableItemKeyedType keyedType = DroppableItemKeyedType.valueOf(split[0]);
                switch (keyedType) {
                    case POTION_JUMP:
                        return Optional.of(getJumpPotion(Integer.parseInt(split[1]), Integer.parseInt(split[2])));
                    case POTION_LEVITATION:
                        return Optional.of(getLevitationPotion(Integer.parseInt(split[1]), Integer.parseInt(split[2])));
                    case ENCHANTED_BOOK:
                        Enchantment enchantment = Enchantment.getByName(split[1]);
                        if (enchantment == null) {
                            BukkitOfUtils.getInstance().logWarning("Failed to parse droppable item: Enchantment not found: " + split[1]);
                            return Optional.empty();
                        }

                        return Optional.of(getEnchantmentBook(enchantment, Integer.parseInt(split[2])));
                    default:
                        return Optional.empty();
                }
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logWarning("Failed to parse droppable item: ", e);
                return Optional.empty();
            }
        }
    }

    /**
     * Creates a jump boost potion item stack with the specified level and duration.
     *
     * @param level    the amplifier level of the jump boost effect
     * @param duration the duration of the effect in ticks
     * @return the configured potion item stack
     */
    public static ItemStack getJumpPotion(int level, int duration) {
        ItemStack stack = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        if (meta == null) return stack;

        meta.setColor(Color.fromRGB(0, 255, 0));
        meta.setDisplayName(ColorUtils.colorize("&aJump Boost"));
        meta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, duration, level), true);

        stack.setItemMeta(meta);

        return stack;
    }

    /**
     * Creates a levitation potion item stack with the specified level and duration.
     *
     * @param level    the amplifier level of the levitation effect
     * @param duration the duration of the effect in ticks
     * @return the configured potion item stack
     */
    public static ItemStack getLevitationPotion(int level, int duration) {
        ItemStack stack = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        if (meta == null) return stack;

        meta.setColor(Color.fromRGB(255, 255, 255));
        meta.setDisplayName(ColorUtils.colorize("&aLevitation"));
        meta.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, level), true);

        stack.setItemMeta(meta);

        return stack;
    }

    /**
     * Creates an enchanted book item stack with the specified enchantment and level.
     *
     * @param enchantment the enchantment to store on the book
     * @param level       the level of the enchantment
     * @return the configured enchanted book item stack
     */
    public static ItemStack getEnchantmentBook(Enchantment enchantment, int level) {
        ItemStack stack = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
        if (meta == null) return stack;

        meta.addStoredEnchant(enchantment, level, true);
        stack.setItemMeta(meta);

        return stack;
    }
}
