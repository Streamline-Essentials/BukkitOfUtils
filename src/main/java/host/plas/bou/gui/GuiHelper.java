package host.plas.bou.gui;

/**
 * Utility class providing constants and helper methods for GUI slot calculations.
 * Handles slot indexing, row sizes, and position calculations for different inventory types.
 */
public class GuiHelper {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GuiHelper() {
        // utility class
    }

    /** The number of slots per row in a standard chest inventory. */
    public static final int ROW_SIZE = 9;
    /** The number of slots per row in a hopper inventory. */
    public static final int ROW_SIZE_HOPPER = 5;
    /** The number of slots per row in a machine (dropper/dispenser) inventory. */
    public static final int ROW_SIZE_MACHINE = 3;

    /** The maximum number of rows in a standard chest inventory. */
    public static final int MAX_ROWS = 6;
    /** The maximum number of rows in a hopper inventory. */
    public static final int MAX_ROWS_HOPPER = 1;
    /** The maximum number of rows in a machine (dropper/dispenser) inventory. */
    public static final int MAX_ROWS_MACHINE = 3;

    /** The 1-based middle slot position in a standard chest row. */
    public static final int MIDDLE_ICON = ROW_SIZE / 2 + 1;
    /** The 1-based middle slot position in a hopper row. */
    public static final int MIDDLE_ICON_HOPPER = ROW_SIZE_HOPPER / 2 + 1;
    /** The 1-based middle slot position in a machine row. */
    public static final int MIDDLE_ICON_MACHINE = ROW_SIZE_MACHINE / 2 + 1;

    /** The offset applied when converting from 1-based slot numbers to 0-based indices. */
    public static final int INVENTORY_OFFSET = -1;

    /**
     * Converts a 1-based slot number to a 0-based inventory index by applying the inventory offset.
     *
     * @param slot the 1-based slot number
     * @return the 0-based inventory index
     */
    public static int getSlot(int slot) {
        return slot + INVENTORY_OFFSET;
    }

    /**
     * Returns the middle slot index for the first row.
     *
     * @return the 0-based index of the middle slot in the first row
     */
    public static int getMiddleSlot() {
        return getMiddleSlot(1);
    }

    /**
     * Returns the last slot index for a given number of rows.
     *
     * @param rows the number of rows
     * @return the 0-based index of the last slot
     */
    public static int getLastSlot(int rows) {
        return getSlot(getTotalSlots(rows));
    }

    /**
     * Calculates the total number of slots for a given number of rows.
     *
     * @param rows the number of rows
     * @return the total number of slots
     */
    public static int getTotalSlots(int rows) {
        return rows * ROW_SIZE;
    }

    /**
     * Returns the middle slot index for a given row (1-based).
     *
     * @param row the 1-based row number
     * @return the 0-based index of the middle slot in the specified row
     */
    public static int getMiddleSlot(int row) {
        return getSlot(getTotalSlots(row - 1) + MIDDLE_ICON);
    }

    /**
     * Returns the slot index for a given row and column position.
     *
     * @param row                the 1-based row number
     * @param column             the column number
     * @param columnZeroIndexed  if {@code true}, the column is treated as 0-based;
     *                           if {@code false}, the column is treated as 1-based
     * @return the 0-based inventory slot index
     */
    public static int getIndexedSlot(int row, int column, boolean columnZeroIndexed) {
        if (columnZeroIndexed) {
            ++column;
        }

        return getSlot(getTotalSlots(row - 1) + column);
    }

    /**
     * Returns the slot index for a given row and 0-based column position.
     *
     * @param row    the 1-based row number
     * @param column the 0-based column number
     * @return the 0-based inventory slot index
     */
    public static int getIndexedSlot(int row, int column) {
        return getIndexedSlot(row, column, true);
    }
}
