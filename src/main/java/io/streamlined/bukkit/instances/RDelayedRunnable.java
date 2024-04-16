package io.streamlined.bukkit.instances;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import tv.quaint.objects.SingleSet;

import java.util.function.Function;

@Setter
@Getter
public class RDelayedRunnable extends DelayedRunnable {
    private Runnable runnable;
    private Function<Void, SingleSet<Location, Runnable>> foliaGetter;

    public RDelayedRunnable(Runnable runnable, Function<Void, SingleSet<Location, Runnable>> foliaGetter, int delay, boolean isAsyncable) {
        super(delay, isAsyncable);
        this.runnable = runnable;
        this.foliaGetter = foliaGetter;
    }

    @Override
    public void onlyOnce() {
        runnable.run();
    }

    @Override
    public SingleSet<Location, Runnable> executeWhenFolia() {
        return foliaGetter.apply(null);
    }
}
