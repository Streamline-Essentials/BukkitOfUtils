package host.plas.bou.gui;

import host.plas.bou.BetterPlugin;
import host.plas.bou.gui.screens.ScreenInstance;
import org.bukkit.entity.Player;

/**
 * Builds a plugin-specific GUI screen opened from {@code /boup menu}.
 * Implementations may return a {@link host.plas.bou.gui.menus.PaginatedMenu}
 * or any other {@link ScreenInstance}.
 */
@FunctionalInterface
public interface BetterPluginMenuBuilder {
    /**
     * Builds the screen for the given player and plugin.
     *
     * @param player the player who will view the menu
     * @param plugin the BetterPlugin providing the menu
     * @return a ready-to-open screen instance
     */
    ScreenInstance build(Player player, BetterPlugin plugin);
}
