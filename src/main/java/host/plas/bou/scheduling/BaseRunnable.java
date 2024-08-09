package host.plas.bou.scheduling;

import host.plas.bou.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public abstract class BaseRunnable implements Runnable {
    private Date startedAt;
    private long currentTickCount;
    private long period;
    private int index;
    private boolean paused;

    /**
     * Constructor for all Streamline API-ed Runnables.
     *
     * @param delay the delay of the task (in ticks)
     * @param period the period of the task (in ticks)
     */
    public BaseRunnable(long delay, long period) {
        this.startedAt = new Date();
        this.currentTickCount = delay * -1;
        this.period = period;
        this.index = TaskManager.getNextIndex();
        this.paused = false;

        TaskManager.start(this);
    }

    public void tick() {
        if (this.paused) return;

        if (this.currentTickCount >= this.period) {
            this.currentTickCount = 0;
            try {
                this.run();
            } catch (Throwable e) {
                MessageUtils.logDebug("Error while ticking runnable: " + this, e);
            }
        }

        this.currentTickCount ++;
    }

    public void cancel() {
        TaskManager.cancel(this);
    }

    public boolean isCancelled() {
        return ! TaskManager.getCurrentRunnables().containsKey(this.index);
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }
}
