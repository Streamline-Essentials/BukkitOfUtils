package io.streamlined.bukkit.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Getter @Setter
public class SchedulableTask {
    private final long id;
    private long delay;
    private TimeUnit timeUnit;
    private boolean async;
    private Runnable task;
    @Nullable
    private Location runAt;
    @Nullable
    private Entity entity;
    private Date registeredAt;

    public SchedulableTask(long delay, TimeUnit timeUnit, boolean async, @Nullable Runnable task, @Nullable Location runAt, Entity entity) {
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.async = async;
        this.task = task;
        this.runAt = runAt;
        this.entity = entity;
        this.registeredAt = new Date();

        this.id = MainScheduler.getNextTaskId();
    }

    public SchedulableTask(long delay, TimeUnit timeUnit, boolean async, @Nullable Runnable task, @Nullable Location runAt) {
        this(delay, timeUnit, async, task, runAt, null);
    }

    public SchedulableTask(long delay, TimeUnit timeUnit, boolean async, @Nullable Runnable task, @Nullable Entity entity) {
        this(delay, timeUnit, async, task, null, entity);
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

    public void run() {
        MainScheduler.execute(this);
    }

//    public Runnable getRunnable() {
//        return () -> {
//            try {
//                getTask().call();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        };
//    }
}
