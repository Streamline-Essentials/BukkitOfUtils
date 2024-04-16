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
public class RBaseRunnable extends BaseRunnable {
    private Runnable runnable;

    public RBaseRunnable(Runnable runnable, int delay, int period, boolean runSync) {
        super(delay, period, runSync);
        this.runnable = runnable;
    }

    @Override
    public ConcurrentSkipListSet<LocationalTask<?>> execute() {
        return new ConcurrentSkipListSet<>(List.of(new LocationalTask<>(runnable, getMainLocationizer(), null, isRunSync())));
    }
}
