package io.streamlined.bukkit.folia;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.streamlined.bukkit.instances.BaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.concurrent.atomic.AtomicReference;

public class FoliaManager {
    public static <R> R runTaskSync(LocationTask<R> locationTask) {
        RegionScheduler regionScheduler = Bukkit.getRegionScheduler();

        AtomicReference<R> result = new AtomicReference<>();
        regionScheduler.execute(BaseManager.getBaseInstance(), locationTask.getLocation(), () -> {
            result.set(locationTask.getSupplier().get());
        });

        return result.get();
    }

    public static void runTaskSync(Location location, Runnable runnable) {
        runTaskSync(new LocationTask<>(() -> {
            runnable.run();
            return null;
        }, location, true));
    }

    public static boolean isPossiblyFolia() {
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
