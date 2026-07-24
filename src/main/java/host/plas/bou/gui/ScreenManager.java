package host.plas.bou.gui;

import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;

/**
 * Central manager for GUI screens and screen blocks. Tracks active screen instances
 * per player and manages loaded screen blocks.
 */
public final class ScreenManager {
    private ScreenManager() {
    }

    /**
     * The listener responsible for GUI maintenance events such as inventory close handling.
     *
     * @param guiMaintenanceListener the GUI maintenance listener to set
     * @return the GUI maintenance listener
     */
    @Getter
    @Setter
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
    @Getter
    @Setter
    private static ConcurrentSkipListSet<ScreenInstance> screens = new ConcurrentSkipListSet<>();

    /**
     * Retrieves the active screen instance for the given player.
     *
     * @param player the player to look up
     * @return an {@link Optional} containing the screen instance, or empty if none exists
     */
    public static Optional<ScreenInstance> getScreen(Player player) {
        if (player == null) return Optional.empty();
        String id = player.getUniqueId().toString();
        for (ScreenInstance screen : screens) {
            if (id.equals(screen.getIdentifier())) {
                return Optional.of(screen);
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves the screen instance associated with the given inventory.
     *
     * @param inventory the inventory to look up
     * @return an {@link Optional} containing the screen instance, or empty if none matches
     */
    public static Optional<ScreenInstance> getScreen(Inventory inventory) {
        if (inventory == null) return Optional.empty();
        for (ScreenInstance screen : screens) {
            if (inventory.equals(screen.getInventory())) {
                return Optional.of(screen);
            }
        }
        return Optional.empty();
    }

    /**
     * Sets the screen instance for a player, removing any existing screen first.
     *
     * @param player the player to associate the screen with
     * @param screen the screen instance to set
     */
    public static void setScreen(Player player, ScreenInstance screen) {
        if (player == null || screen == null) return;
        removeScreen(player);
        screens.add(screen);
    }

    /**
     * Removes the screen instance associated with the given player.
     *
     * @param player the player whose screen should be removed
     */
    public static void removeScreen(Player player) {
        if (player == null) return;
        String id = player.getUniqueId().toString();
        screens.removeIf(s -> id.equals(s.getIdentifier()));
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
        if (block == null) return players;

        for (ScreenInstance screenInstance : screens) {
            screenInstance.getScreenBlock().ifPresent(screenBlock -> {
                if (screenBlock.equals(block) || screenBlock.getIdentifier().equals(block.getIdentifier())) {
                    players.add(screenInstance);
                }
            });
        }
        return players;
    }

    /**
     * The set of all loaded screen blocks that can open GUI screens.
     *
     * @param loadedBlocks the set of screen blocks to set
     * @return the set of loaded screen blocks
     */
    @Getter
    @Setter
    private static ConcurrentSkipListSet<ScreenBlock> loadedBlocks = new ConcurrentSkipListSet<>();

    /**
     * Adds a screen block to the set of loaded blocks.
     *
     * @param block the screen block to add
     */
    public static void addBlock(ScreenBlock block) {
        if (block == null) return;
        loadedBlocks.add(block);
    }

    /**
     * Removes screen blocks matching the given predicate from the loaded blocks set.
     *
     * @param predicate the condition for removal
     */
    public static void removeBlock(Predicate<ScreenBlock> predicate) {
        if (predicate == null) return;
        loadedBlocks.removeIf(predicate);
    }

    /**
     * Retrieves the loaded screen block associated with the given screen instance.
     *
     * @param instance the screen instance to look up
     * @return an {@link Optional} containing the matching screen block, or empty if not found
     */
    public static Optional<ScreenBlock> getScreenBlock(ScreenInstance instance) {
        if (instance == null) return Optional.empty();
        Optional<ScreenBlock> attached = instance.getScreenBlock();
        if (attached.isPresent()) {
            ScreenBlock block = attached.get();
            if (loadedBlocks.contains(block)) {
                return Optional.of(block);
            }
            for (ScreenBlock loaded : loadedBlocks) {
                if (loaded.getIdentifier().equals(block.getIdentifier())) {
                    return Optional.of(loaded);
                }
            }
            return attached;
        }
        return Optional.empty();
    }

    /**
     * Retrieves the screen block that the given player is currently viewing.
     *
     * @param player the player to look up
     * @return an {@link Optional} containing the screen block, or empty if the player has no active screen block
     */
    public static Optional<ScreenBlock> getScreenBlockOf(Player player) {
        return getScreen(player).flatMap(ScreenManager::getScreenBlock);
    }

    /**
     * Checks whether the given screen block is in the loaded blocks set.
     *
     * @param block the screen block to check
     * @return {@code true} if the block is loaded, {@code false} otherwise
     */
    public static boolean hasBlock(ScreenBlock block) {
        return block != null && loadedBlocks.contains(block);
    }
}
