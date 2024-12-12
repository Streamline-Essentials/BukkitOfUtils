package host.plas.bou.gui.menus;

import host.plas.bou.gui.GuiType;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.icons.BasicIcon;
import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.math.CosmicMath;
import host.plas.bou.utils.obj.ManagedInventory;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

@Getter @Setter
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

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom,
                         BiFunction<Player, Integer, Icon> whenNotFilled, BiFunction<Player, Integer, Icon> whenFilled) {
        this(player, type, fullSlots, 1, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, whenFilled);
    }

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int currentPage,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom,
                         BiFunction<Player, Integer, Icon> whenNotFilled) {
        this(player, type, fullSlots, currentPage, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, (p, i) -> null);
    }

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom,
                         BiFunction<Player, Integer, Icon> whenNotFilled) {
        this(player, type, fullSlots, 1, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, (p, i) -> null);
    }

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots, int currentPage,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom) {
        this(player, type, fullSlots, currentPage, slotsPerPage, padLeft, padRight, padTop, padBottom, (p, i) -> null, (p, i) -> null);
    }

    public PaginatedMenu(@NotNull Player player, GuiType type, ManagedInventory fullSlots,
                         int slotsPerPage, int padLeft, int padRight, int padTop, int padBottom) {
        this(player, type, fullSlots, 1, slotsPerPage, padLeft, padRight, padTop, padBottom, (p, i) -> null, (p, i) -> null);
    }

    public static InventorySheet buildSheet(@NotNull Player player, ManagedInventory fullSlots, int page, int slotsPerPage,
                                            int padLeft, int padRight, int padTop, int padBottom,
                                            BiFunction<Player, Integer, Icon> whenNotFilled, BiFunction<Player, Integer, Icon> whenFilled) {
        InventorySheet sheet = new InventorySheet(6 * 9); // 6 rows of 9 slots
        int maxPages = getMaxPages(fullSlots, slotsPerPage);
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        for (int i = 0; i < sheet.getSize(); i ++) {
            if (isFillable(sheet, i, slotsPerPage, padLeft, padRight, padTop, padBottom)) {
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
            } else {
                Icon icon = whenNotFilled.apply(player, i);
                if (icon != null) sheet.addIcon(i, icon);
                else {
                    Icon basicIcon = buildPageIcon(i);
                    sheet.addIcon(i, basicIcon);
                }
            }
        }

        return sheet;
    }

    public void previousPage() {
        currentPage --;
        if (currentPage < 1) currentPage = 1;
        openPage(currentPage);
    }

    public void nextPage() {
        currentPage ++;
        int maxPages = getMaxPages(fullSlots, slotsPerPage);
        if (currentPage > maxPages) currentPage = maxPages;
        openPage(currentPage);
    }

    public static Icon buildPageIcon(int index) {
        if (index < 6 * 9 - 9) {
            return buildFillerIcon();
        } else {
            if (index == 6 * 9 - 9) {
                return buildPreviousPageIcon();
            } else if (index == 6 * 9 - 5) {
                return buildNextPageIcon();
            } else {
//                return buildFillerIcon();
                return buildAirSlot();
            }
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
        event.getViewers().forEach((viewer) -> {
            if (viewer instanceof Player) {
                Player player = (Player) viewer;
                ScreenManager.getScreen(player).ifPresent((screen) -> {
                    if (screen instanceof TaskMenu) {
                        TaskMenu taskMenu = (TaskMenu) screen;
                        taskMenu.previousPage();
                    }
                });
            }
        });
    }

    public static void nextPage(InventoryEvent event) {
        event.getViewers().forEach((viewer) -> {
            if (viewer instanceof Player) {
                Player player = (Player) viewer;
                ScreenManager.getScreen(player).ifPresent((screen) -> {
                    if (screen instanceof TaskMenu) {
                        TaskMenu taskMenu = (TaskMenu) screen;
                        taskMenu.nextPage();
                    }
                });
            }
        });
    }

    public static ItemStack getFiller() {
        return ItemUtils.make(Material.BLACK_STAINED_GLASS_PANE, "");
    }

    public static Icon buildFillerIcon() {
        return new BasicIcon(getFiller()).onClick(TaskMenu::fillerAction).onDrag(TaskMenu::fillerAction);
    }

    public static void fillerAction(InventoryEvent event) {
        event.getViewers().forEach((viewer) -> {
            if (viewer instanceof Player) {
                Player player = (Player) viewer;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            }
        });
    }

    public void openPage(int page) {
        InventorySheet sheet = buildSheet(player, fullSlots, page, slotsPerPage, padLeft, padRight, padTop, padBottom, whenNotFilled, whenFilled);
        setInventorySheet(sheet);
        open();
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
        int hardMaxCols = 9;

        int rowStart = padTop;
        if (rowStart > hardMaxRows) rowStart = hardMaxRows;
        int rowEnd = hardMaxRows - padBottom;
        if (rowEnd < rowStart) rowEnd = rowStart;
        int colStart = padLeft;
        if (colStart > hardMaxCols) colStart = hardMaxCols;
        int colEnd = hardMaxCols - padRight;
        if (colEnd < colStart) colEnd = colStart;

        if (currentIndex < 0 || currentIndex >= sheet.getSize()) return -1;

        if (currentIndex < rowStart * hardMaxCols + colStart || currentIndex >= rowEnd * hardMaxCols + colEnd) return -1;

        int row = CosmicMath.ceilDiv(currentIndex, hardMaxCols); // the row that the current index is on in the sheet
        int rowCol = currentIndex % hardMaxCols; // the column that the current index is on in the sheet

        if (row < rowStart || row >= rowEnd || rowCol < colStart || rowCol >= colEnd) return -1;

        int maxRowSlots = colEnd - colStart;
        int maxColSlots = rowEnd - rowStart;

        int pageRow = row - rowStart;
        int pageCol = rowCol - colStart;

        return pageRow * maxRowSlots + pageCol;
    }
}
