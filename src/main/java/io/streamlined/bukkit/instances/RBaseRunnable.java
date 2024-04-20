package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.folia.LocationTask;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

@Setter
@Getter
public class RBaseRunnable extends BaseRunnable {
    private ConcurrentSkipListSet<LocationTask<?>> tasks;

    public RBaseRunnable(ConcurrentSkipListSet<LocationTask<?>> tasks, int delay, int period, boolean runSync) {
        super(delay, period, runSync);
        this.tasks = tasks;
    }

    @Override
    public ConcurrentSkipListSet<LocationTask<?>> buildTasks() {
        return tasks;
    }

    @Override
    public void execute(LocationTask<?> task) {
        task.execute();
    }
}
