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

public class TaskMenu extends PaginatedMenu {
    public TaskMenu(@NotNull Player player) {
        super(player, BouGuiTypes.TASK_MENU, buildTaskList(),
                4 * 9, 0, 0, 0, 2);
    }

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
