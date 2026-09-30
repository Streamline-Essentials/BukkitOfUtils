package host.plas.bou.scheduling;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An abstract runnable that executes once after a specified delay and then cancels itself.
 */
public abstract class BaseDelayedRunnable extends BaseRunnable {
    private final AtomicBoolean fired = new AtomicBoolean(false);

    /**
     * Constructs a new BaseDelayedRunnable with the specified delay.
     *
     * @param delay the number of ticks to wait before executing
     */
    public BaseDelayedRunnable(long delay) {
        super(delay, 0);
    }

    @Override
    public void run() {
        // The timer can still fire between the first run starting and cancel() taking effect.
        if (! fired.compareAndSet(false, true)) return;

        try {
            runDelayed();
        } finally {
            this.cancel();
        }
    }

    /**
     * The task logic to execute after the delay has elapsed.
     * Subclasses must implement this method with their delayed logic.
     */
    public abstract void runDelayed();
}
