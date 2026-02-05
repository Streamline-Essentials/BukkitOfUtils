package host.plas.bou.gui.menus;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.gui.GuiType;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.icons.BasicIcon;
import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.obj.ManagedInventory;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;
import java.util.function.BiFunction;

public class PaginatedMenu extends ScreenInstance {
    private ManagedInventory fullSlots;
    private int slotsPerPage;
    private int padLeft;
    private int padRight;
    private int padTop;
    private int padBottom;
    private BiFunction<Player, Integer, Icon> whenNotFilled; // player, sheet index -> icon
    private BiFunction<Player, Integer, Icon> whenFilled; // player, page index -> icon
    private int currentPage;

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int currentPage, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom, BiFunction<Player, Integer, Icon> whenNotFilled, BiFunction<Player, Integer, Icon> whenFilled) {
        super(player, type, buildSheet(player, fullSlots, currentPage, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, whenFilled));
        this.fullSlots = fullSlots;
        this.slotsPerPage = slotsPerPage;
        this.padLeft = padLeft;
        this.padRight = padRight;
        this.padTop = padTop;
        this.padBottom = padBottom;
        this.whenNotFilled = whenNotFilled;
        this.whenFilled = whenFilled;
        this.currentPage = currentPage;
    }

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom, BiFunction<Player, Integer, Icon> whenNotFilled, BiFunction<Player, Integer, Icon> whenFilled) {
        this(player, type, fullSlots, 1, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, whenFilled);
    }

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int currentPage, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom, BiFunction<Player, Integer, Icon> whenNotFilled) {
        this(player, type, fullSlots, currentPage, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, (p, i) -> null);
    }

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom, BiFunction<Player, Integer, Icon> whenNotFilled) {
        this(player, type, fullSlots, 1, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, (p, i) -> null);
    }

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int currentPage, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom) {
        this(player, type, fullSlots, currentPage, slotsPerPage, padLeft, padRight, padTop, padBottom, (p, i) -> null, (p, i) -> null);
    }

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom) {
        this(player, type, fullSlots, 1, slotsPerPage, padLeft, padRight, padTop, padBottom, (p, i) -> null, (p, i) -> null);
    }

    public static InventorySheet buildSheet(@NotNull Player player, ManagedInventory fullSlots, int page, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom, BiFunction<Player, Integer, Icon> whenNotFilled, BiFunction<Player, Integer, Icon> whenFilled) {
        InventorySheet sheet = new InventorySheet(6 * 9); // 6 rows of 9 slots
        try {
            int maxPages = getMaxPages(fullSlots, slotsPerPage);
            if (page < 1) page = 1;
            if (page > maxPages) page = maxPages;
            for (int i = 0; i < sheet.getSize(); i++) {
                try {
                    if (isFillable(sheet, i, slotsPerPage, padLeft, padRight, padTop, padBottom)) {
                        try {
                            int pageIndex = getPageIndexOfSheetSlot(sheet, i, slotsPerPage, padLeft, padRight, padTop, padBottom);
                            Icon icon = whenFilled.apply(player, pageIndex);
                            if (icon != null) sheet.addIcon(i, icon);
                             else {
                                ItemStack stack = fullSlots.getItem((page - 1) * slotsPerPage + pageIndex);
                                if (stack != null) {
                                    BasicIcon basicIcon = new BasicIcon(stack);
                                    sheet.addIcon(i, basicIcon);
                                }
                            }
                        } catch (Throwable e) {
                            BukkitOfUtils.getInstance().logSevere("Error while building filled icon", e);
                            Icon basicIcon = buildPageIcon(i);
                            sheet.addIcon(i, basicIcon);
                        }
                    } else {
                        try {
                            Icon icon = whenNotFilled.apply(player, i);
                            if (icon != null) sheet.addIcon(i, icon);
                             else {
                                Icon basicIcon = buildPageIcon(i);
                                sheet.addIcon(i, basicIcon);
                            }
                        } catch (Throwable e) {
                            BukkitOfUtils.getInstance().logSevere("Error while building not filled icon", e);
                            Icon basicIcon = buildPageIcon(i);
                            sheet.addIcon(i, basicIcon);
                        }
                    }
                } catch (Throwable e) {
                    BukkitOfUtils.getInstance().logSevere("Error while building icon", e);
                    Icon basicIcon = buildPageIcon(i);
                    sheet.addIcon(i, basicIcon);
                }
            }
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevere("Error while building sheet", e);
        }
        return sheet;
    }

    public void previousPage() {
        try {
            currentPage--;
            if (currentPage < 1) currentPage = 1;
            openPage(currentPage);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevere("Error while going to previous page", e);
        }
    }

    public void nextPage() {
        try {
            currentPage++;
            int maxPages = getMaxPages(fullSlots, slotsPerPage);
            if (currentPage > maxPages) currentPage = maxPages;
            openPage(currentPage);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevere("Error while going to next page", e);
        }
    }

    public static Icon buildPageIcon(int index) {
        try {
            if (index < 6 * 9 - 9) {
                return buildFillerIcon();
            } else {
                if (index == 6 * 9 - 9) {
                    return buildPreviousPageIcon();
                } else if (index == 6 * 9 - 1) {
                    return buildNextPageIcon();
                } else {
//                return buildFillerIcon();
                    return buildAirSlot();
                }
            }
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevere("Error while building page icon", e);
            return new BasicIcon(ItemUtils.make(Material.AIR, ""));
        }
    }

    public static Icon buildAirSlot() {
        return new BasicIcon(ItemUtils.make(Material.AIR, ""));
    }

    public static ItemStack getPreviousPageItemStack() {
        return ItemUtils.make(Material.ARROW, "&bPrevious Page");
    }

    public static ItemStack getNextPageItemStack() {
        return ItemUtils.make(Material.ARROW, "&bNext Page");
    }

    public static Icon buildPreviousPageIcon() {
        return new BasicIcon(getPreviousPageItemStack()).onClick(TaskMenu::previousPage);
    }

    public static Icon buildNextPageIcon() {
        return new BasicIcon(getNextPageItemStack()).onClick(TaskMenu::nextPage);
    }

    public static void previousPage(InventoryEvent event) {
        Inventory inventory = event.getInventory();
        Optional<ScreenInstance> optional = ScreenManager.getScreen(inventory);
        if (optional.isEmpty()) return;
        ScreenInstance screen = optional.get();
        screen.getViewers().forEach((uuid, viewer) -> {
            if (viewer instanceof Player) {
                Player player = (Player) viewer;
                if (screen instanceof TaskMenu) {
                    TaskMenu taskMenu = (TaskMenu) screen;
                    taskMenu.previousPage();
                }
            }
        });
    }

    public static void nextPage(InventoryEvent event) {
        Inventory inventory = event.getInventory();
        Optional<ScreenInstance> optional = ScreenManager.getScreen(inventory);
        if (optional.isEmpty()) return;
        ScreenInstance screen = optional.get();
        screen.getViewers().forEach((uuid, viewer) -> {
            if (viewer instanceof Player) {
                Player player = (Player) viewer;
                if (screen instanceof TaskMenu) {
                    TaskMenu taskMenu = (TaskMenu) screen;
                    taskMenu.nextPage();
                }
            }
        });
    }

    public static ItemStack getFiller() {
        Material material;
        try {
            material = Material.BLACK_STAINED_GLASS_PANE; // Default to modern material
        } catch (Throwable e) {
            try {
                // Attempt to use the modern material
                material = Material.valueOf("BLACK_STAINED_GLASS_PANE");
            } catch (Throwable t) {
                try {
                    // Fallback to a legacy alternative, if available
                    material = Material.valueOf("STAINED_GLASS_PANE");
                } catch (Throwable legacyEx) {
                    // Log an error if no valid material is found
                    BukkitOfUtils.getInstance().logSevere("Could not find material for filler icon", legacyEx);
                    material = Material.AIR; // Fallback to AIR to avoid crashes
                }
            }
        }
        return ItemUtils.make(material, "");
    }

    public static Icon buildFillerIcon() {
        try {
            return new BasicIcon(getFiller()).onClick(TaskMenu::fillerAction).onDrag(TaskMenu::fillerAction);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevere("Error while building filler icon", e);
            return new BasicIcon(ItemUtils.make(Material.AIR, ""));
        }
    }

    public static void fillerAction(InventoryEvent event) {
        event.getViewers().forEach(viewer -> {
            if (viewer instanceof Player) {
                Player player = (Player) viewer;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
            }
        });
    }

    public void openPage(int page) {
        InventorySheet sheet = buildSheet(player, fullSlots, page, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, whenFilled);
        setInventorySheet(sheet);
        open();
    }

    @Override
    public void redraw() {
        openPage(currentPage);
    }

    public static int getMaxPages(ManagedInventory fullSlots, int pageSize) {
        return (int) Math.ceil((double) fullSlots.size() / pageSize);
    }

    // pad <direction> is the number of slots to pad in that direction (there are 9 slots per row and 6 rows)
    // slotsPerPage is the number of slots per page (max is 9 * 6 = 54)
    // currentIndex is the index of the slot currently being filled (in the sheet)
    // a page is defined by a range of slots that is equal to or lesser than sheet.size().
    // a page is a square of slots defined by the pad values, but max of slotsPerPage and hard max of 6 * 9 (54)
    // this needs to return true if the slot is fillable, false if it is not
    public static boolean isFillable(InventorySheet sheet, int currentIndex, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom) {
        return getPageIndexOfSheetSlot(sheet, currentIndex, slotsPerPage, padLeft, padRight, padTop, padBottom) != -1;
    }

    // pad <direction> is the number of slots to pad in that direction (there are 9 slots per row and 6 rows)
    // slotsPerPage is the number of slots per page (max is 9 * 6 = 54)
    // currentIndex is the index of the slot currently being checked (in the sheet)
    // a page is defined by a range of slots that is equal to or lesser than sheet.size().
    // a page is a square of slots defined by the pad values, but max of slotsPerPage and hard max of 6 * 9 (54)
    // this needs to return the pageIndex found at the slot of currentIndex, or -1 if the slot is not on the page
    public static int getPageIndexOfSheetSlot(InventorySheet sheet, int currentIndex, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom) {
        int hardMaxRows = sheet.getRows();
        int hardMaxCols = 9; // Fixed number of columns per row in the grid
        // Calculate effective row and column ranges considering padding
        int rowStart = padTop;
        if (rowStart >= hardMaxRows) return -1;
        int rowEnd = hardMaxRows - padBottom;
        if (rowEnd <= rowStart) return -1;
        int colStart = padLeft;
        if (colStart >= hardMaxCols) return -1;
        int colEnd = hardMaxCols - padRight;
        if (colEnd <= colStart) return -1;
        // Validate currentIndex is within the total size of the sheet
        if (currentIndex < 0 || currentIndex >= sheet.getSize()) return -1;
        // Determine row and column position of currentIndex in the sheet
        int row = currentIndex / hardMaxCols; // Row index (0-based)
        int col = currentIndex % hardMaxCols; // Column index (0-based)
        // Check if the slot falls within the padded bounds
        if (row < rowStart || row >= rowEnd || col < colStart || col >= colEnd) return -1;
        // Calculate the dimensions of the effective page
        int maxRowSlots = rowEnd - rowStart; // Number of rows in the page
        int maxColSlots = colEnd - colStart; // Number of columns in the page
        // Compute the local position within the page
        int pageRow = row - rowStart;
        int pageCol = col - colStart;
        // Compute the page index based on the local position
        int pageIndex = pageRow * maxColSlots + pageCol;
        // Ensure the computed page index is within the valid range of slotsPerPage
        return pageIndex < slotsPerPage ? pageIndex : -1;
    }

    public ManagedInventory getFullSlots() {
        return this.fullSlots;
    }

    public int getSlotsPerPage() {
        return this.slotsPerPage;
    }

    public int getPadLeft() {
        return this.padLeft;
    }

    public int getPadRight() {
        return this.padRight;
    }

    public int getPadTop() {
        return this.padTop;
    }

    public int getPadBottom() {
        return this.padBottom;
    }

    public BiFunction<Player, Integer, Icon> getWhenNotFilled() {
        return this.whenNotFilled;
    }

    public BiFunction<Player, Integer, Icon> getWhenFilled() {
        return this.whenFilled;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setFullSlots(final ManagedInventory fullSlots) {
        this.fullSlots = fullSlots;
    }

    public void setSlotsPerPage(final int slotsPerPage) {
        this.slotsPerPage = slotsPerPage;
    }

    public void setPadLeft(final int padLeft) {
        this.padLeft = padLeft;
    }

    public void setPadRight(final int padRight) {
        this.padRight = padRight;
    }

    public void setPadTop(final int padTop) {
        this.padTop = padTop;
    }

    public void setPadBottom(final int padBottom) {
        this.padBottom = padBottom;
    }

    public void setWhenNotFilled(final BiFunction<Player, Integer, Icon> whenNotFilled) {
        this.whenNotFilled = whenNotFilled;
    }

    public void setWhenFilled(final BiFunction<Player, Integer, Icon> whenFilled) {
        this.whenFilled = whenFilled;
    }

    public void setCurrentPage(final int currentPage) {
        this.currentPage = currentPage;
    }
}
