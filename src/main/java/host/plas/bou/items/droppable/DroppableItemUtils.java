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

public class DroppableItemUtils {
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

    public static ItemStack getEnchantmentBook(Enchantment enchantment, int level) {
        ItemStack stack = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
        if (meta == null) return stack;

        meta.addStoredEnchant(enchantment, level, true);
        stack.setItemMeta(meta);

        return stack;
    }
}
