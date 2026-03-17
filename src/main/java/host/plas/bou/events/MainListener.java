package host.plas.bou.events;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.papi.PAPICompat;
import host.plas.bou.events.self.plugin.PluginDisableEvent;
import host.plas.bou.gui.screens.events.BlockRedrawEvent;
import host.plas.bou.utils.DatabaseUtils;
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
     * Handles the custom PluginDisableEvent by flushing database and PAPI resources
     * for the disabled plugin.
     *
     * @param event the plugin disable event
     */
    @BaseProcessor
    public void onPluginDisable(PluginDisableEvent event) {
        if (! PAPICompat.isEnabled()) return;

        try {
            BetterPlugin plugin = event.getPlugin();
            DatabaseUtils.flush(plugin);
            PAPICompat.flush(plugin);
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
     * Handles the Bukkit PluginDisableEvent to shut down PAPI compatibility
     * when PlaceholderAPI is disabled.
     *
     * @param event the Bukkit plugin disable event
     */
    @EventHandler
    public void onPluginDisable(org.bukkit.event.server.PluginDisableEvent event) {
        Plugin plugin = event.getPlugin();
        if (plugin.getName().equalsIgnoreCase("PlaceholderAPI")) {
            PAPICompat.shutdown();
        }
    }
}
