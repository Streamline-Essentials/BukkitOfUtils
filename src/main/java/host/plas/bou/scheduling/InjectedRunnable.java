package host.plas.bou.scheduling;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A Runnable wrapper that tracks execution status via a TaskAnswer.
 * Wraps a delegate runnable and records whether it completed successfully or with an error.
 */
@Getter @Setter
public class InjectedRunnable implements Runnable {
    /**
     * The delegate runnable to be executed.
     *
     * @param runnable the delegate runnable to set
     * @return the delegate runnable
     */
    private Runnable runnable;
    /**
     * The atomic reference tracking the current execution status of this task.
     *
     * @param injected the task answer reference to set
     * @return the task answer reference
     */
    private AtomicReference<TaskAnswer> injected;
    /**
     * Invoked once after every execution, after the answer has been recorded.
     *
     * @param onFinish the hook to set, or null for none
     * @return the finish hook, or null
     */
    private volatile Runnable onFinish;
    /**
     * Invoked when the scheduler drops this runnable without running it, for example because
     * the entity it was bound to was removed from the world.
     *
     * @param onRetire the hook to set, or null for none
     * @return the retire hook, or null
     */
    private volatile Runnable onRetire;
    /**
     * Whether the scheduler has dropped this runnable without running it.
     *
     * @param retired the retired state to set
     * @return true if this runnable was retired
     */
    private volatile boolean retired;

    /**
     * Constructs an InjectedRunnable with the given delegate runnable and initial task answer.
     *
     * @param runnable the delegate runnable to execute
     * @param injected the initial task answer reference
     */
    public InjectedRunnable(Runnable runnable, AtomicReference<TaskAnswer> injected) {
        this.runnable = runnable;
        this.injected = injected;
    }

    /**
     * Constructs an InjectedRunnable with the given delegate runnable and a default RUNNING status.
     *
     * @param runnable the delegate runnable to execute
     */
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
        } finally {
            Runnable hook = onFinish;
            if (hook != null) hook.run();
        }
    }

    /**
     * Marks this runnable as dropped by the scheduler: records a REJECTED answer and invokes
     * the retire hook. Passed to entity schedulers as their "retired" callback.
     */
    public void retire() {
        retired = true;
        injected.set(TaskAnswer.REJECTED);

        Runnable hook = onRetire;
        if (hook != null) hook.run();
    }

    /**
     * Gets the current task answer value.
     *
     * @return the current TaskAnswer
     */
    public TaskAnswer getInjectedNow() {
        return injected.get();
    }

    /**
     * Checks whether the runnable has finished executing (i.e., status is no longer RUNNING).
     *
     * @return true if the task is done
     */
    public boolean isDone() {
        return injected.get().isDone();
    }

    /**
     * Sets the task answer to the specified value.
     *
     * @param answer the TaskAnswer to set
     * @return this InjectedRunnable for chaining
     */
    public InjectedRunnable setAnswer(TaskAnswer answer) {
        injected.set(answer);
        return this;
    }
}
