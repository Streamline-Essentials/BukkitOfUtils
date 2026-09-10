package host.plas.bou.libs.universalScheduler.foliaScheduler;

import host.plas.bou.libs.universalScheduler.scheduling.tasks.MyScheduledTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

/**
 * A {@link MyScheduledTask} backed by a Folia {@link ScheduledTask}.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level.</p>
 *
 * <p>This class references Folia-only types, so it must never be loaded on a non-Folia
 * server. Only {@link FoliaScheduler} touches it, and that class is itself reached only
 * when Folia has been detected.</p>
 */
public class FoliaScheduledTask implements MyScheduledTask {
    private final ScheduledTask task;

    /**
     * @param task the underlying Folia task
     */
    public FoliaScheduledTask(ScheduledTask task) {
        this.task = task;
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
        return task.getOwningPlugin();
    }

    @Override
    public boolean isCurrentlyRunning() {
        ScheduledTask.ExecutionState state = task.getExecutionState();
        return state == ScheduledTask.ExecutionState.RUNNING
                || state == ScheduledTask.ExecutionState.CANCELLED_RUNNING;
    }

    @Override
    public boolean isRepeatingTask() {
        return task.isRepeatingTask();
    }
}
