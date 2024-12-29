package host.plas.bou.scheduling;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.menus.TaskMenu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentSkipListMap;

public class TaskMenuUpdater extends BaseRunnable {
    public TaskMenuUpdater() {
        super(0, 20);
    }

    @Override
    public void run() {
        ConcurrentSkipListMap<String, Player> toRedraw = new ConcurrentSkipListMap<>();

        ScreenManager.getScreens().forEach(screen -> {
            try {
                if (screen instanceof TaskMenu) {
                    ConcurrentSkipListMap<String, HumanEntity> viewers = null;
                    try {
                        viewers = new ConcurrentSkipListMap<>(screen.getViewers());
                    } catch (Throwable t) {
                        BukkitOfUtils.getInstance().logWarning("Failed to get viewers for task menu: " + screen, t);
                        return;
                    }

                    viewers.forEach((uuid, viewer) -> {
                        if (viewer == null) return;

                        try {
                            if (viewer instanceof Player) {
                                Player player = (Player) viewer;
                                toRedraw.put(uuid, player);
                            }
                        } catch (Throwable t) {
                            BukkitOfUtils.getInstance().logWarning("Failed to update task menu for viewer [1]: " + viewer, t);
                        }
                    });
                }
            } catch (Throwable t) {
                BukkitOfUtils.getInstance().logWarning("Failed to update task menu: " + screen, t);
            }
        });

        toRedraw.forEach((uuid, player) -> {
            if (player == null) return;

            try {
                TaskMenu.open(player);
            } catch (Throwable t) {
                BukkitOfUtils.getInstance().logWarning("Failed to update task menu for player [2]: " + player.getName(), t);
            }
        });
    }
}
