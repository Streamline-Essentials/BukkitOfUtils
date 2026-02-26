package host.plas.bou.gui;

public class GuiHelper {
    public static final int ROW_SIZE = 9;
    public static final int ROW_SIZE_HOPPER = 5;
    public static final int ROW_SIZE_MACHINE = 3;
    public static final int MAX_ROWS = 6;
    public static final int MAX_ROWS_HOPPER = 1;
    public static final int MAX_ROWS_MACHINE = 3;
    public static final int MIDDLE_ICON = 5;
    public static final int MIDDLE_ICON_HOPPER = 3;
    public static final int MIDDLE_ICON_MACHINE = 2;
    public static final int INVENTORY_OFFSET = -1;

    public static int getSlot(int slot) {
        return slot + -1;
    }

    public static int getMiddleSlot() {
        return getMiddleSlot(1);
    }

    public static int getLastSlot(int rows) {
        return getSlot(getTotalSlots(rows));
    }

    public static int getTotalSlots(int rows) {
        return rows * 9;
    }

    public static int getMiddleSlot(int row) {
        return getSlot(getTotalSlots(row - 1) + 5);
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
