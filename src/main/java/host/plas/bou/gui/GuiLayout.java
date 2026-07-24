package host.plas.bou.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared inventory geometry helpers for shell borders, content slots, and nav positions.
 */
public final class GuiLayout {
    public static final int SIZE_SMALL = 27;
    public static final int SIZE_MEDIUM = 36;
    public static final int SIZE_LARGE = 54;

    public static final int CREATE_SLOT = 42;
    public static final int HELP_SLOT = 44;

    public static final int OPTIONS_PREVIEW_SLOT = 13;
    public static final int OPTIONS_PRIMARY_SLOT = 11;
    public static final int OPTIONS_SECONDARY_SLOT = 15;
    public static final int OPTIONS_DELETE_SLOT = 22;

    /** @deprecated Use {@link #defaultBackSlot(int)} instead. */
    @Deprecated
    public static final int RETURN_SLOT = SIZE_LARGE - 5;
    /** @deprecated Use {@link #defaultBackSlot(int)} instead. */
    @Deprecated
    public static final int OPTIONS_RETURN_SLOT = SIZE_MEDIUM - 5;
    /** @deprecated Use {@link #pagePrevSlot(int)} with {@link #defaultBackSlot(int)} instead. */
    @Deprecated
    public static final int PAGE_PREV_SLOT = RETURN_SLOT - 1;
    /** @deprecated Use {@link #pageNextSlot(int)} with {@link #defaultBackSlot(int)} instead. */
    @Deprecated
    public static final int PAGE_NEXT_SLOT = RETURN_SLOT + 1;

    private GuiLayout() {
    }

    /** Center of the bottom row (column 5, index 4). */
    public static int defaultBackSlot(int size) {
        return size - 5;
    }

    public static int pagePrevSlot(int backSlot) {
        return backSlot - 1;
    }

    public static int pageNextSlot(int backSlot) {
        return backSlot + 1;
    }

    /** Top-center slot (column 5, index 4). */
    public static int defaultBalanceSlot(int size) {
        return 4;
    }

    public static int[] listContentSlots() {
        return listContentSlots(SIZE_LARGE);
    }

    /**
     * Inner content slots excluding the outer border ring and bottom navigation row.
     *
     * @param size inventory size in slots
     * @return ordered content slot indices
     */
    public static int[] listContentSlots(int size) {
        int rows = size / 9;
        int lastRow = rows - 1;
        List<Integer> slots = new ArrayList<>();
        for (int row = 1; row < lastRow; row++) {
            for (int col = 1; col <= 7; col++) {
                slots.add(row * 9 + col);
            }
        }
        int[] result = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            result[i] = slots.get(i);
        }
        return result;
    }

    public static int[] cornerSlots(int size) {
        int lastRowStart = size - 9;
        return new int[]{0, 8, lastRowStart, size - 1};
    }

    public static ItemStack[] createShell(int size, CornerColor cornerColor) {
        ItemStack[] contents = new ItemStack[size];
        fillShell(contents, size, cornerColor);
        return contents;
    }

    /**
     * Fills the outer edge with black stained glass, corners with the themed color,
     * and leaves the interior slots empty (air).
     *
     * @param contents    destination contents array
     * @param size        inventory size
     * @param cornerColor corner accent color
     */
    public static void fillShell(ItemStack[] contents, int size, CornerColor cornerColor) {
        ItemStack border = GuiItems.filler(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack corner = GuiItems.cornerPane(cornerColor == null ? CornerColor.YELLOW : cornerColor);
        Set<Integer> cornerSet = new HashSet<>();
        for (int slot : cornerSlots(size)) {
            cornerSet.add(slot);
        }

        int rows = size / 9;
        int lastRow = rows - 1;
        for (int slot = 0; slot < size && slot < contents.length; slot++) {
            if (cornerSet.contains(slot)) {
                contents[slot] = corner.clone();
                continue;
            }
            int row = slot / 9;
            int col = slot % 9;
            if (row == 0 || row == lastRow || col == 0 || col == 8) {
                contents[slot] = border.clone();
            }
        }
    }

    /**
     * @deprecated Use {@link #fillShell(ItemStack[], int, CornerColor)} instead.
     */
    @Deprecated
    public static void fillCorners(ItemStack[] contents, int size, CornerColor cornerColor) {
        fillShell(contents, size, cornerColor);
    }
}
