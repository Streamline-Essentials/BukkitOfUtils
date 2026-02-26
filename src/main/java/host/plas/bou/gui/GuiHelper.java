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

    public static int getSlot(int slot) {
        return slot + INVENTORY_OFFSET;
    }

    public static int getMiddleSlot() {
        return getMiddleSlot(1);
    }

    public static int getLastSlot(int rows) {
        return getSlot(getTotalSlots(rows));
    }

    public static int getTotalSlots(int rows) {
        return rows * ROW_SIZE;
    }

    public static int getMiddleSlot(int row) {
        return getSlot(getTotalSlots(row - 1) + MIDDLE_ICON);
    }

    public static int getIndexedSlot(int row, int column, boolean columnZeroIndexed) {
        if (columnZeroIndexed) {
            ++column;
        }

        return getSlot(getTotalSlots(row - 1) + column);
    }

    public static int getIndexedSlot(int row, int column) {
        return getIndexedSlot(row, column, true);
    }
}
