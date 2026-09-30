package host.plas.bou.libs.usched.scheduling.tasks;

import org.bukkit.plugin.Plugin;

/**
 * A handle for work the scheduler refused to accept, such as a task bound to an entity that
 * has already been removed from the world. It never runs and always reports itself cancelled.
 */
public class CancelledScheduledTask implements MyScheduledTask {
    private final Plugin plugin;

    /**
     * @param plugin the plugin the work was scheduled for
     */
    public CancelledScheduledTask(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void cancel() {
        // Nothing is scheduled.
    }

    @Override
    public boolean isCancelled() {
        return true;
    }

    @Override
    public Plugin getOwningPlugin() {
        return plugin;
    }

    @Override
    public boolean isCurrentlyRunning() {
        return false;
    }

    @Override
    public boolean isRepeatingTask() {
        return false;
    }
}
