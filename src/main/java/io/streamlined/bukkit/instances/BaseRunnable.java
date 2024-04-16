package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.folia.FoliaChecker;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import tv.quaint.objects.SingleSet;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
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
    private boolean asyncable;

    @Setter
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
                executeSwitch();
            } catch (Exception e) {
                e.printStackTrace();
            }
            counter = 0;
        }

        counter ++;
    }

    public void executeSwitch() {
        if (isAsyncable()) executeWithChecks();
        else executeSyncWithChecks();
    }

    public void executeSyncWithChecks() {
        FoliaChecker.validate(() -> {
            SingleSet<Location, Runnable> set = executeWhenFolia();

            FoliaManager.runTaskSync(set.getKey(), set.getValue());

            return null;
        }, () -> {
            Bukkit.getScheduler().runTask(BaseManager.getBaseInstance(), this::execute);

            return null;
        });
    }

    public Location getLocation() {
        return locationGetter.apply(this);
    }

    public abstract void execute();

    public abstract SingleSet<Location, Runnable> executeWhenFolia();

    public void executeWithChecks() {
        FoliaChecker.validate(() -> {
            SingleSet<Location, Runnable> set = executeWhenFolia();

            FoliaManager.runTaskSync(set.getKey(), set.getValue());

            return null;
        }, () -> {
            execute();

            return null;
        });
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
