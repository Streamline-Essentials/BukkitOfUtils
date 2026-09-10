package host.plas.bou.gui;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.LegacySupport;
import host.plas.bou.gui.slots.SlotType;
import host.plas.bou.items.PdcTags;
import org.bukkit.inventory.ItemStack;

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

    /** The key name used to mark items as static (non-interactive). */
    private static final String STATIC_KEY = "inventory-static";

    /** The key name used to mark items as buttons. */
    private static final String BUTTON_KEY = "inventory-button";

    /**
     * Injects a static marker into the item's persistent data container,
     * preventing it from being moved by players.
     *
     * <p>On legacy servers without the persistent data container API (1.8-1.13) this is a
     * no-op. Click protection there is handled by
     * {@link GuiMaintenanceListener}, which resolves the slot type from the open
     * {@link host.plas.bou.gui.screens.ScreenInstance} rather than from item metadata.</p>
     *
     * @param stack the item stack to mark as static
     */
    public static void injectStatic(ItemStack stack) {
        if (stack == null) return;
        if (! LegacySupport.hasPersistentDataContainer()) return;

        PdcTags.setMarker(stack, PdcTags.key(BukkitOfUtils.getInstance(), STATIC_KEY));
    }

    /**
     * Injects a button marker into the item's persistent data container,
     * preventing it from being moved by players.
     *
     * <p>On legacy servers without the persistent data container API this is a no-op; see
     * {@link #injectStatic(ItemStack)}.</p>
     *
     * @param stack the item stack to mark as a button
     */
    public static void injectButton(ItemStack stack) {
        if (stack == null) return;
        if (! LegacySupport.hasPersistentDataContainer()) return;

        PdcTags.setMarker(stack, PdcTags.key(BukkitOfUtils.getInstance(), BUTTON_KEY));
    }

    /**
     * Checks whether the given item stack is marked as static.
     *
     * <p>Always {@code false} on legacy servers, where no marker is ever written.</p>
     *
     * @param stack the item stack to check
     * @return {@code true} if the item has the static marker, {@code false} otherwise
     */
    public static boolean isStatic(ItemStack stack) {
        if (stack == null) return false;
        if (! LegacySupport.hasPersistentDataContainer()) return false;

        return PdcTags.hasMarker(stack, PdcTags.key(BukkitOfUtils.getInstance(), STATIC_KEY));
    }

    /**
     * Checks whether the given item stack is marked as a button.
     *
     * <p>Always {@code false} on legacy servers, where no marker is ever written.</p>
     *
     * @param stack the item stack to check
     * @return {@code true} if the item has the button marker, {@code false} otherwise
     */
    public static boolean isButton(ItemStack stack) {
        if (stack == null) return false;
        if (! LegacySupport.hasPersistentDataContainer()) return false;

        return PdcTags.hasMarker(stack, PdcTags.key(BukkitOfUtils.getInstance(), BUTTON_KEY));
    }
}
