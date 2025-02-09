package host.plas.bou.events;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.papi.PAPICompat;
import host.plas.bou.events.self.plugin.PluginDisableEvent;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.screens.events.BlockRedrawEvent;
import host.plas.bou.utils.DatabaseUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import tv.quaint.events.BaseEventHandler;
import tv.quaint.events.BaseEventListener;
import tv.quaint.events.processing.BaseProcessor;

import java.util.Objects;

public class MainListener implements ListenerConglomerate {
    public MainListener() {
        BukkitOfUtils.getInstance().registerListenerConglomerate(this);

        BukkitOfUtils.getInstance().logInfo("&cBukkitOfUtils &dMain Listener &fregistered.");
    }

    @BaseProcessor
    public void onPluginDisable(PluginDisableEvent event) {
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
}
