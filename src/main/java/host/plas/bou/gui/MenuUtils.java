package host.plas.bou.gui;

import host.plas.bou.BukkitOfUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Utility class for GUI menu operations including computing outer border slots,
 * creating namespaced keys, and managing static/button metadata tags on items.
 */
public class MenuUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MenuUtils() {
        // utility class
    }

    /**
     * Computes the set of outer border slot indices for an inventory with the given number of rows.
     *
     * @param rows the number of rows (clamped between 1 and 6)
     * @return a sorted set of slot indices that form the outer border
     */
    public static ConcurrentSkipListSet<Integer> getOuter(int rows) {
        ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>();
        if (rows < 1) return set;
        if (rows > 6) rows = 6;

        // top and bottom
        for (int r = 1; r <= rows; r++) {
            for (int i = 1; i <= 9; i++) {
                int real = i * r - 1;

                if (real > 9 && real < 17) {
                    continue;
                }
                if (real > 18 && real < 26) {
                    continue;
                }
                if (real > 27 && real < 35) {
                    continue;
                }
                if (real > 36 && real < 44) {
                    continue;
                }
                if (real > 45 && real < 53) {
                    continue;
                }

                set.add(real);
            }
        }

        return set;
    }

    /**
     * Creates a namespaced key for inventory-related persistent data with the given suffix.
     *
     * @param string the suffix to append to the "inventory-" prefix
     * @return the namespaced key
     */
    public static NamespacedKey getInventoryKey(String string) {
        return new NamespacedKey(BukkitOfUtils.getInstance(), "inventory-" + string);
    }

    /**
     * Returns the namespaced key used to mark items as static (non-interactive).
     *
     * @return the static namespaced key
     */
    public static NamespacedKey getStaticKey() {
        return getInventoryKey("static");
    }

    /**
     * Returns the namespaced key used to mark items as buttons.
     *
     * @return the button namespaced key
     */
    public static NamespacedKey getButtonKey() {
        return getInventoryKey("button");
    }

    /**
     * Injects a static marker into the item's persistent data container,
     * preventing it from being moved by players.
     *
     * @param stack the item stack to mark as static
     */
    public static void injectStatic(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(getStaticKey(), PersistentDataType.INTEGER, 1);
            stack.setItemMeta(meta);
        }
    }

    /**
     * Injects a button marker into the item's persistent data container,
     * preventing it from being moved by players.
     *
     * @param stack the item stack to mark as a button
     */
    public static void injectButton(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(getButtonKey(), PersistentDataType.INTEGER, 1);
            stack.setItemMeta(meta);
        }
    }

    /**
     * Checks whether the given item stack is marked as static.
     *
     * @param stack the item stack to check
     * @return {@code true} if the item has the static marker, {@code false} otherwise
     */
    public static boolean isStatic(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(getStaticKey(), PersistentDataType.INTEGER);
    }

    /**
     * Checks whether the given item stack is marked as a button.
     *
     * @param stack the item stack to check
     * @return {@code true} if the item has the button marker, {@code false} otherwise
     */
    public static boolean isButton(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(getButtonKey(), PersistentDataType.INTEGER);
    }
}
