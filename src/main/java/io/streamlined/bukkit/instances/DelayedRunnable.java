package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.folia.LocationalTask;

import java.util.concurrent.ConcurrentSkipListSet;

public abstract class DelayedRunnable extends BaseRunnable {
    public DelayedRunnable(int delay, boolean isAsyncable) {
        super(delay, 1, isAsyncable);
    }

    @Override
    public ConcurrentSkipListSet<LocationalTask<?>> execute() {
        ConcurrentSkipListSet<LocationalTask<?>> r = onlyOnce();
        cancel();

        return r;
    }

    public abstract ConcurrentSkipListSet<LocationalTask<?>> onlyOnce();
}
