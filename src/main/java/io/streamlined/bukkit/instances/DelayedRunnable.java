package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.folia.LocationTask;

public abstract class DelayedRunnable extends BaseRunnable {
    public DelayedRunnable(int delay, boolean isAsyncable) {
        super(delay, 1, isAsyncable);
    }

    @Override
    public void execute(LocationTask<?> task) {
        onlyOnce(task);
        cancel();
    }

    public abstract void onlyOnce(LocationTask<?> task);
}
