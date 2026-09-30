package host.plas.bou.scheduling;

import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.MessageUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.async.AsyncUtils;
import org.bukkit.plugin.Plugin;

import javax.swing.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An abstract base class for repeating or delayed runnables managed by the TaskManager.
 * Provides tick-based scheduling using a Swing Timer and async execution.
 */
@Setter @Getter
public abstract class BaseRunnable implements Runnable {
    /**
     * The date and time when this runnable was created.
     * @param startedAt the start date to set
     * @return the start date
     */
    private Date startedAt;
    /**
     * The current tick count used to track progress toward the next execution.
     * @param currentTickCount the current tick count to set
     * @return the current tick count
     */
    private long currentTickCount;
    /**
     * The number of ticks between each execution of this runnable.
     * @param period the period to set
     * @return the period in ticks
     */
    private long period;
    /**
     * The unique index identifying this runnable in the TaskManager.
     * @param index the index to set
     * @return the unique index
     */
    private int index;
    /**
     * Whether this runnable is currently paused.
     * @param paused the paused state to set
     * @return true if this runnable is paused
     */
    private boolean paused;

    /**
     * The total number of ticks this runnable has been alive.
     * @param ticksLived the ticks lived count to set
     * @return the total ticks lived
     */
    private long ticksLived;

    /**
     * The Swing Timer that drives the tick cycle for this runnable.
     * @param timer the timer to set
     * @return the Swing Timer
     */
    private Timer timer;

    /**
     * The plugin whose class defines this runnable; it is cancelled when that plugin is disabled.
     * @return the owning plugin
     */
    @Setter(AccessLevel.NONE)
    private Plugin owningPlugin;

    /**
     * Set while an execution is in progress. The timer never starts a new execution while the
     * previous one is still running, so a run that takes longer than the period delays the next
     * one instead of overlapping it.
     */
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private final AtomicBoolean executing = new AtomicBoolean(false);

    /**
     * Constructs a new BaseRunnable with the specified delay and period.
     * The runnable is automatically registered and started via TaskManager.
     *
     * @param delay  the number of ticks to wait before the first execution
     * @param period the number of ticks between subsequent executions
     */
    public BaseRunnable(long delay, long period) {
        this.startedAt = new Date();
        this.currentTickCount = delay * -1;
        this.period = period;
        this.index = TaskManager.getNextIndex();
        this.paused = false;
        this.ticksLived = 0;
        this.owningPlugin = TaskManager.findOwner(getClass());

        this.timer = createTimer();

        TaskManager.start(this);
    }

    /**
     * Constructs a new BaseRunnable with no delay and the specified period.
     *
     * @param period the number of ticks between subsequent executions
     */
    public BaseRunnable(long period) {
        this(0, period);
    }

    /**
     * Registers this runnable with the TaskManager.
     */
    public void load() {
        TaskManager.load(this);
    }

    /**
     * Unregisters this runnable from the TaskManager.
     */
    public void unload() {
        TaskManager.unload(this);
    }

    /**
     * Starts the internal timer if it is not already running.
     */
    public void start() {
        if (timer.isRunning()) return;

        timer.start();
    }

    /**
     * Stops the internal timer if it is currently running.
     */
    public void stop() {
        if (! timer.isRunning()) return;

        timer.stop();
    }

    /**
     * Restarts the internal timer.
     */
    public void restart() {
        timer.restart();
    }

    /**
     * Creates a new Swing Timer configured with the base ticking frequency
     * that invokes {@link #tick()} on each tick.
     *
     * @return the newly created Timer
     */
    public Timer createTimer() {
        return new Timer(BaseManager.getBaseConfig().getTickingFrequency(), e -> {
            try {
                tick();
            } catch (Throwable t) {
                MessageUtils.logDebug("Error while ticking runnable: " + this, t);
            }
        });
    }

    /**
     * Performs a single tick cycle. If the runnable is not paused, the tick count has reached
     * the period and no previous execution is still running, the runnable is executed
     * asynchronously. When the previous execution is still running, the tick count keeps
     * growing and the runnable executes on the first tick after it finishes.
     *
     * @return a CompletableFuture that completes when the async execution finishes,
     *         or a completed future if the runnable was not executed this tick
     */
    public CompletableFuture<Void> tick() {
        if (this.paused) return CompletableFuture.completedFuture(null);

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        if (this.currentTickCount >= this.period && executing.compareAndSet(false, true)) {
            this.currentTickCount = 0;
            try {
                future = AsyncUtils.executeAsync(this);
                future.whenComplete((ignored, error) -> executing.set(false));
            } catch (Throwable e) {
                executing.set(false);
                MessageUtils.logDebug("Error while ticking runnable: " + this, e);
            }
        }

        countTicks();

        return future;
    }

    /**
     * Increments both the current tick count and the total ticks lived counters.
     */
    public void countTicks() {
        this.currentTickCount ++;
        this.ticksLived ++;
    }

    /**
     * Cancels this runnable and removes it from the TaskManager.
     */
    public void cancel() {
        TaskManager.cancel(this);
    }

    /**
     * Checks whether this runnable has been cancelled (removed from the TaskManager).
     *
     * @return true if this runnable is no longer registered in the TaskManager
     */
    public boolean isCancelled() {
        return ! TaskManager.getCurrentRunnables().containsKey(this.index);
    }

    /**
     * Pauses this runnable so that it will not execute on subsequent ticks.
     */
    public void pause() {
        this.paused = true;
    }

    /**
     * Resumes this runnable so that it will execute on subsequent ticks.
     */
    public void resume() {
        this.paused = false;
    }

    @Override
    public String toString() {
        return "BaseRunnable{" +
                "startedAt=" + startedAt +
                ", currentTickCount=" + currentTickCount +
                ", period=" + period +
                ", index=" + index +
                ", paused=" + paused +
                '}';
    }
}
