package io.streamlined.bukkit.instances;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class FoliaManager {
    public static void runTaskSync(Location location, Runnable runnable) {
        RegionScheduler regionScheduler = Bukkit.getRegionScheduler();

        regionScheduler.execute(BaseManager.getBaseInstance(), location, runnable);
    }

    public static boolean isFolia() {
        try {
            RegionScheduler regionScheduler = Bukkit.getRegionScheduler();

            PluginMeta pluginMeta = BaseManager.getBaseInstance().getPluginMeta();
            boolean bool = pluginMeta.isFoliaSupported();

            return true;
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
    }
}
