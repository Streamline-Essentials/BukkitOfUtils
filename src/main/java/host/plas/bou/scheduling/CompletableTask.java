package host.plas.bou.scheduling;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.libs.usched.UniversalScheduler;
import host.plas.bou.libs.usched.scheduling.schedulers.TaskScheduler;
import host.plas.bou.libs.usched.scheduling.tasks.MyScheduledTask;
import host.plas.bou.utils.VersionTool;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private volatile boolean cancelled;

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
     * The plugin whose code this task runs; tasks are scheduled under this plugin and are
     * cancelled when it is disabled.
     *
     * @return the owning plugin
     */
    @Setter(AccessLevel.NONE)
    private Plugin owningPlugin;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private final AtomicInteger nextCompletionKey = new AtomicInteger(0);
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private final Object settleLock = new Object();
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private volatile boolean settled;

    /** Tasks that have neither run nor been cancelled yet. */
    private static final Set<CompletableTask> PENDING = ConcurrentHashMap.newKeySet();
    /**
     * How often pending tasks are checked for having been cancelled outside of
     * {@link #cancel()} (by the server on plugin disable, or through {@link #getTask()}).
     */
    private static final long SWEEP_PERIOD_MILLIS = 1000L;
    private static final Object SWEEPER_LOCK = new Object();
    private static ScheduledExecutorService sweeper;

    /**
     * Constructs a CompletableTask wrapping the given scheduled task and injected runnable.
     *
     * <p>The task completes once: after the first execution of the runnable (including the first
     * run of a repeating task), or when it is cancelled. Completion callbacks run asynchronously
     * on the common fork-join pool, never on the server thread, and only when the task was not
     * cancelled. {@link #getFuture()} completes after the callbacks have run, or on cancel.</p>
     *
     * @param task             the underlying scheduled task
     * @param injectedRunnable the runnable being executed by the task
     */
    public CompletableTask(MyScheduledTask task, InjectedRunnable injectedRunnable) {
        this.task = task;
        this.injectedRunnable = injectedRunnable;
        this.cancelled = false;
        this.completionRunnables = new ConcurrentSkipListMap<>();
        this.future = new CompletableFuture<>();
        this.owningPlugin = resolveOwner(task, injectedRunnable);

        injectedRunnable.setOnFinish(this::settle);
        injectedRunnable.setOnRetire(this::cancel);

        if (task == null || task.isCancelled() || injectedRunnable.isRetired()) {
            cancel();
            return;
        }

        track(this);

        // The runnable may already have finished before the hook above was installed.
        if (injectedRunnable.isDone()) settle();
    }

    /**
     * Constructs a CompletableTask that runs the given runnable immediately.
     *
     * @param runnable the runnable to execute
     */
    public CompletableTask(InjectedRunnable runnable) {
        this(schedulerFor(runnable).runTask(runnable), runnable);
    }

    /**
     * Constructs a CompletableTask that runs the given runnable after a delay.
     *
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     */
    public CompletableTask(InjectedRunnable runnable, long delay) {
        this(schedulerFor(runnable).runTaskLater(runnable, delay), runnable);
    }

    /**
     * Constructs a CompletableTask that runs the given runnable repeatedly.
     *
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     */
    public CompletableTask(InjectedRunnable runnable, long delay, long period) {
        this(schedulerFor(runnable).runTaskTimer(runnable, delay, period), runnable);
    }

    /**
     * Constructs a CompletableTask bound to the given entity that runs immediately.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to execute
     */
    public CompletableTask(Entity entity, InjectedRunnable runnable) {
        this(schedulerFor(runnable).runTask(entity, runnable, runnable::retire), runnable);
    }

    /**
     * Constructs a CompletableTask bound to the given entity that runs after a delay.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     */
    public CompletableTask(Entity entity, InjectedRunnable runnable, long delay) {
        this(schedulerFor(runnable).runTaskLater(entity, runnable, runnable::retire, delay), runnable);
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
        this(schedulerFor(runnable).runTaskTimer(entity, runnable, runnable::retire, delay, period), runnable);
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
        this(schedulerFor(runnable).runTask(world, x, z, runnable), runnable);
    }

    /**
     * Constructs a CompletableTask bound to a chunk that runs immediately.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to execute
     */
    public CompletableTask(Chunk chunk, InjectedRunnable runnable) {
        this(schedulerFor(runnable).runTask(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable), runnable);
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
        this(schedulerFor(runnable).runTaskLater(world, x, z, runnable, delay), runnable);
    }

    /**
     * Constructs a CompletableTask bound to a chunk that runs after a delay.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     */
    public CompletableTask(Chunk chunk, InjectedRunnable runnable, long delay) {
        this(schedulerFor(runnable).runTaskLater(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay), runnable);
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
        this(schedulerFor(runnable).runTaskTimer(world, x, z, runnable, delay, period), runnable);
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
        this(schedulerFor(runnable).runTaskTimer(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay, period), runnable);
    }

    /**
     * Constructs a CompletableTask that teleports the given entity to the specified location.
     *
     * @param entityToTeleport the entity to teleport
     * @param location         the target location
     */
    public CompletableTask(Entity entityToTeleport, Location location) {
        this(entityToTeleport, new InjectedRunnable(() -> teleportNow(entityToTeleport, location)));
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
     * Completes the injected runnable with the given answer and completes this task, running
     * the completion callbacks unless the task was cancelled.
     *
     * @param answer the task answer to set
     * @return the injected runnable after setting the answer
     */
    public InjectedRunnable complete(TaskAnswer answer) {
        InjectedRunnable runnable = getInjectedRunnable().setAnswer(answer);
        settle();
        return runnable;
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
     * Runs all registered completion callbacks in priority order on the calling thread.
     * A callback that throws is logged and does not stop the ones after it.
     */
    public void runCompletion() {
        getCompletionRunnables().forEach((priority, runnable) -> runCallback(runnable));
    }

    /**
     * Registers a callback to run when this task completes. A callback registered after the task
     * has already completed runs right away (asynchronously); one registered on a cancelled task
     * never runs.
     *
     * @param runnable the callback to execute on completion
     * @return this CompletableTask for chaining
     */
    public CompletableTask whenComplete(Runnable runnable) {
        if (runnable == null) return this;

        synchronized (settleLock) {
            if (cancelled) return this;
            if (! settled) {
                getCompletionRunnables().put(nextCompletionKey.getAndIncrement(), runnable);
                return this;
            }
        }

        if (! isCancelled()) CompletableFuture.runAsync(() -> runCallback(runnable));
        return this;
    }

    /**
     * Checks whether this task has finished: its runnable has run, or it has been cancelled.
     *
     * @return true if the task will not run for the first time anymore
     */
    public boolean isTaskCompleted() {
        if (isTaskValid()) {
            if (task.isCancelled()) {
                cancel();
                return true;
            }
            return isDone();
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
     * Marks this task complete exactly once: stops tracking it, runs the completion callbacks
     * asynchronously, then completes {@link #getFuture()}.
     */
    private void settle() {
        synchronized (settleLock) {
            if (settled) return;
            settled = true;
        }
        PENDING.remove(this);

        if (cancelled) {
            future.complete(null);
            return;
        }

        CompletableFuture.runAsync(this::runCompletion)
                .whenComplete((ignored, error) -> future.complete(null));
    }

    /**
     * The plugin the task is actually registered under. It differs from the plugin that wrote the
     * runnable when that plugin was not enabled at scheduling time (for example, work scheduled
     * from its own disable logic), in which case BukkitOfUtils owns and runs it.
     */
    private static Plugin resolveOwner(MyScheduledTask task, InjectedRunnable runnable) {
        if (task != null) {
            try {
                Plugin owner = task.getOwningPlugin();
                if (owner != null) return owner;
            } catch (Throwable ignored) {
                // Fall back to the plugin that provides the runnable.
            }
        }
        return TaskManager.findOwner(ownerSource(runnable));
    }

    private static void runCallback(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            BukkitOfUtils.getInstance().logWarning("A completion callback of a scheduled task threw an exception.", t);
        }
    }

    private static void teleportNow(Entity entity, Location location) {
        if (UniversalScheduler.isExpandedSchedulingAvailable) {
            VersionTool.teleportAsync(entity, location);
        } else {
            entity.teleport(location);
        }
    }

    /**
     * The object whose class identifies the plugin a runnable belongs to: the caller's own
     * runnable, not the {@link InjectedRunnable} wrapper that BukkitOfUtils puts around it.
     */
    private static Object ownerSource(InjectedRunnable runnable) {
        if (runnable.getClass() == InjectedRunnable.class && runnable.getRunnable() != null) {
            return runnable.getRunnable();
        }
        return runnable;
    }

    private static TaskScheduler schedulerFor(InjectedRunnable runnable) {
        return TaskManager.schedulerFor(ownerSource(runnable));
    }

    private static void track(CompletableTask task) {
        PENDING.add(task);

        synchronized (SWEEPER_LOCK) {
            if (sweeper != null) return;

            sweeper = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "BukkitOfUtils-CompletableTask-Sweeper");
                thread.setDaemon(true);
                return thread;
            });
            sweeper.scheduleWithFixedDelay(CompletableTask::sweep, SWEEP_PERIOD_MILLIS, SWEEP_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private static void sweep() {
        for (CompletableTask task : PENDING) {
            try {
                if (task.settled) {
                    PENDING.remove(task);
                } else if (task.task == null || task.task.isCancelled()) {
                    task.cancel();
                }
            } catch (Throwable t) {
                PENDING.remove(task);
            }
        }
    }

    /**
     * Cancels every pending task owned by the given plugin.
     *
     * @param plugin the plugin whose pending tasks to cancel
     */
    public static void cancelOwnedBy(Plugin plugin) {
        if (plugin == null) return;

        for (CompletableTask task : PENDING) {
            if (task.owningPlugin == plugin) task.cancel();
        }
    }

    /**
     * Cancels every pending task and stops the background sweeper.
     */
    public static void cancelAll() {
        for (CompletableTask task : PENDING) {
            task.cancel();
        }
        PENDING.clear();

        synchronized (SWEEPER_LOCK) {
            if (sweeper != null) {
                sweeper.shutdownNow();
                sweeper = null;
            }
        }
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
