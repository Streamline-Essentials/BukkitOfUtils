package io.streamlined.bukkit.instances;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

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
                checkAndExecute(isRunSync());
            } catch (Throwable e) {
                e.printStackTrace();
            }
            counter = 0;
        }

        counter ++;
    }

    public void checkAndExecute(boolean runSync) {
        checkAndExecute(runSync, 0);
    }

    public void checkAndExecute(boolean runSync, int tries) {
        if (runSync) {
            try {
                Bukkit.getScheduler().runTask(BaseManager.getBaseInstance(), this::execute);
            } catch (Throwable e) {
                e.printStackTrace();
                if (tries >= 1) return;
                checkAndExecute(false, tries + 1);
            }
        } else {
            try {
                execute();
            } catch (Throwable e) {
                e.printStackTrace();
                if (tries >= 1) return;
                checkAndExecute(true, tries + 1);
            }
        }
    }

    public abstract void execute();

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
