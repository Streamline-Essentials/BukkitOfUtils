package io.streamlined.bukkit.instances;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RBaseRunnable extends BaseRunnable {
    private Runnable runnable;

    public RBaseRunnable(Runnable runnable, int delay, int period, boolean runSync) {
        super(delay, period, runSync);

        this.runnable = runnable;
    }

    @Override
    public void execute() {
        runnable.run();
    }
}
