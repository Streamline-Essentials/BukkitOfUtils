package host.plas.bou.scheduling;

/**
 * An abstract runnable that executes once after a specified delay and then cancels itself.
 */
public abstract class BaseDelayedRunnable extends BaseRunnable {
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
        runDelayed();

        this.cancel();
    }

    /**
     * The task logic to execute after the delay has elapsed.
     * Subclasses must implement this method with their delayed logic.
     */
    public abstract void runDelayed();
}
