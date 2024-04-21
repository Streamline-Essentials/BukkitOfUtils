package io.streamlined.bukkit.instances;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RDelayedRunnable extends DelayedRunnable {
    private Runnable runnable;

    public RDelayedRunnable(Runnable runnable, int delay, boolean isAsyncable) {
        super(delay, isAsyncable);

        this.runnable = runnable;
    }

    @Override
    public void onlyOnce() {
        runnable.run();
    }
}
