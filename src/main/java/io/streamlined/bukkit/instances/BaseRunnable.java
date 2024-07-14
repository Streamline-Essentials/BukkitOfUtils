package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.scheduler.MainScheduler;
import io.streamlined.bukkit.scheduler.SchedulableTask;
import io.streamlined.bukkit.scheduler.ScheduleType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public abstract class BaseRunnable implements Runnable, Comparable<BaseRunnable> {
    public static final int STANDARD_TICK_DELAY = 50;

    private final Date startTime;
    private final int index;
    private int delay;
    private int period;
    private int warmup;
    private int counter;
    private int ticksLived;
    private boolean runSync;
    private Date lastExecution;

    private boolean standardTick;
    private long standardWaitingTicks;

    @Nullable
    private Location runAt;
    @Nullable
    private Entity entity;

    public BaseRunnable(int delay, int period, boolean runSync, boolean load, @Nullable Location runAt, @Nullable Entity entity) {
        startTime = new Date();
        this.index = MainScheduler.getNextRunnableIndex();

        this.delay = delay;
        this.period = period;
        this.warmup = delay;
        this.runSync = runSync;

        this.runAt = runAt;
        this.entity = entity;

        this.standardTick = true;
        this.standardWaitingTicks = 0;

        if (load) load();
    }

    public BaseRunnable(int delay, int period, boolean runSync, boolean load) {
        this(delay, period, runSync, load, null, null);
    }

    public BaseRunnable(int delay, int period, boolean runSync, @Nullable Location runAt, @Nullable Entity entity) {
        this(delay, period, runSync, true, runAt, entity);
    }

    public BaseRunnable(int delay, int period, boolean runSync, boolean load, Entity entity) {
        this(delay, period, runSync, load, null, entity);
    }

    public BaseRunnable(int delay, int period, boolean runSync, boolean load, @Nullable Location runAt) {
        this(delay, period, runSync, load, runAt, null);
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

    public ScheduleType getType() {
        return ScheduleType.getType(getEntity(), getRunAt());
    }

    public boolean isLocationBased() {
        return getType() == ScheduleType.LOCATION;
    }

    public boolean isSenderBased() {
        return getType() == ScheduleType.ENTITY;
    }

    public boolean isGlobal() {
        return getType() == ScheduleType.GLOBAL;
    }

    public void runAsync() {
        CompletableFuture.runAsync(this);
    }

    public boolean pollStandardWaiting() {
        if (! standardTick) return true;

        if (standardWaitingTicks <= 0) {
            standardWaitingTicks = findNewStandardWaitingTicks();

            return true;
        }

        standardWaitingTicks --;
        return false;
    }

    public int findNewStandardWaitingTicks() {
        return ( STANDARD_TICK_DELAY / MainScheduler.getMillisPerTick() ) - 1;
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
                forwardExecution();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            counter = 0;
        }

        counter ++;
    }

    public void forwardExecution() {
        this.lastExecution = new Date();

        wrapExecution().run();
    }

    public SchedulableTask wrapExecution() {
        return new SchedulableTask(0, TimeUnit.MILLISECONDS, ! isRunSync(), this::execute, getRunAt(), getEntity());
    }

    public abstract void execute();

    public void load() {
        MainScheduler.loadRunnable(this);
    }

    public void unload() {
        MainScheduler.unloadRunnable(this);
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
