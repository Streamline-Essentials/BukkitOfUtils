package host.plas.bou.scheduling;

import java.util.concurrent.atomic.AtomicReference;

public class InjectedRunnable implements Runnable {
    private Runnable runnable;
    private AtomicReference<TaskAnswer> injected;

    public InjectedRunnable(Runnable runnable, AtomicReference<TaskAnswer> injected) {
        this.runnable = runnable;
        this.injected = injected;
    }

    public InjectedRunnable(Runnable runnable) {
        this(runnable, new AtomicReference<>(TaskAnswer.RUNNING));
    }

    @Override
    public void run() {
        try {
            runnable.run();
            injected.set(TaskAnswer.ACCEPTED);
        } catch (Throwable e) {
            e.printStackTrace();
            injected.set(TaskAnswer.ERROR);
        }
    }

    public TaskAnswer getInjectedNow() {
        return injected.get();
    }

    public boolean isDone() {
        return injected.get().isDone();
    }

    public InjectedRunnable setAnswer(TaskAnswer answer) {
        injected.set(answer);
        return this;
    }

    public Runnable getRunnable() {
        return this.runnable;
    }

    public AtomicReference<TaskAnswer> getInjected() {
        return this.injected;
    }

    public void setRunnable(final Runnable runnable) {
        this.runnable = runnable;
    }

    public void setInjected(final AtomicReference<TaskAnswer> injected) {
        this.injected = injected;
    }
}
