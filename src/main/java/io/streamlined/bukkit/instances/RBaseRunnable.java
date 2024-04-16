package io.streamlined.bukkit.instances;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import tv.quaint.objects.SingleSet;

import java.util.function.Function;

@Setter
@Getter
public class RBaseRunnable extends BaseRunnable {
    private Runnable runnable;
    private Function<Void, SingleSet<Location, Runnable>> foliaGetter;

    public RBaseRunnable(Runnable runnable, Function<Void, SingleSet<Location, Runnable>> foliaGetter, int delay, int period, boolean isAsyncable) {
        super(delay, period, isAsyncable);
        this.runnable = runnable;
        this.foliaGetter = foliaGetter;
    }

    @Override
    public void execute() {
        runnable.run();
    }

    @Override
    public SingleSet<Location, Runnable> executeWhenFolia() {
        return foliaGetter.apply(null);
    }
}
