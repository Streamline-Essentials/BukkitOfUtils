package io.streamlined.bukkit.instances;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class BaseRunnable implements Runnable, Comparable<BaseRunnable> {
    @Getter
    private final Date startTime;
    @Getter
    private final int index;
    @Getter @Setter
    private int delay;
    @Getter @Setter
    private int period;
    @Getter @Setter
    private int warmup;
    @Getter @Setter
    private int counter;
    @Getter @Setter
    private int ticksLived;
    @Getter @Setter
    private boolean asyncable;

    @Getter @Setter
    private Function<BaseRunnable, Location> locationGetter;

    public BaseRunnable(int delay, int period, boolean isAsyncable, boolean load, Function<BaseRunnable, Location> locationGetter) {
        startTime = new Date();
        this.index = BaseManager.getNextRunnableIndex();

        this.delay = delay;
        this.period = period;
        this.warmup = delay;
        this.asyncable = isAsyncable;

        this.locationGetter = locationGetter;

        if (load) load();
    }

    public BaseRunnable(int delay, int period, boolean isAsyncable, boolean load) {
        this(delay, period, isAsyncable, load, getMainLocationGetter());
    }

    public static Function<BaseRunnable, Location> getMainLocationGetter() {
        return runnable -> BaseManager.getMainWorld().getSpawnLocation();
    }

    public BaseRunnable(int delay, int period, boolean isAsyncable) {
        this(delay, period, isAsyncable, true);
    }

    public BaseRunnable(int delay, int period, boolean isAsyncable, Function<BaseRunnable, Location> locationGetter) {
        this(delay, period, isAsyncable, true, locationGetter);
    }

    public BaseRunnable(int delay, int period) {
        this(delay, period, true, true);
    }

    public BaseRunnable(int delay, int period, Function<BaseRunnable, Location> locationGetter) {
        this(delay, period, true, true, locationGetter);
    }

    public BaseRunnable(int period) {
        this(0, period, true, true);
    }

    public BaseRunnable(int period, Function<BaseRunnable, Location> locationGetter) {
        this(0, period, true, true, locationGetter);
    }

    public BaseRunnable() {
        this(1);
    }

    public void runAsync() {
        if (! asyncable) return;

        CompletableFuture.runAsync(this);
    }

    public void runOnlySync() {
        if (asyncable) return;

        this.run();
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
            if (! isAsyncable()) executeSync();
            else execute();
            counter = 0;
        }

        counter ++;
    }

    public void executeSync() {
        try {
            Bukkit.getScheduler().runTask(BaseManager.getBaseInstance(), this::execute);
        } catch (Exception e) {
//            e.printStackTrace(); // Don't print.
            try {
                FoliaManager.runTaskSync(getLocation(), this::execute);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public Location getLocation() {
        return locationGetter.apply(this);
    }

    public abstract void execute();

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
