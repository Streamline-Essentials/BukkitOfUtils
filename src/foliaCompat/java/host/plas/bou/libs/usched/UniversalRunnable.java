package host.plas.bou.libs.usched;

import host.plas.bou.libs.usched.scheduling.tasks.MyScheduledTask;
import org.bukkit.plugin.Plugin;

/**
 * A {@link Runnable} that remembers the task it was scheduled as, mirroring Bukkit's
 * {@code BukkitRunnable}.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level. Nothing inside
 * BOU uses this type, but plugins built on BOU may, so it is kept for API compatibility.</p>
 */
public abstract class UniversalRunnable implements Runnable {
    MyScheduledTask task;

    /**
     * Cancels this runnable's task.
     *
     * @throws IllegalStateException if it was never scheduled
     */
    public synchronized void cancel() throws IllegalStateException {
        checkScheduled();
        task.cancel();
    }

    /**
     * @return true if the task has been cancelled
     * @throws IllegalStateException if it was never scheduled
     */
    public synchronized boolean isCancelled() throws IllegalStateException {
        checkScheduled();
        return task.isCancelled();
    }

    /**
     * Schedules this runnable to run on the next tick.
     *
     * @param plugin the owning plugin
     * @return a handle to the scheduled task
     * @throws IllegalArgumentException if the plugin is null
     * @throws IllegalStateException    if already scheduled
     */
    public synchronized MyScheduledTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        checkNotYetScheduled();
        return setupTask(UniversalScheduler.getScheduler(plugin).runTask(this));
    }

    /**
     * Schedules this runnable off the main thread.
     *
     * @param plugin the owning plugin
     * @return a handle to the scheduled task
     * @throws IllegalArgumentException if the plugin is null
     * @throws IllegalStateException    if already scheduled
     */
    public synchronized MyScheduledTask runTaskAsynchronously(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        checkNotYetScheduled();
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskAsynchronously(this));
    }

    /**
     * Schedules this runnable after a delay.
     *
     * @param plugin the owning plugin
     * @param delay  delay in ticks
     * @return a handle to the scheduled task
     * @throws IllegalArgumentException if the plugin is null
     * @throws IllegalStateException    if already scheduled
     */
    public synchronized MyScheduledTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        checkNotYetScheduled();
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskLater(this, delay));
    }

    /**
     * Schedules this runnable off the main thread after a delay.
     *
     * @param plugin the owning plugin
     * @param delay  delay in ticks
     * @return a handle to the scheduled task
     * @throws IllegalArgumentException if the plugin is null
     * @throws IllegalStateException    if already scheduled
     */
    public synchronized MyScheduledTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        checkNotYetScheduled();
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskLaterAsynchronously(this, delay));
    }

    /**
     * Schedules this runnable to repeat.
     *
     * @param plugin the owning plugin
     * @param delay  initial delay in ticks
     * @param period period in ticks
     * @return a handle to the scheduled task
     * @throws IllegalArgumentException if the plugin is null
     * @throws IllegalStateException    if already scheduled
     */
    public synchronized MyScheduledTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        checkNotYetScheduled();
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskTimer(this, delay, period));
    }

    /**
     * Schedules this runnable to repeat off the main thread.
     *
     * @param plugin the owning plugin
     * @param delay  initial delay in ticks
     * @param period period in ticks
     * @return a handle to the scheduled task
     * @throws IllegalArgumentException if the plugin is null
     * @throws IllegalStateException    if already scheduled
     */
    public synchronized MyScheduledTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        checkNotYetScheduled();
        return setupTask(UniversalScheduler.getScheduler(plugin).runTaskTimerAsynchronously(this, delay, period));
    }

    private void checkScheduled() {
        if (task == null) {
            throw new IllegalStateException("Not scheduled yet");
        }
    }

    private void checkNotYetScheduled() {
        if (task != null) {
            throw new IllegalStateException("Already scheduled");
        }
    }

    private MyScheduledTask setupTask(MyScheduledTask task) {
        this.task = task;
        return task;
    }
}
