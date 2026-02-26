package host.plas.bou.scheduling;

import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.MessageUtils;
import gg.drak.thebase.async.AsyncUtils;
import javax.swing.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public abstract class BaseRunnable implements Runnable {
    private Date startedAt;
    private long currentTickCount;
    private long period;
    private int index;
    private boolean paused;
    private long ticksLived;
    private Timer timer;

    public BaseRunnable(long delay, long period) {
        this.startedAt = new Date();
        this.currentTickCount = delay * -1;
        this.period = period;
        this.index = TaskManager.getNextIndex();
        this.paused = false;
        this.ticksLived = 0;
        this.timer = createTimer();
        TaskManager.start(this);
    }

    public BaseRunnable(long period) {
        this(0, period);
    }

    public void load() {
        TaskManager.load(this);
    }

    public void unload() {
        TaskManager.unload(this);
    }

    public void start() {
        if (timer.isRunning()) return;
        timer.start();
    }

    public void stop() {
        if (!timer.isRunning()) return;
        timer.stop();
    }

    public void restart() {
        timer.restart();
    }

    public Timer createTimer() {
        return new Timer(BaseManager.getBaseConfig().getTickingFrequency(), e -> {
            try {
                tick();
            } catch (Throwable t) {
                MessageUtils.logDebug("Error while ticking runnable: " + this, t);
            }
        });
    }

    public CompletableFuture<Void> tick() {
        if (this.paused) return CompletableFuture.completedFuture(null);
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (this.currentTickCount >= this.period) {
            this.currentTickCount = 0;
            try {
                future = AsyncUtils.executeAsync(this);
            } catch (Throwable e) {
                MessageUtils.logDebug("Error while ticking runnable: " + this, e);
            }
        }
        countTicks();
        return future;
    }

    public void countTicks() {
        this.currentTickCount++;
        this.ticksLived++;
    }

    public void cancel() {
        TaskManager.cancel(this);
    }

    public boolean isCancelled() {
        return !TaskManager.getCurrentRunnables().containsKey(this.index);
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    @Override
    public String toString() {
        return "BaseRunnable{" + "startedAt=" + startedAt + ", currentTickCount=" + currentTickCount + ", period=" + period + ", index=" + index + ", paused=" + paused + '}';
    }

    public void setStartedAt(final Date startedAt) {
        this.startedAt = startedAt;
    }

    public void setCurrentTickCount(final long currentTickCount) {
        this.currentTickCount = currentTickCount;
    }

    public void setPeriod(final long period) {
        this.period = period;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void setPaused(final boolean paused) {
        this.paused = paused;
    }

    public void setTicksLived(final long ticksLived) {
        this.ticksLived = ticksLived;
    }

    public void setTimer(final Timer timer) {
        this.timer = timer;
    }

    public Date getStartedAt() {
        return this.startedAt;
    }

    public long getCurrentTickCount() {
        return this.currentTickCount;
    }

    public long getPeriod() {
        return this.period;
    }

    public int getIndex() {
        return this.index;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public long getTicksLived() {
        return this.ticksLived;
    }

    public Timer getTimer() {
        return this.timer;
    }
}
