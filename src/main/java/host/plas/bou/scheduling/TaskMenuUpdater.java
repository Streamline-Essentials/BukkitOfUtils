package host.plas.bou.scheduling;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.menus.TaskMenu;

public class TaskMenuUpdater extends BaseRunnable {
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
