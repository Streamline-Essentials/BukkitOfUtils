package host.plas.bou.libs.universalScheduler.bukkitScheduler;

import host.plas.bou.libs.universalScheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * A {@link MyScheduledTask} backed by a plain {@link BukkitTask}.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level.</p>
 */
public class BukkitScheduledTask implements MyScheduledTask {
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
        return task.isCancelled();
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
