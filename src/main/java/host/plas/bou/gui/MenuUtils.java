package host.plas.bou.gui;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.gui.slots.SlotType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Utility class for GUI menu operations including computing outer border slots,
 * creating namespaced keys, and managing static/button metadata tags on items.
 */
public final class MenuUtils {
    private MenuUtils() {
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

        int size = rows * 9;
        int lastRow = rows - 1;
        for (int slot = 0; slot < size; slot++) {
            int row = slot / 9;
            int col = slot % 9;
            if (row == 0 || row == lastRow || col == 0 || col == 8) {
                set.add(slot);
            }
        }
        return set;
    }

    /**
     * Applies a themed shell (black border + colored corners) onto an {@link InventorySheet}.
     *
     * @param sheet       the sheet to modify
     * @param cornerColor the corner accent color
     */
    public static void applyShell(InventorySheet sheet, CornerColor cornerColor) {
        if (sheet == null) return;
        ItemStack[] shell = GuiLayout.createShell(sheet.getSize(), cornerColor == null ? CornerColor.YELLOW : cornerColor);
        for (int i = 0; i < shell.length; i++) {
            if (shell[i] != null) {
                sheet.setIcon(i, shell[i], SlotType.STATIC);
            }
        }
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
        if (stack == null) return;
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
        if (stack == null) return;
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
        if (stack == null) return false;
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
        if (stack == null) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(getButtonKey(), PersistentDataType.INTEGER);
    }
}
