package host.plas.bou.events;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.papi.PAPICompat;
import host.plas.bou.events.self.plugin.PluginDisableEvent;
import host.plas.bou.gui.screens.events.BlockRedrawEvent;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.DatabaseUtils;
import host.plas.bou.utils.PluginUtils;
import gg.drak.thebase.events.processing.BaseProcessor;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

/**
 * The main event listener for BukkitOfUtils.
 * Handles plugin disable events, GUI redraw events, and PlaceholderAPI shutdown.
 */
public class MainListener extends BOUListener {
    /**
     * Constructs a new MainListener instance.
     */
    public MainListener() {
        super();
    }

    /**
     * Handles the custom PluginDisableEvent by cancelling the plugin's tracked tasks and
     * flushing its database and PAPI resources.
     *
     * @param event the plugin disable event
     */
    @BaseProcessor
    public void onPluginDisable(PluginDisableEvent event) {
        try {
            BetterPlugin plugin = event.getPlugin();
            TaskManager.cancelOwnedBy(plugin);
            DatabaseUtils.flush(plugin);
            if (PAPICompat.isEnabled()) {
                PAPICompat.flush(plugin);
            }
            if (!(plugin instanceof BukkitOfUtils)) {
                PluginUtils.unregisterPlugin(plugin);
            }
        } catch (Throwable t) {
            BukkitOfUtils.getInstance().logWarning("Failed to fully disable a Better Plugin!", t);
        }
    }

    /**
     * Handles block redraw events by delegating to the screen block's redraw handler.
     *
     * @param event the block redraw event
     */
    @BaseProcessor
    public void onRedrawEvent(BlockRedrawEvent event) {
        event.getScreenBlock().onRedraw(event);
    }

    /**
     * Handles the Bukkit PluginDisableEvent: cancels tasks BukkitOfUtils tracks for plugins that
     * do not extend {@link BetterPlugin} (those are handled after their own disable logic by the
     * BukkitOfUtils disable event), and shuts down PAPI compatibility when PlaceholderAPI is disabled.
     *
     * @param event the Bukkit plugin disable event
     */
    @EventHandler
    public void onPluginDisable(org.bukkit.event.server.PluginDisableEvent event) {
        Plugin plugin = event.getPlugin();
        if (! (plugin instanceof BetterPlugin)) {
            try {
                TaskManager.cancelOwnedBy(plugin);
            } catch (Throwable t) {
                BukkitOfUtils.getInstance().logWarning("Failed to cancel tasks of " + plugin.getName() + ".", t);
            }
        }
        if (plugin.getName().equalsIgnoreCase("PlaceholderAPI")) {
            PAPICompat.shutdown();
        }
    }
}
