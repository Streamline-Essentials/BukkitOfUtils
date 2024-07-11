package io.streamlined.bukkit.scheduler;

import io.streamlined.bukkit.instances.BaseManager;
import io.streamlined.bukkit.instances.BaseRunnable;
import io.streamlined.bukkit.instances.RBaseRunnable;
import io.streamlined.bukkit.instances.RDelayedRunnable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import javax.swing.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class MainScheduler {
    @Getter @Setter
    private static long lastTaskId = 0;

    public static long getNextTaskId() {
        return ++ lastTaskId;
    }

    public static boolean isFoliaServer() {
        // Check server properties or configuration for indications of Folia
        String serverName = Bukkit.getServer().getName();
        String serverVersion = Bukkit.getServer().getVersion();

        // Example checks, these should be replaced with actual indicators
        if (serverName.contains("Folia") || serverVersion.contains("Folia")) {
            return true;
        }

        // Further checks specific to Folia
        // Check for specific files, environment variables, or unique APIs

        return false;
    }

    public static void runAsync(SchedulableTask task) {
        CompletableFuture.runAsync(task.getTask());
    }

    public static void executeSyncNonFolia(SchedulableTask task) {
        Bukkit.getScheduler().runTask(BaseManager.getBaseInstance(), task.getTask());
    }

    public static void executeSyncFolia(SchedulableTask task) {
        FoliaBridge.sync(task);
    }

    public static void executeAsyncNonFolia(SchedulableTask task) {
        runAsync(task);
    }

    public static void executeAsyncFolia(SchedulableTask task) {
        runAsync(task);
    }

    public static void executeSync(SchedulableTask task) {
        if (isFoliaServer()) {
            executeSyncFolia(task);
        } else {
            executeSyncNonFolia(task);
        }
    }

    public static void executeAsync(SchedulableTask task) {
        if (isFoliaServer()) {
            executeAsyncFolia(task);
        } else {
            executeAsyncNonFolia(task);
        }
    }

    public static void execute(SchedulableTask task) {
        if (task.isAsync()) {
            executeAsync(task);
        } else {
            executeSync(task);
        }
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<BaseRunnable> loadedRunnables = new ConcurrentSkipListSet<>();
    @Setter
    private static int nextRunnableIndex = 0;
    @Getter @Setter
    private static Timer ticker;

    public static void init() {
        ticker = new Timer(1, e -> tick());
        ticker.start();
    }

    public static void tick() {
        getLoadedRunnables().forEach(runnable -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public static void tickAsync() {
        getLoadedRunnables().forEach(runnable -> {
            try {
                runnable.runAsync();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public static void stop() {
        ticker.stop();
    }

    public static <T extends BaseRunnable> void loadRunnable(T runnable) {
        loadedRunnables.add(runnable);
    }

    public static <T extends BaseRunnable> void unloadRunnable(T runnable) {
        try {
            loadedRunnables.remove(runnable);
        } catch (Exception e) {
            T r = getRunnable(runnable.getIndex());
            if (r == null) r = getRunnable(runnable.getStartTime());

            if (r != null) loadedRunnables.remove(r);
        }
    }

    public static <T extends BaseRunnable> T getRunnable(Date startDate) {
        AtomicReference<T> runnable = new AtomicReference<>();

        loadedRunnables.forEach(r -> {
            if (r.getStartTime().equals(startDate)) {
                runnable.set((T) r);
            }
        });

        return runnable.get();
    }

    public static <T extends BaseRunnable> T getRunnable(int index) {
        AtomicReference<T> runnable = new AtomicReference<>();

        loadedRunnables.forEach(r -> {
            if (r.getIndex() == index) {
                runnable.set((T) r);
            }
        });

        return runnable.get();
    }

    public static int getNextRunnableIndex() {
        int index = nextRunnableIndex;

        nextRunnableIndex ++;

        return index;
    }

    public void schedule(Runnable runnable, int delay, int period, boolean isAsyncable) {
        new RBaseRunnable(runnable, delay, period, isAsyncable);
    }

    public void schedule(Runnable runnable, int delay, int period) {
        new RBaseRunnable(runnable, delay, period, true);
    }

    public void schedule(Runnable runnable, int delay, boolean isAsyncable) {
        new RDelayedRunnable(runnable, delay, isAsyncable);
    }

    public void schedule(Runnable runnable, int delay) {
        new RDelayedRunnable(runnable, delay, true);
    }
}
