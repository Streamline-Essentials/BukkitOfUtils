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

/**
 * Central manager for GUI screens and screen blocks. Tracks active screen instances
 * per player and manages loaded screen blocks.
 */
public class ScreenManager {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ScreenManager() {
        // Utility class
    }

    /**
     * The listener responsible for GUI maintenance events such as inventory close handling.
     *
     * @param guiMaintenanceListener the GUI maintenance listener to set
     * @return the GUI maintenance listener
     */
    @Getter @Setter
    private static GuiMaintenanceListener guiMaintenanceListener;

    /**
     * Initializes the screen manager by creating the GUI maintenance listener.
     */
    public static void init() {
        guiMaintenanceListener = new GuiMaintenanceListener();
    }

    /**
     * The set of all active screen instances, keyed by player.
     *
     * @param screens the set of screen instances to set
     * @return the set of active screen instances
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<ScreenInstance> screens = new ConcurrentSkipListSet<>();

    /**
     * Retrieves the active screen instance for the given player.
     *
     * @param player the player to look up
     * @return an {@link Optional} containing the screen instance, or empty if none exists
     */
    public static Optional<ScreenInstance> getScreen(Player player) {
        AtomicReference<Optional<ScreenInstance>> screen = new AtomicReference<>(Optional.empty());

        screens.forEach(s -> {
            if (s.getIdentifier().equals(player.getUniqueId().toString())) {
                screen.set(Optional.of(s));
            }
        });

        return screen.get();
    }

    /**
     * Retrieves the screen instance associated with the given inventory.
     *
     * @param inventory the inventory to look up
     * @return an {@link Optional} containing the screen instance, or empty if none matches
     */
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

    /**
     * Sets the screen instance for a player, removing any existing screen first.
     *
     * @param player the player to associate the screen with
     * @param screen the screen instance to set
     */
    public static void setScreen(Player player, ScreenInstance screen) {
        if (hasScreen(player)) {
            removeScreen(player);
        }

        screens.add(screen);
    }

    /**
     * Removes the screen instance associated with the given player.
     *
     * @param player the player whose screen should be removed
     */
    public static void removeScreen(Player player) {
        screens.removeIf(s -> s.getIdentifier().equals(player.getUniqueId().toString()));
    }

    /**
     * Checks whether the given player has an active screen instance.
     *
     * @param player the player to check
     * @return {@code true} if the player has an active screen, {@code false} otherwise
     */
    public static boolean hasScreen(Player player) {
        return getScreen(player).isPresent();
    }

    /**
     * Returns all screen instances that are viewing the specified screen block.
     *
     * @param block the screen block to find viewers for
     * @return a set of screen instances currently viewing the block
     */
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

    /**
     * The set of all loaded screen blocks that can open GUI screens.
     *
     * @param loadedBlocks the set of screen blocks to set
     * @return the set of loaded screen blocks
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<ScreenBlock> loadedBlocks = new ConcurrentSkipListSet<>();

    /**
     * Adds a screen block to the set of loaded blocks.
     *
     * @param block the screen block to add
     */
    public static void addBlock(ScreenBlock block) {
        loadedBlocks.add(block);
    }

    /**
     * Removes screen blocks matching the given predicate from the loaded blocks set.
     *
     * @param predicate the condition for removal
     */
    public static void removeBlock(Predicate<ScreenBlock> predicate) {
        loadedBlocks.removeIf(predicate);
    }

    /**
     * Retrieves the loaded screen block associated with the given screen instance.
     *
     * @param instance the screen instance to look up
     * @return an {@link Optional} containing the matching screen block, or empty if not found
     */
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

    /**
     * Retrieves the screen block that the given player is currently viewing.
     *
     * @param player the player to look up
     * @return an {@link Optional} containing the screen block, or empty if the player has no active screen block
     */
    public static Optional<ScreenBlock> getScreenBlockOf(Player player) {
        AtomicReference<Optional<ScreenBlock>> block = new AtomicReference<>(Optional.empty());

        getScreen(player).ifPresent(screen -> {
            block.set(getScreenBlock(screen));
        });

        return block.get();
    }

    /**
     * Checks whether the given screen block is in the loaded blocks set.
     *
     * @param block the screen block to check
     * @return {@code true} if the block is loaded, {@code false} otherwise
     */
    public static boolean hasBlock(ScreenBlock block) {
        return loadedBlocks.contains(block);
    }
}
