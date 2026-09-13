package host.plas.bou.libs.usched.bukkit;

import host.plas.bou.libs.usched.scheduling.tasks.MyScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * A {@link MyScheduledTask} backed by a plain {@link BukkitTask}.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level.</p>
 */
public class BukkitScheduledTask implements MyScheduledTask {
    /**
     * BukkitTask#isCancelled() was added after 1.8 — that API declares only getTaskId,
     * getOwner, isSync and cancel. Probe once so modern servers keep the upstream
     * behaviour while 1.8.x falls back to the scheduler's queue state.
     */
    private static final boolean HAS_IS_CANCELLED = hasIsCancelled();

    private static boolean hasIsCancelled() {
        try {
            BukkitTask.class.getMethod("isCancelled");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    final BukkitTask task;
    final boolean isRepeating;

    /**
     * Wraps a non-repeating Bukkit task.
     *
     * @param task the underlying task
     */
    public BukkitScheduledTask(BukkitTask task) {
        this(task, false);
    }

    /**
     * Wraps a Bukkit task.
     *
     * @param task        the underlying task
     * @param isRepeating whether the task repeats
     */
    public BukkitScheduledTask(BukkitTask task, boolean isRepeating) {
        this.task = task;
        this.isRepeating = isRepeating;
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public boolean isCancelled() {
        if (HAS_IS_CANCELLED) return task.isCancelled();

        // 1.8.x fallback: a live task is either waiting to run or running right now.
        int id = task.getTaskId();
        return ! Bukkit.getScheduler().isQueued(id) && ! Bukkit.getScheduler().isCurrentlyRunning(id);
    }

    @Override
    public Plugin getOwningPlugin() {
        return task.getOwner();
    }

    @Override
    public boolean isCurrentlyRunning() {
        return Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId());
    }

    @Override
    public boolean isRepeatingTask() {
        return isRepeating;
    }
}
