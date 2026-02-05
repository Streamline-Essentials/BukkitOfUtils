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
import java.util.function.Predicate;

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

    @Getter @Setter
    private static ConcurrentSkipListSet<ScreenBlock> loadedBlocks = new ConcurrentSkipListSet<>();

    public static void addBlock(ScreenBlock block) {
        loadedBlocks.add(block);
    }

    public static void removeBlock(Predicate<ScreenBlock> predicate) {
        loadedBlocks.removeIf(predicate);
    }

    public static Optional<ScreenBlock> getScreenBlock(ScreenInstance instance) {
        AtomicReference<Optional<ScreenBlock>> block = new AtomicReference<>(Optional.empty());

        loadedBlocks.forEach(b -> {
            if (block.get().isPresent()) return;

            if (instance.getScreenBlock().isPresent() && instance.getScreenBlock().get().equals(b)) {
                block.set(Optional.of(b));
            }
        });

        return block.get();
    }

    public static Optional<ScreenBlock> getScreenBlockOf(Player player) {
        AtomicReference<Optional<ScreenBlock>> block = new AtomicReference<>(Optional.empty());

        getScreen(player).ifPresent(screen -> {
            block.set(getScreenBlock(screen));
        });

        return block.get();
    }

    public static boolean hasBlock(ScreenBlock block) {
        return loadedBlocks.contains(block);
    }
}
