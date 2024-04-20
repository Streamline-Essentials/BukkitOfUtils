package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.folia.LocationTask;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

@Setter
@Getter
public class RDelayedRunnable extends DelayedRunnable {
    private ConcurrentSkipListSet<LocationTask<?>> tasks;

    public RDelayedRunnable(ConcurrentSkipListSet<LocationTask<?>> tasks, int delay, boolean isAsyncable) {
        super(delay, isAsyncable);
        this.tasks = tasks;
    }

    @Override
    public ConcurrentSkipListSet<LocationTask<?>> buildTasks() {
        return tasks;
    }

    @Override
    public void onlyOnce(LocationTask<?> task) {
        task.execute();
    }
}
