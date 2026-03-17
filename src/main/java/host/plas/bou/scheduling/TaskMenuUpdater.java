package host.plas.bou.scheduling;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.menus.TaskMenu;

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
        ScreenManager.getScreens().forEach(screen -> {
            try {
                if (screen instanceof TaskMenu) {
                    screen.redraw();
                }
            } catch (Throwable t) {
                BukkitOfUtils.getInstance().logWarning("Failed to update task menu: " + screen, t);
            }
        });
    }
}
