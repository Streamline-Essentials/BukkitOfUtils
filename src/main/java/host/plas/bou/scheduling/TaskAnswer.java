package host.plas.bou.scheduling;

/**
 * Represents the possible states of a scheduled task's execution result.
 */
public enum TaskAnswer {
    /** The task was accepted and completed successfully. */
    ACCEPTED,
    /** The task was rejected and not executed. */
    REJECTED,
    /** The task encountered an error during execution. */
    ERROR,
    /** The task result is null or unavailable. */
    NULL,
    /** The task is currently running and has not yet completed. */
    RUNNING,
    ;

    /**
     * Checks whether this answer represents a completed (non-running) state.
     *
     * @return true if this answer is not RUNNING
     */
    public boolean isDone() {
        return this != RUNNING;
    }
}
