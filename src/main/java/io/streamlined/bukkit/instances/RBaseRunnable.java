package io.streamlined.bukkit.instances;

import lombok.Getter;
import lombok.Setter;

public class RBaseRunnable extends BaseRunnable {
    @Getter @Setter
    private Runnable runnable;

    public RBaseRunnable(Runnable runnable, int delay, int period, boolean isAsyncable) {
        super(delay, period, isAsyncable);
        this.runnable = runnable;
    }

    @Override
    public void execute() {
        runnable.run();
    }
}
