package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.folia.LocationalTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import tv.quaint.objects.SingleSet;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;

@Setter
@Getter
public class RDelayedRunnable extends DelayedRunnable {
    private Runnable runnable;

    public RDelayedRunnable(Runnable runnable, int delay, boolean isAsyncable) {
        super(delay, isAsyncable);
        this.runnable = runnable;
    }

    @Override
    public ConcurrentSkipListSet<LocationalTask<?>> onlyOnce() {
        return new ConcurrentSkipListSet<>(List.of(new LocationalTask<>(runnable, getMainLocationizer(), null, isRunSync())));
    }
}
