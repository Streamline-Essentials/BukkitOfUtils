package host.plas.bou.gui.menus;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.gui.GuiType;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.icons.BasicIcon;
import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.obj.ManagedInventory;
import lombok.Getter;
import lombok.Setter;
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

/**
 * A paginated GUI menu that displays items from a managed inventory across multiple pages.
 * Supports configurable padding, custom fill/empty slot handlers, and page navigation.
 */
@Getter @Setter
public class PaginatedMenu extends ScreenInstance {
    /**
     * The managed inventory containing all items to be paginated across pages.
     *
     * @param fullSlots the managed inventory to set
     * @return the managed inventory containing all items
     */
    private ManagedInventory fullSlots;

    /**
     * The number of fillable item slots per page.
     *
     * @param slotsPerPage the number of slots per page to set
     * @return the number of fillable slots per page
     */
    private int slotsPerPage;

    /**
     * The number of columns to pad on the left side of each page.
     *
     * @param padLeft the left padding to set
     * @return the left padding in columns
     */
    private int padLeft;

    /**
     * The number of columns to pad on the right side of each page.
     *
     * @param padRight the right padding to set
     * @return the right padding in columns
     */
    private int padRight;

    /**
     * The number of rows to pad on the top of each page.
     *
     * @param padTop the top padding to set
     * @return the top padding in rows
     */
    private int padTop;

    /**
     * The number of rows to pad on the bottom of each page.
     *
     * @param padBottom the bottom padding to set
     * @return the bottom padding in rows
     */
    private int padBottom;

    /**
     * Function providing icons for non-fillable slots (player, sheet index to icon).
     *
     * @param whenNotFilled the function to set for non-fillable slot icons
     * @return the function that provides icons for non-fillable slots
     */
    private BiFunction<Player, Integer, Icon> whenNotFilled;

    /**
     * Function providing icons for fillable slots (player, page index to icon).
     *
     * @param whenFilled the function to set for fillable slot icons
     * @return the function that provides icons for fillable slots
     */
    private BiFunction<Player, Integer, Icon> whenFilled;

    /**
     * The current page number (1-based).
     *
     * @param currentPage the current page number to set
     * @return the current page number
     */
    private int currentPage;

    /**
     * Constructs a new PaginatedMenu with all configuration parameters.
     *
     * @param player        the player viewing the menu
     * @param type          the GUI type
     * @param fullSlots     the managed inventory containing all items to paginate
     * @param currentPage   the initial page number (1-based)
     * @param slotsPerPage  the number of fillable slots per page
     * @param padLeft       the number of columns to pad on the left
     * @param padRight      the number of columns to pad on the right
     * @param padTop        the number of rows to pad on the top
     * @param padBottom     the number of rows to pad on the bottom
     * @param whenNotFilled function providing icons for non-fillable slots
     * @param whenFilled    function providing icons for fillable slots (overrides item display)
     */
    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int currentPage,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom,
                         BiFunction<Player, Integer, Icon> whenNotFilled, BiFunction<Player, Integer, Icon> whenFilled) {
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

    /**
     * Constructs a new PaginatedMenu starting at page 1.
     *
     * @param player        the player viewing the menu
     * @param type          the GUI type
     * @param fullSlots     the managed inventory containing all items to paginate
     * @param slotsPerPage  the number of fillable slots per page
     * @param padLeft       the number of columns to pad on the left
     * @param padRight      the number of columns to pad on the right
     * @param padTop        the number of rows to pad on the top
     * @param padBottom     the number of rows to pad on the bottom
     * @param whenNotFilled function providing icons for non-fillable slots
     * @param whenFilled    function providing icons for fillable slots
     */
    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom,
                         BiFunction<Player, Integer, Icon> whenNotFilled, BiFunction<Player, Integer, Icon> whenFilled) {
        this(player, type, fullSlots, 1, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, whenFilled);
    }

    /**
     * Constructs a new PaginatedMenu with a custom not-filled handler and no filled handler.
     *
     * @param player        the player viewing the menu
     * @param type          the GUI type
     * @param fullSlots     the managed inventory containing all items to paginate
     * @param currentPage   the initial page number (1-based)
     * @param slotsPerPage  the number of fillable slots per page
     * @param padLeft       the number of columns to pad on the left
     * @param padRight      the number of columns to pad on the right
     * @param padTop        the number of rows to pad on the top
     * @param padBottom     the number of rows to pad on the bottom
     * @param whenNotFilled function providing icons for non-fillable slots
     */
    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int currentPage,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom,
                         BiFunction<Player, Integer, Icon> whenNotFilled) {
        this(player, type, fullSlots, currentPage, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, (p, i) -> null);
    }

    /**
     * Constructs a new PaginatedMenu starting at page 1 with a custom not-filled handler.
     *
     * @param player        the player viewing the menu
     * @param type          the GUI type
     * @param fullSlots     the managed inventory containing all items to paginate
     * @param slotsPerPage  the number of fillable slots per page
     * @param padLeft       the number of columns to pad on the left
     * @param padRight      the number of columns to pad on the right
     * @param padTop        the number of rows to pad on the top
     * @param padBottom     the number of rows to pad on the bottom
     * @param whenNotFilled function providing icons for non-fillable slots
     */
    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom,
                         BiFunction<Player, Integer, Icon> whenNotFilled) {
        this(player, type, fullSlots, 1, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, (p, i) -> null);
    }

    /**
     * Constructs a new PaginatedMenu with no custom slot handlers.
     *
     * @param player       the player viewing the menu
     * @param type         the GUI type
     * @param fullSlots    the managed inventory containing all items to paginate
     * @param currentPage  the initial page number (1-based)
     * @param slotsPerPage the number of fillable slots per page
     * @param padLeft      the number of columns to pad on the left
     * @param padRight     the number of columns to pad on the right
     * @param padTop       the number of rows to pad on the top
     * @param padBottom    the number of rows to pad on the bottom
     */
    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int currentPage,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom) {
        this(player, type, fullSlots, currentPage, slotsPerPage, padLeft, padRight, padTop, padBottom, (p, i) -> null, (p, i) -> null);
    }

    /**
     * Constructs a new PaginatedMenu starting at page 1 with no custom slot handlers.
     *
     * @param player       the player viewing the menu
     * @param type         the GUI type
     * @param fullSlots    the managed inventory containing all items to paginate
     * @param slotsPerPage the number of fillable slots per page
     * @param padLeft      the number of columns to pad on the left
     * @param padRight     the number of columns to pad on the right
     * @param padTop       the number of rows to pad on the top
     * @param padBottom    the number of rows to pad on the bottom
     */
    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom) {
        this(player, type, fullSlots, 1, slotsPerPage, padLeft, padRight, padTop, padBottom, (p, i) -> null, (p, i) -> null);
    }

    /**
     * Builds an inventory sheet for a specific page of the paginated menu.
     *
     * @param player        the player viewing the menu
     * @param fullSlots     the managed inventory containing all items
     * @param page          the page number to build (1-based)
     * @param slotsPerPage  the number of fillable slots per page
     * @param padLeft       the number of columns to pad on the left
     * @param padRight      the number of columns to pad on the right
     * @param padTop        the number of rows to pad on the top
     * @param padBottom     the number of rows to pad on the bottom
     * @param whenNotFilled function providing icons for non-fillable slots
     * @param whenFilled    function providing icons for fillable slots
     * @return the constructed {@link InventorySheet} for the requested page
     */
    public static InventorySheet buildSheet(@NotNull Player player, ManagedInventory fullSlots, int page, int slotsPerPage,
                                            int padLeft, int padRight, int padTop, int padBottom,
                                            BiFunction<Player, Integer, Icon> whenNotFilled, BiFunction<Player, Integer, Icon> whenFilled) {
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

    /**
     * Navigates to the previous page, clamping at page 1.
     */
    public void previousPage() {
        try {
            currentPage--;
            if (currentPage < 1) currentPage = 1;
            openPage(currentPage);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevere("Error while going to previous page", e);
        }
    }

    /**
     * Navigates to the next page, clamping at the maximum page number.
     */
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

    /**
     * Builds the appropriate page icon for the given slot index.
     * Returns navigation icons for the bottom row corners, filler for other non-bottom slots,
     * and air for the remaining bottom slots.
     *
     * @param index the slot index
     * @return the appropriate {@link Icon} for the slot
     */
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

    /**
     * Builds an empty air slot icon.
     *
     * @return an air {@link Icon}
     */
    public static Icon buildAirSlot() {
        return new BasicIcon(ItemUtils.make(Material.AIR, ""));
    }

    /**
     * Creates the item stack for the previous page navigation arrow.
     *
     * @return an arrow {@link ItemStack} labeled "Previous Page"
     */
    public static ItemStack getPreviousPageItemStack() {
        return ItemUtils.make(Material.ARROW, "&bPrevious Page");
    }

    /**
     * Creates the item stack for the next page navigation arrow.
     *
     * @return an arrow {@link ItemStack} labeled "Next Page"
     */
    public static ItemStack getNextPageItemStack() {
        return ItemUtils.make(Material.ARROW, "&bNext Page");
    }

    /**
     * Builds the previous page navigation icon with a click handler.
     *
     * @return the previous page {@link Icon}
     */
    public static Icon buildPreviousPageIcon() {
        return new BasicIcon(getPreviousPageItemStack()).onClick(TaskMenu::previousPage);
    }

    /**
     * Builds the next page navigation icon with a click handler.
     *
     * @return the next page {@link Icon}
     */
    public static Icon buildNextPageIcon() {
        return new BasicIcon(getNextPageItemStack()).onClick(TaskMenu::nextPage);
    }

    /**
     * Handles the previous page action triggered by an inventory event.
     * Navigates all viewers of the inventory to the previous page if applicable.
     *
     * @param event the inventory event that triggered the action
     */
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

    /**
     * Handles the next page action triggered by an inventory event.
     * Navigates all viewers of the inventory to the next page if applicable.
     *
     * @param event the inventory event that triggered the action
     */
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

    /**
     * Creates a filler item stack using black stained glass pane, with fallback to legacy materials.
     *
     * @return the filler {@link ItemStack}
     */
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

    /**
     * Builds a filler icon with click and drag handlers that play a bass note sound.
     *
     * @return the filler {@link Icon}
     */
    public static Icon buildFillerIcon() {
        try {
            return new BasicIcon(getFiller()).onClick(TaskMenu::fillerAction).onDrag(TaskMenu::fillerAction);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevere("Error while building filler icon", e);
            return new BasicIcon(ItemUtils.make(Material.AIR, ""));
        }
    }

    /**
     * Plays a bass note block sound to all viewers of the inventory event.
     *
     * @param event the inventory event that triggered the filler action
     */
    public static void fillerAction(InventoryEvent event) {
        event.getViewers().forEach((viewer) -> {
            if (viewer instanceof Player) {
                Player player = (Player) viewer;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            }
        });
    }

    /**
     * Opens a specific page of the paginated menu by rebuilding the inventory sheet.
     *
     * @param page the page number to open (1-based)
     */
    public void openPage(int page) {
        InventorySheet sheet = buildSheet(player, fullSlots, page, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, whenFilled);
        setInventorySheet(sheet);
        open();
    }

    @Override
    public void redraw() {
        openPage(currentPage);
    }

    /**
     * Calculates the maximum number of pages needed to display all items.
     *
     * @param fullSlots the managed inventory containing all items
     * @param pageSize  the number of items per page
     * @return the total number of pages
     */
    public static int getMaxPages(ManagedInventory fullSlots, int pageSize) {
        return (int) Math.ceil((double) fullSlots.size() / pageSize);
    }

    // pad <direction> is the number of slots to pad in that direction (there are 9 slots per row and 6 rows)
    // slotsPerPage is the number of slots per page (max is 9 * 6 = 54)
    // currentIndex is the index of the slot currently being filled (in the sheet)
    // a page is defined by a range of slots that is equal to or lesser than sheet.size().
    // a page is a square of slots defined by the pad values, but max of slotsPerPage and hard max of 6 * 9 (54)
    // this needs to return true if the slot is fillable, false if it is not
    /**
     * Determines whether a slot at the given index is fillable based on padding constraints.
     *
     * @param sheet        the inventory sheet
     * @param currentIndex the slot index to check
     * @param slotsPerPage the number of fillable slots per page
     * @param padLeft      the number of columns to pad on the left
     * @param padRight     the number of columns to pad on the right
     * @param padTop       the number of rows to pad on the top
     * @param padBottom    the number of rows to pad on the bottom
     * @return {@code true} if the slot is fillable, {@code false} otherwise
     */
    public static boolean isFillable(InventorySheet sheet, int currentIndex, int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom) {
        return getPageIndexOfSheetSlot(sheet, currentIndex, slotsPerPage, padLeft, padRight, padTop, padBottom) != -1;
    }

    // pad <direction> is the number of slots to pad in that direction (there are 9 slots per row and 6 rows)
    // slotsPerPage is the number of slots per page (max is 9 * 6 = 54)
    // currentIndex is the index of the slot currently being checked (in the sheet)
    // a page is defined by a range of slots that is equal to or lesser than sheet.size().
    // a page is a square of slots defined by the pad values, but max of slotsPerPage and hard max of 6 * 9 (54)
    // this needs to return the pageIndex found at the slot of currentIndex, or -1 if the slot is not on the page
    /**
     * Computes the page-relative index for a given sheet slot index, considering padding.
     * Returns -1 if the slot is outside the fillable area.
     *
     * @param sheet        the inventory sheet
     * @param currentIndex the slot index to check
     * @param slotsPerPage the number of fillable slots per page
     * @param padLeft      the number of columns to pad on the left
     * @param padRight     the number of columns to pad on the right
     * @param padTop       the number of rows to pad on the top
     * @param padBottom    the number of rows to pad on the bottom
     * @return the page-relative index, or -1 if the slot is not fillable
     */
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
}
