package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.folia.FoliaChecker;
import io.streamlined.bukkit.folia.LocationalTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import tv.quaint.objects.SingleSet;

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

    public BaseRunnable(int delay, int period, boolean isAsyncable, boolean load) {
        startTime = new Date();
        this.index = BaseManager.getNextRunnableIndex();

        this.delay = delay;
        this.period = period;
        this.warmup = delay;
        this.runSync = isAsyncable;

        if (load) load();
    }

    public BaseRunnable(int delay, int period, boolean isAsyncable) {
        this(delay, period, isAsyncable, true);
    }

    public BaseRunnable(int delay, int period) {
        this(delay, period, true, true);
    }

    public BaseRunnable(int period) {
        this(0, period, true, true);
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
                executeNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
            counter = 0;
        }

        counter ++;
    }

    public void executeNow() {
        ConcurrentSkipListSet<LocationalTask<?>> set = execute();

        set.forEach(LocationalTask::execute);
    }

    public <T> LocationalTask<T> buildTask() {
        return new LocationalTask<>(this::execute, getMainLocationizer(), null, ! isRunSync());
    }

    public <T> Function<T, Location> getMainLocationizer() {
        return t -> BaseManager.getMainWorld().getSpawnLocation();
    }

    public abstract ConcurrentSkipListSet<LocationalTask<?>> execute();

    public abstract <T> SingleSet<Function<T, Location>, Runnable> executeWhenFolia();

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
