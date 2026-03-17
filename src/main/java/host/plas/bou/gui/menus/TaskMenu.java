package host.plas.bou.gui.menus;

import host.plas.bou.BukkitOfUtils;
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
        ManagedInventory inventory = new ManagedInventory(taskItems.size());

        try {
            AtomicInteger slot = new AtomicInteger(0);
            taskItems.forEach((key, value) -> {
                inventory.setItem(slot.getAndIncrement(), value);
            });

            asyncItems.forEach((key, value) -> {
                inventory.setItem(slot.getAndIncrement(), value);
            });
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Error while building task list: " + e.getMessage(), e);
        }

        return inventory;
    }

    /**
     * Opens a task menu for the given player. If not called from the main server thread,
     * the operation is rescheduled to run synchronously.
     *
     * @param player the player to open the task menu for, or {@code null} to do nothing
     */
    public static void open(Player player) {
        if (player == null) return;

        if (! TaskManager.isThreadSync()) {
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
