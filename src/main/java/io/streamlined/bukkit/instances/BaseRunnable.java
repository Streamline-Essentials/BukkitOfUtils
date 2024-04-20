package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.folia.FoliaChecker;
import io.streamlined.bukkit.folia.LocationTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;

@Getter
public abstract class BaseRunnable implements Runnable, Comparable<BaseRunnable> {
    private final Date startTime;
    private final int index;
    @Setter
    private int delay;
    @Setter
    private int period;
    @Setter
    private int warmup;
    @Setter
    private int counter;
    @Setter
    private int ticksLived;
    @Setter
    private boolean runSync;

    public BaseRunnable(int delay, int period, boolean runSync, boolean load) {
        startTime = new Date();
        this.index = BaseManager.getNextRunnableIndex();

        this.delay = delay;
        this.period = period;
        this.warmup = delay;
        this.runSync = runSync;

        if (load) load();
    }

    public BaseRunnable(int delay, int period, boolean runSync) {
        this(delay, period, runSync, true);
    }

    public BaseRunnable(int delay, int period) {
        this(delay, period, false, true);
    }

    public BaseRunnable(int period) {
        this(0, period, false, true);
    }

    public BaseRunnable() {
        this(1);
    }

    public void runAsync() {
        CompletableFuture.runAsync(this);
    }

    @Override
    public void run() {
        this.ticksLived ++;

        if (delay == -1 || period == -1) return;

        if (warmup > 0) {
            warmup --;
            return;
        }

        if (counter >= period) {
            try {
                buildAndExecute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            counter = 0;
        }

        counter ++;
    }

    public void buildAndExecute() {
        try {
            ConcurrentSkipListSet<LocationTask<?>> tasks = new ConcurrentSkipListSet<>();

            if (isRunSync()) {
                if (FoliaChecker.isPossiblyFolia()) {
                    FoliaChecker.runTaskSync(getLocation(), () -> {
                        tasks.addAll(buildTasks());
                    });
                } else {
                    Bukkit.getScheduler().runTask(BaseManager.getBaseInstance(), () -> {
                        tasks.addAll(buildTasks());
                    });
                }
            } else {
                tasks.addAll(buildTasks());
            }

            tasks.forEach(this::execute);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract ConcurrentSkipListSet<LocationTask<?>> buildTasks();

    public abstract void execute(LocationTask<?> task);

    public Location getLocation() {
        return BaseManager.getMainWorld().getSpawnLocation();
    }

    public void load() {
        BaseManager.loadRunnable(this);
    }

    public void unload() {
        BaseManager.unloadRunnable(this);
    }

    public void cancel() {
        this.delay = -1;
        this.period = -1;

        unload();
    }

    @Override
    public int compareTo(@NotNull BaseRunnable o) {
        return Integer.compare(index, o.getIndex());
    }
}
