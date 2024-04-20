package io.streamlined.bukkit.folia;

import io.streamlined.bukkit.instances.BaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.concurrent.atomic.AtomicReference;

public class FoliaChecker {
    public static boolean isFolia() {
        try {
            return FoliaManager.isFolia();
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
    }

    public static <R> R execute(LocationTask<R> locationTask, boolean runSync) {
        try {
            if (runSync) {
                return runTaskSync(locationTask);
            } else {
                return locationTask.getSupplier().get();
            }
        } catch (UnsupportedOperationException e) {
            return FoliaManager.runTaskSync(locationTask);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void validate(LocationTask<Void> locationTask, boolean runSync) {
        try {
            execute(locationTask, runSync);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <R> R runTaskSync(LocationTask<R> locationTask) {
        if (isFolia()) {
            try {
                AtomicReference<R> result = new AtomicReference<>();
                Bukkit.getScheduler().runTask(BaseManager.getBaseInstance(), () -> {
                    result.set(locationTask.getSupplier().get());
                });
                return result.get();
            } catch (UnsupportedOperationException e) {
                return FoliaManager.runTaskSync(locationTask);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            try {
                AtomicReference<R> result = new AtomicReference<>();
                Bukkit.getScheduler().runTask(BaseManager.getBaseInstance(), () -> {
                    result.set(locationTask.getSupplier().get());
                });
                return result.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void runTaskSync(Location location, Runnable runnable) {
        runTaskSync(new LocationTask<>(() -> {
            runnable.run();
            return null;
        }, location, true));
    }
}
