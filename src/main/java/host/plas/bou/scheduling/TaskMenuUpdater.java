package host.plas.bou.scheduling;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.menus.TaskMenu;
import host.plas.bou.gui.screens.ScreenInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * A periodic runnable that redraws all open TaskMenu screens at a fixed interval.
 */
public class TaskMenuUpdater extends BaseRunnable {
    /**
     * Constructs a new TaskMenuUpdater with a period of 20 ticks.
     */
    public TaskMenuUpdater() {
        super(20);
    }

    @Override
    public void run() {
        // BaseRunnable executes asynchronously — hop to the main thread for Bukkit inventory work.
        if (!TaskManager.isThreadSync()) {
            TaskManager.runTask(this::updateOpenMenus);
            return;
        }
        updateOpenMenus();
    }

    private void updateOpenMenus() {
        List<ScreenInstance> snapshot = new ArrayList<>(ScreenManager.getScreens());
        for (ScreenInstance screen : snapshot) {
            try {
                if (screen instanceof TaskMenu) {
                    ((TaskMenu) screen).redraw();
                }
            } catch (Throwable t) {
                BukkitOfUtils.getInstance().logWarning("Failed to update task menu: " + screen, t);
            }
        }
    }
}
