package host.plas.bou.scheduling;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;

public class CompletableTask {
    private MyScheduledTask task;
    private InjectedRunnable injectedRunnable;
    private boolean cancelled;
    private CompletableFuture<Void> future;
    private ConcurrentSkipListMap<Integer, Runnable> completionRunnables;

    public CompletableTask(MyScheduledTask task, InjectedRunnable injectedRunnable) {
        this.task = task;
        this.injectedRunnable = injectedRunnable;
        this.cancelled = false;
        this.completionRunnables = new ConcurrentSkipListMap<>();
        this.future = CompletableFuture.runAsync(() -> {
            boolean invalid = !isTaskValid();
            while (!(isDone() || isCancelled() || isTaskCompleted() || invalid)) {
                if (!isTaskValid()) {
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

    public CompletableTask(InjectedRunnable runnable) {
        this(TaskManager.getScheduler().runTask(runnable), runnable);
    }

    public CompletableTask(InjectedRunnable runnable, long delay) {
        this(TaskManager.getScheduler().runTaskLater(runnable, delay), runnable);
    }

    public CompletableTask(InjectedRunnable runnable, long delay, long period) {
        this(TaskManager.getScheduler().runTaskTimer(runnable, delay, period), runnable);
    }

    public CompletableTask(Entity entity, InjectedRunnable runnable) {
        this(TaskManager.getScheduler().runTask(entity, runnable), runnable);
    }

    public CompletableTask(Entity entity, InjectedRunnable runnable, long delay) {
        this(TaskManager.getScheduler().runTaskLater(entity, runnable, delay), runnable);
    }

    public CompletableTask(Entity entity, InjectedRunnable runnable, long delay, long period) {
        this(TaskManager.getScheduler().runTaskTimer(entity, runnable, delay, period), runnable);
    }

    public CompletableTask(World world, int x, int z, InjectedRunnable runnable) {
        this(TaskManager.getScheduler().runTask(world, x, z, runnable), runnable);
    }

    public CompletableTask(Chunk chunk, InjectedRunnable runnable) {
        this(TaskManager.getScheduler().runTask(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable), runnable);
    }

    public CompletableTask(World world, int x, int z, InjectedRunnable runnable, long delay) {
        this(TaskManager.getScheduler().runTaskLater(world, x, z, runnable, delay), runnable);
    }

    public CompletableTask(Chunk chunk, InjectedRunnable runnable, long delay) {
        this(TaskManager.getScheduler().runTaskLater(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay), runnable);
    }

    public CompletableTask(World world, int x, int z, InjectedRunnable runnable, long delay, long period) {
        this(TaskManager.getScheduler().runTaskTimer(world, x, z, runnable, delay, period), runnable);
    }

    public CompletableTask(Chunk chunk, InjectedRunnable runnable, long delay, long period) {
        this(TaskManager.getScheduler().runTaskTimer(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay, period), runnable);
    }

    public CompletableTask(Entity entityToTeleport, Location location) {
        this(TaskManager.getScheduler().teleport(entityToTeleport, location), new InjectedRunnable(() -> {
        })); // fix later...
    }

    public void cancel() {
        if (task != null) task.cancel();
        cancelled = true;
        completionRunnables.clear();
        complete();
    }

    public boolean isDone() {
        return injectedRunnable.isDone();
    }

    public InjectedRunnable complete(TaskAnswer answer) {
        return getInjectedRunnable().setAnswer(answer);
    }

    public InjectedRunnable complete() {
        return complete(TaskAnswer.REJECTED);
    }

    public void runCompletion() {
        getCompletionRunnables().forEach((priority, runnable) -> {
            runnable.run();
        });
    }

    public CompletableTask whenComplete(Runnable runnable) {
        getCompletionRunnables().put(getCompletionRunnables().size(), runnable);
        return this;
    }

    public boolean isTaskCompleted() {
        if (isTaskValid()) {
            if (task.isCancelled()) {
                cancel();
                return true;
            }
            return !task.isCurrentlyRunning();
        }
        return false;
    }

    public boolean isTaskValid() {
        return task != null;
    }

    public static CompletableTask of(Runnable runnable) {
        return new CompletableTask(new InjectedRunnable(runnable));
    }

    public static CompletableTask of(Runnable runnable, long delay) {
        return new CompletableTask(new InjectedRunnable(runnable), delay);
    }

    public static CompletableTask of(Runnable runnable, long delay, long period) {
        return new CompletableTask(new InjectedRunnable(runnable), delay, period);
    }

    public static CompletableTask of(Entity entity, Runnable runnable) {
        return new CompletableTask(entity, new InjectedRunnable(runnable));
    }

    public static CompletableTask of(Entity entity, Runnable runnable, long delay) {
        return new CompletableTask(entity, new InjectedRunnable(runnable), delay);
    }

    public static CompletableTask of(Entity entity, Runnable runnable, long delay, long period) {
        return new CompletableTask(entity, new InjectedRunnable(runnable), delay, period);
    }

    public static CompletableTask of(World world, int x, int z, Runnable runnable) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable));
    }

    public static CompletableTask of(Chunk chunk, Runnable runnable) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable));
    }

    public static CompletableTask of(World world, int x, int z, Runnable runnable, long delay) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable), delay);
    }

    public static CompletableTask of(Chunk chunk, Runnable runnable, long delay) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable), delay);
    }

    public static CompletableTask of(World world, int x, int z, Runnable runnable, long delay, long period) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable), delay, period);
    }

    public static CompletableTask of(Chunk chunk, Runnable runnable, long delay, long period) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable), delay, period);
    }

    public static CompletableTask of(Entity entityToTeleport, Location location) {
        return new CompletableTask(entityToTeleport, location);
    }

    public MyScheduledTask getTask() {
        return this.task;
    }

    public InjectedRunnable getInjectedRunnable() {
        return this.injectedRunnable;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public CompletableFuture<Void> getFuture() {
        return this.future;
    }

    public ConcurrentSkipListMap<Integer, Runnable> getCompletionRunnables() {
        return this.completionRunnables;
    }

    public void setTask(final MyScheduledTask task) {
        this.task = task;
    }

    public void setInjectedRunnable(final InjectedRunnable injectedRunnable) {
        this.injectedRunnable = injectedRunnable;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setFuture(final CompletableFuture<Void> future) {
        this.future = future;
    }

    public void setCompletionRunnables(final ConcurrentSkipListMap<Integer, Runnable> completionRunnables) {
        this.completionRunnables = completionRunnables;
    }
}
