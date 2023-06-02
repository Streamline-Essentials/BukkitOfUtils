package io.streamlined.bukkit.instances;

import lombok.Getter;
import lombok.Setter;

public class RDelayedRunnable extends DelayedRunnable {
    @Getter @Setter
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
