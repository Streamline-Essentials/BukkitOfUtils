package host.plas.bou.gui.menus;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.type.BouGuiTypes;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.obj.ManagedInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A paginated menu that displays all active synchronous and asynchronous tasks
 * as items in a GUI inventory.
 */
public class TaskMenu extends PaginatedMenu {
    /**
     * Constructs a new TaskMenu for the given player, displaying all active tasks.
     *
     * @param player the player to show the task menu to
     */
    public TaskMenu(@NotNull Player player) {
        super(player, BouGuiTypes.TASK_MENU, buildTaskList(),
                4 * 9, 0, 0, 0, 2);
    }

    /**
     * Builds a managed inventory containing item representations of all active
     * synchronous and asynchronous tasks.
     *
     * @return a {@link ManagedInventory} populated with task item stacks
     */
    public static ManagedInventory buildTaskList() {
        ConcurrentSkipListMap<Integer, ItemStack> taskItems = TaskManager.getTaskItems();
        ConcurrentSkipListMap<Integer, ItemStack> asyncItems = TaskManager.getAsyncItems();
        int total = Math.max(1, taskItems.size() + asyncItems.size());
        ManagedInventory inventory = new ManagedInventory(total);

        try {
            AtomicInteger slot = new AtomicInteger(0);
            taskItems.forEach((key, value) -> inventory.setItem(slot.getAndIncrement(), value));
            asyncItems.forEach((key, value) -> inventory.setItem(slot.getAndIncrement(), value));
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Error while building task list: " + e.getMessage(), e);
        }

        return inventory;
    }

    /**
     * Refreshes the backing task list from {@link TaskManager} and clamps the current page.
     */
    public void refreshTasks() {
        setFullSlots(buildTaskList());
        int maxPages = Math.max(1, getMaxPages(getFullSlots(), getSlotsPerPage()));
        if (getCurrentPage() > maxPages) {
            setCurrentPage(maxPages);
        }
        if (getCurrentPage() < 1) {
            setCurrentPage(1);
        }
    }

    /**
     * Rebuilds the task list and refreshes icons in-place when possible.
     * Falls back to reopening the page if the inventory is not open.
     */
    @Override
    public void redraw() {
        if (!TaskManager.isThreadSync()) {
            TaskManager.runTask(getPlayer(), this::redraw);
            return;
        }

        try {
            refreshTasks();
            InventorySheet sheet = buildSheet(
                    getPlayer(),
                    getFullSlots(),
                    getCurrentPage(),
                    getSlotsPerPage(),
                    getPadLeft(),
                    getPadRight(),
                    getPadTop(),
                    getPadBottom(),
                    getWhenNotFilled(),
                    getWhenFilled()
            );
            setInventorySheet(sheet);

            if (getInventory() != null && getPlayer().getOpenInventory().getTopInventory().equals(getInventory())) {
                build(sheet);
            } else {
                openPage(getCurrentPage());
            }
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Error while redrawing task menu: " + e.getMessage(), e);
        }
    }

    /**
     * Opens a task menu for the given player. If not called from the main server thread,
     * the operation is rescheduled to run synchronously.
     *
     * @param player the player to open the task menu for, or {@code null} to do nothing
     */
    public static void open(Player player) {
        if (player == null) return;

        if (!TaskManager.isThreadSync()) {
            TaskManager.runTask(player, () -> open(player));
            return;
        }

        try {
            TaskMenu menu = new TaskMenu(player);
            try {
                menu.open();
            } catch (Exception e) {
                BukkitOfUtils.getInstance().logWarning("Error while opening task menu: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Error while creating task menu: " + e.getMessage(), e);
        }
    }
}
