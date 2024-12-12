package host.plas.bou.gui.menus;

import host.plas.bou.gui.type.BouGuiTypes;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.obj.ManagedInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentSkipListMap;

public class TaskMenu extends PaginatedMenu {
    public TaskMenu(@NotNull Player player) {
        super(player, BouGuiTypes.TASK_MENU, buildTaskList(),
                4 * 9, 0, 0, 0, 2);
    }

    public static ManagedInventory buildTaskList() {
        ConcurrentSkipListMap<Integer, ItemStack> taskItems = TaskManager.getTaskItems();
        ManagedInventory inventory = new ManagedInventory(taskItems.size());

        for (Integer slot : taskItems.keySet()) {
            inventory.setItem(slot, taskItems.get(slot));
        }

        return inventory;
    }

    public static void open(@NotNull Player player) {
        TaskMenu menu = new TaskMenu(player);
        menu.open();
    }
}
