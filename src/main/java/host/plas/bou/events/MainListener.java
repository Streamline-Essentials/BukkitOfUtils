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

public class MainListener extends BOUListener {
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

    @BaseProcessor
    public void onRedrawEvent(BlockRedrawEvent event) {
        event.getScreenBlock().onRedraw(event);
    }

    @EventHandler
    public void onPluginDisable(org.bukkit.event.server.PluginDisableEvent event) {
        Plugin plugin = event.getPlugin();
        if (plugin.getName().equalsIgnoreCase("PlaceholderAPI")) {
            PAPICompat.shutdown();
        }
    }
}
