package host.plas.bou.scheduling;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A task wrapper that combines a scheduled Bukkit task with a CompletableFuture,
 * allowing callers to chain completion callbacks and track task lifecycle.
 */
@Getter @Setter
public class CompletableTask {
    /**
     * The underlying scheduled task.
     *
     * @param task the scheduled task to set
     * @return the scheduled task
     */
    private MyScheduledTask task;
    /**
     * The runnable being executed by the task.
     *
     * @param injectedRunnable the injected runnable to set
     * @return the injected runnable
     */
    private InjectedRunnable injectedRunnable;
    /**
     * Whether this task has been cancelled.
     *
     * @param cancelled the cancelled state to set
     * @return true if the task is cancelled
     */
    private boolean cancelled;

    /**
     * The completable future tracking task lifecycle.
     *
     * @param future the future to set
     * @return the future
     */
    private CompletableFuture<Void> future;

    /**
     * Priority-ordered map of callbacks to run on task completion.
     *
     * @param completionRunnables the completion runnables map to set
     * @return the completion runnables map
     */
    private ConcurrentSkipListMap<Integer, Runnable> completionRunnables;

    /**
     * Constructs a CompletableTask wrapping the given scheduled task and injected runnable.
     * Starts an async future that waits for task completion and runs completion callbacks.
     *
     * @param task             the underlying scheduled task
     * @param injectedRunnable the runnable being executed by the task
     */
    public CompletableTask(MyScheduledTask task, InjectedRunnable injectedRunnable) {
        this.task = task;
        this.injectedRunnable = injectedRunnable;
        this.cancelled = false;
        this.completionRunnables = new ConcurrentSkipListMap<>();

        this.future = CompletableFuture.runAsync(() -> {
            boolean invalid = ! isTaskValid();

            while (! (isDone() || isCancelled() || isTaskCompleted() || invalid)) {
                if (! isTaskValid()) {
                    invalid = true;
                    break;
                }

                Thread.onSpinWait();
            }

            if (invalid) {
                cancel();
                return;
            }

            if (isCancelled()) cancel();

            runCompletion();
        });
    }

    /**
     * Constructs a CompletableTask that runs the given runnable immediately.
     *
     * @param runnable the runnable to execute
     */
    public CompletableTask(InjectedRunnable runnable) {
        this(TaskManager.getScheduler().runTask(runnable), runnable);
    }

    /**
     * Constructs a CompletableTask that runs the given runnable after a delay.
     *
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     */
    public CompletableTask(InjectedRunnable runnable, long delay) {
        this(TaskManager.getScheduler().runTaskLater(runnable, delay), runnable);
    }

    /**
     * Constructs a CompletableTask that runs the given runnable repeatedly.
     *
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     */
    public CompletableTask(InjectedRunnable runnable, long delay, long period) {
        this(TaskManager.getScheduler().runTaskTimer(runnable, delay, period), runnable);
    }

    /**
     * Constructs a CompletableTask bound to the given entity that runs immediately.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to execute
     */
    public CompletableTask(Entity entity, InjectedRunnable runnable) {
        this(TaskManager.getScheduler().runTask(entity, runnable), runnable);
    }

    /**
     * Constructs a CompletableTask bound to the given entity that runs after a delay.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     */
    public CompletableTask(Entity entity, InjectedRunnable runnable, long delay) {
        this(TaskManager.getScheduler().runTaskLater(entity, runnable, delay), runnable);
    }

    /**
     * Constructs a CompletableTask bound to the given entity that runs repeatedly.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     */
    public CompletableTask(Entity entity, InjectedRunnable runnable, long delay, long period) {
        this(TaskManager.getScheduler().runTaskTimer(entity, runnable, delay, period), runnable);
    }

    /**
     * Constructs a CompletableTask bound to a region by world and chunk coordinates that runs immediately.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to execute
     */
    public CompletableTask(World world, int x, int z, InjectedRunnable runnable) {
        this(TaskManager.getScheduler().runTask(world, x, z, runnable), runnable);
    }

    /**
     * Constructs a CompletableTask bound to a chunk that runs immediately.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to execute
     */
    public CompletableTask(Chunk chunk, InjectedRunnable runnable) {
        this(TaskManager.getScheduler().runTask(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable), runnable);
    }

    /**
     * Constructs a CompletableTask bound to a region by world and chunk coordinates that runs after a delay.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     */
    public CompletableTask(World world, int x, int z, InjectedRunnable runnable, long delay) {
        this(TaskManager.getScheduler().runTaskLater(world, x, z, runnable, delay), runnable);
    }

    /**
     * Constructs a CompletableTask bound to a chunk that runs after a delay.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     */
    public CompletableTask(Chunk chunk, InjectedRunnable runnable, long delay) {
        this(TaskManager.getScheduler().runTaskLater(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay), runnable);
    }

    /**
     * Constructs a CompletableTask bound to a region by world and chunk coordinates that runs repeatedly.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     */
    public CompletableTask(World world, int x, int z, InjectedRunnable runnable, long delay, long period) {
        this(TaskManager.getScheduler().runTaskTimer(world, x, z, runnable, delay, period), runnable);
    }

    /**
     * Constructs a CompletableTask bound to a chunk that runs repeatedly.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     */
    public CompletableTask(Chunk chunk, InjectedRunnable runnable, long delay, long period) {
        this(TaskManager.getScheduler().runTaskTimer(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay, period), runnable);
    }

    /**
     * Constructs a CompletableTask that teleports the given entity to the specified location.
     *
     * @param entityToTeleport the entity to teleport
     * @param location         the target location
     */
    public CompletableTask(Entity entityToTeleport, Location location) {
        this(TaskManager.getScheduler().teleport(entityToTeleport, location), new InjectedRunnable(() -> {})); // fix later...
    }

    /**
     * Cancels this task, clears all completion callbacks, and marks the future as complete.
     */
    public void cancel() {
        if (task != null) task.cancel();
        cancelled = true;

        completionRunnables.clear();

        complete();
    }

    /**
     * Checks whether the injected runnable has finished executing.
     *
     * @return true if the runnable is done
     */
    public boolean isDone() {
        return injectedRunnable.isDone();
    }

    /**
     * Completes the injected runnable with the given answer.
     *
     * @param answer the task answer to set
     * @return the injected runnable after setting the answer
     */
    public InjectedRunnable complete(TaskAnswer answer) {
        return getInjectedRunnable().setAnswer(answer);
    }

    /**
     * Completes the injected runnable with a REJECTED answer.
     *
     * @return the injected runnable after setting the answer
     */
    public InjectedRunnable complete() {
        return complete(TaskAnswer.REJECTED);
    }

    /**
     * Runs all registered completion callbacks in priority order.
     */
    public void runCompletion() {
        getCompletionRunnables().forEach((priority, runnable) -> {
            runnable.run();
        });
    }

    /**
     * Registers a callback to run when this task completes.
     *
     * @param runnable the callback to execute on completion
     * @return this CompletableTask for chaining
     */
    public CompletableTask whenComplete(Runnable runnable) {
        getCompletionRunnables().put(getCompletionRunnables().size(), runnable);

        return this;
    }

    /**
     * Checks whether the underlying scheduled task has completed or been cancelled.
     *
     * @return true if the task is no longer running
     */
    public boolean isTaskCompleted() {
        if (isTaskValid()) {
            if (task.isCancelled()) {
                cancel();
                return true;
            }
            return ! task.isCurrentlyRunning();
        }

        return false;
    }

    /**
     * Checks whether the underlying scheduled task reference is non-null.
     *
     * @return true if the task is not null
     */
    public boolean isTaskValid() {
        return task != null;
    }

    /**
     * Creates a CompletableTask that runs the given runnable immediately.
     *
     * @param runnable the runnable to execute
     * @return a new CompletableTask
     */
    public static CompletableTask of(Runnable runnable) {
        return new CompletableTask(new InjectedRunnable(runnable));
    }

    /**
     * Creates a CompletableTask that runs the given runnable after a delay.
     *
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     * @return a new CompletableTask
     */
    public static CompletableTask of(Runnable runnable, long delay) {
        return new CompletableTask(new InjectedRunnable(runnable), delay);
    }

    /**
     * Creates a CompletableTask that runs the given runnable repeatedly.
     *
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return a new CompletableTask
     */
    public static CompletableTask of(Runnable runnable, long delay, long period) {
        return new CompletableTask(new InjectedRunnable(runnable), delay, period);
    }

    /**
     * Creates a CompletableTask bound to an entity that runs immediately.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to execute
     * @return a new CompletableTask
     */
    public static CompletableTask of(Entity entity, Runnable runnable) {
        return new CompletableTask(entity, new InjectedRunnable(runnable));
    }

    /**
     * Creates a CompletableTask bound to an entity that runs after a delay.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     * @return a new CompletableTask
     */
    public static CompletableTask of(Entity entity, Runnable runnable, long delay) {
        return new CompletableTask(entity, new InjectedRunnable(runnable), delay);
    }

    /**
     * Creates a CompletableTask bound to an entity that runs repeatedly.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return a new CompletableTask
     */
    public static CompletableTask of(Entity entity, Runnable runnable, long delay, long period) {
        return new CompletableTask(entity, new InjectedRunnable(runnable), delay, period);
    }

    /**
     * Creates a CompletableTask bound to a region that runs immediately.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to execute
     * @return a new CompletableTask
     */
    public static CompletableTask of(World world, int x, int z, Runnable runnable) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable));
    }

    /**
     * Creates a CompletableTask bound to a chunk that runs immediately.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to execute
     * @return a new CompletableTask
     */
    public static CompletableTask of(Chunk chunk, Runnable runnable) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable));
    }

    /**
     * Creates a CompletableTask bound to a region that runs after a delay.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     * @return a new CompletableTask
     */
    public static CompletableTask of(World world, int x, int z, Runnable runnable, long delay) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable), delay);
    }

    /**
     * Creates a CompletableTask bound to a chunk that runs after a delay.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     * @return a new CompletableTask
     */
    public static CompletableTask of(Chunk chunk, Runnable runnable, long delay) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable), delay);
    }

    /**
     * Creates a CompletableTask bound to a region that runs repeatedly.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return a new CompletableTask
     */
    public static CompletableTask of(World world, int x, int z, Runnable runnable, long delay, long period) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable), delay, period);
    }

    /**
     * Creates a CompletableTask bound to a chunk that runs repeatedly.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return a new CompletableTask
     */
    public static CompletableTask of(Chunk chunk, Runnable runnable, long delay, long period) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable), delay, period);
    }

    /**
     * Creates a CompletableTask that teleports an entity to the given location.
     *
     * @param entityToTeleport the entity to teleport
     * @param location         the target location
     * @return a new CompletableTask
     */
    public static CompletableTask of(Entity entityToTeleport, Location location) {
        return new CompletableTask(entityToTeleport, location);
    }
}
