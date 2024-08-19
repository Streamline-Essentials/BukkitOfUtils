package host.plas.bou.gui;

import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ScreenManager {
    @Getter @Setter
    private static GuiMaintenanceListener guiMaintenanceListener;

    public static void init() {
        guiMaintenanceListener = new GuiMaintenanceListener();
    }

    @Getter @Setter
    private static ConcurrentHashMap<Player, ScreenInstance> screens = new ConcurrentHashMap<>();

    public static Optional<ScreenInstance> getScreen(Player player) {
        AtomicReference<Optional<ScreenInstance>> screen = new AtomicReference<>(Optional.empty());

        screens.forEach((p, s) -> {
            if (p.getUniqueId().equals(player.getUniqueId())) {
                screen.set(Optional.of(s));
            }
        });

        return screen.get();
    }

    public static void setScreen(Player player, ScreenInstance screen) {
        if (hasScreen(player)) {
            removeScreen(player);
        }

        screens.put(player, screen);
    }

    public static void removeScreen(Player player) {
        screens.forEach((p, sheet) -> {
            if (p.getUniqueId().equals(player.getUniqueId())) {
                screens.remove(p);
            }
        });
    }

    public static boolean hasScreen(Player player) {
        return getScreen(player).isPresent();
    }

    public static ConcurrentHashMap<Player, ScreenInstance> getPlayersOf(ScreenBlock block) {
        ConcurrentHashMap<Player, ScreenInstance> players = new ConcurrentHashMap<>();

        getScreens().forEach((player, screenInstance) -> {
            screenInstance.getScreenBlock().ifPresent(screenBlock -> {
                if (screenBlock.equals(block)) {
                    players.put(player, screenInstance);
                }
            });
        });

        return players;
    }
}
