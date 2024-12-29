package host.plas.bou.gui;

import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class ScreenManager {
    @Getter @Setter
    private static GuiMaintenanceListener guiMaintenanceListener;

    public static void init() {
        guiMaintenanceListener = new GuiMaintenanceListener();
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<ScreenInstance> screens = new ConcurrentSkipListSet<>();

    public static Optional<ScreenInstance> getScreen(Player player) {
        AtomicReference<Optional<ScreenInstance>> screen = new AtomicReference<>(Optional.empty());

        screens.forEach(s -> {
            if (s.getIdentifier().equals(player.getUniqueId().toString())) {
                screen.set(Optional.of(s));
            }
        });

        return screen.get();
    }

    public static Optional<ScreenInstance> getScreen(Inventory inventory) {
        AtomicReference<Optional<ScreenInstance>> screen = new AtomicReference<>(Optional.empty());

        screens.forEach(s -> {
            if (screen.get().isPresent()) return;

            if (s.getInventory().equals(inventory)) {
                screen.set(Optional.of(s));
            }
        });

        return screen.get();
    }

    public static void setScreen(Player player, ScreenInstance screen) {
        if (hasScreen(player)) {
            removeScreen(player);
        }

        screens.add(screen);
    }

    public static void removeScreen(Player player) {
        screens.removeIf(s -> s.getIdentifier().equals(player.getUniqueId().toString()));
    }

    public static boolean hasScreen(Player player) {
        return getScreen(player).isPresent();
    }

    public static ConcurrentSkipListSet<ScreenInstance> getPlayersOf(ScreenBlock block) {
        ConcurrentSkipListSet<ScreenInstance> players = new ConcurrentSkipListSet<>();

        getScreens().forEach(screenInstance -> {
            screenInstance.getScreenBlock().ifPresent(screenBlock -> {
                if (screenBlock.equals(block)) {
                    players.add(screenInstance);
                }
            });
        });

        return players;
    }
}
