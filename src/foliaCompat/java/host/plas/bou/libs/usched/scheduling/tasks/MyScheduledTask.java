package host.plas.bou.libs.usched.scheduling.tasks;

import org.bukkit.plugin.Plugin;

/**
 * A platform-agnostic handle to a scheduled task.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level.</p>
 */
public interface MyScheduledTask {
    /**
     * Cancels the task.
     */
    void cancel();

    /**
     * @return true if the task has been cancelled
     */
    boolean isCancelled();

    /**
     * @return the plugin that owns this task
     */
    Plugin getOwningPlugin();

    /**
     * @return true if the task is executing right now
     */
    boolean isCurrentlyRunning();

    /**
     * @return true if the task repeats
     */
    boolean isRepeatingTask();
}
