package host.plas.bou.gui;

public class GuiHelper {
    public static final int ROW_SIZE = 9;
    public static final int ROW_SIZE_HOPPER = 5;
    public static final int ROW_SIZE_MACHINE = 3;

    public static final int MAX_ROWS = 6;
    public static final int MAX_ROWS_HOPPER = 1;
    public static final int MAX_ROWS_MACHINE = 3;

    public static final int MIDDLE_ICON = ROW_SIZE / 2 + 1;
    public static final int MIDDLE_ICON_HOPPER = ROW_SIZE_HOPPER / 2 + 1;
    public static final int MIDDLE_ICON_MACHINE = ROW_SIZE_MACHINE / 2 + 1;

    public static final int INVENTORY_OFFSET = -1;

    /**
     * Get the slot index adjusted for zero-based indexing
     * @param slot The one-based slot index
     * @return The zero-based slot index
     */
    public static int getSlot(int slot) {
        return slot + INVENTORY_OFFSET;
    }

    /**
     * Get the middle slot index for a standard inventory
     * @return The middle slot index
     */
    public static int getMiddleSlot() {
        return getMiddleSlot(1);
    }

    /**
     * Get the last slot index for a given number of rows
     * @param rows The number of rows
     * @return The last slot index
     */
    public static int getLastSlot(int rows) {
        return getSlot(getTotalSlots(rows));
    }

    /**
     * Get the total number of slots for a given number of rows
     * @param rows The number of rows
     * @return The total number of slots
     */
    public static int getTotalSlots(int rows) {
        return rows * ROW_SIZE;
    }

    /**
     * Get the middle slot of a specific row (1-indexed)
     * @param row The row number (1-indexed)
     * @return The middle slot of the specified row
     */
    public static int getMiddleSlot(int row) {
        return getSlot((getTotalSlots(row - 1)) + MIDDLE_ICON);
    }

    /**
     * Get the slot index based on row and column (1-indexed or 0-indexed)
     * @param row The row number (1-indexed)
     * @param column The column number (1-indexed or 0-indexed based on columnZeroIndexed)
     * @param columnZeroIndexed Whether the column is zero-indexed
     * @return The slot index
     */
    public static int getIndexedSlot(int row, int column, boolean columnZeroIndexed) {
        if (columnZeroIndexed) {
            column += 1;
        }
        return getSlot(getTotalSlots(row - 1) + column);
    }

    /**
     * Get the slot index based on row and column (0-indexed)
     * @param row The row number (1-indexed)
     * @param column The column number (0-indexed)
     * @return The slot index
     */
    public static int getIndexedSlot(int row, int column) {
        return getIndexedSlot(row, column, true);
    }
}
