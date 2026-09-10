package host.plas.bou.libs.universalScheduler.scheduling.schedulers;

import host.plas.bou.libs.universalScheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Platform-agnostic scheduler abstraction covering Bukkit, Paper and Folia.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level; the upstream
 * artifact ships class file version 65.0 (Java 21) and cannot load on the Java 11
 * runtimes BOU supports.</p>
 *
 * <p>No method signature here may mention a Folia-only type. Folia classes appear only
 * inside the FoliaScheduler body and fields, so this interface stays loadable on servers
 * where {@code io.papermc.paper.threadedregions} does not exist.</p>
 */
public interface TaskScheduler {
    /**
     * @return true if the current thread is the global/main tick thread
     */
    boolean isGlobalThread();

    /**
     * @return true if the current thread ticks any region
     */
    default boolean isTickThread() {
        return isGlobalThread();
    }

    /**
     * @param entity the entity to check ownership for
     * @return true if the current thread owns the entity
     */
    boolean isEntityThread(Entity entity);

    /**
     * @param location the location to check ownership for
     * @return true if the current thread owns the region
     */
    boolean isRegionThread(Location location);

    /**
     * Runs a task on the next tick.
     *
     * @param runnable the work to run
     * @return a handle to the scheduled task
     */
    MyScheduledTask runTask(Runnable runnable);

    /**
     * Runs a task after a delay.
     *
     * @param runnable the work to run
     * @param delay    delay in ticks
     * @return a handle to the scheduled task
     */
    MyScheduledTask runTaskLater(Runnable runnable, long delay);

    /**
     * Runs a repeating task.
     *
     * @param runnable the work to run
     * @param delay    initial delay in ticks
     * @param period   period in ticks
     * @return a handle to the scheduled task
     */
    MyScheduledTask runTaskTimer(Runnable runnable, long delay, long period);

    /**
     * @param plugin   ignored; retained for upstream API compatibility
     * @param runnable the work to run
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTask(Plugin plugin, Runnable runnable) {
        return runTask(runnable);
    }

    /**
     * @param plugin   ignored; retained for upstream API compatibility
     * @param runnable the work to run
     * @param delay    delay in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        return runTaskLater(runnable, delay);
    }

    /**
     * @param plugin   ignored; retained for upstream API compatibility
     * @param runnable the work to run
     * @param delay    initial delay in ticks
     * @param period   period in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
        return runTaskTimer(runnable, delay, period);
    }

    /**
     * @param location the region context
     * @param runnable the work to run
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTask(Location location, Runnable runnable) {
        return runTask(runnable);
    }

    /**
     * @param location the region context
     * @param runnable the work to run
     * @param delay    delay in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskLater(Location location, Runnable runnable, long delay) {
        return runTaskLater(runnable, delay);
    }

    /**
     * @param location the region context
     * @param runnable the work to run
     * @param delay    initial delay in ticks
     * @param period   period in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskTimer(Location location, Runnable runnable, long delay, long period) {
        return runTaskTimer(runnable, delay, period);
    }

    /**
     * @param runnable the work to run
     * @param delay    delay in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask scheduleSyncDelayedTask(Runnable runnable, long delay) {
        return runTaskLater(runnable, delay);
    }

    /**
     * @param runnable the work to run
     * @return a handle to the scheduled task
     */
    default MyScheduledTask scheduleSyncDelayedTask(Runnable runnable) {
        return runTask(runnable);
    }

    /**
     * @param runnable the work to run
     * @param delay    initial delay in ticks
     * @param period   period in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
        return runTaskTimer(runnable, delay, period);
    }

    /**
     * @param entity   the entity context
     * @param runnable the work to run
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTask(Entity entity, Runnable runnable) {
        return runTask(runnable);
    }

    /**
     * @param entity   the entity context
     * @param runnable the work to run
     * @param delay    delay in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskLater(Entity entity, Runnable runnable, long delay) {
        return runTaskLater(runnable, delay);
    }

    /**
     * @param entity   the entity context
     * @param runnable the work to run
     * @param delay    initial delay in ticks
     * @param period   period in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskTimer(Entity entity, Runnable runnable, long delay, long period) {
        return runTaskTimer(runnable, delay, period);
    }

    /**
     * @param world    the world containing the region
     * @param x        chunk X
     * @param z        chunk Z
     * @param runnable the work to run
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTask(World world, int x, int z, Runnable runnable) {
        return runTask(runnable);
    }

    /**
     * @param world    the world containing the region
     * @param x        chunk X
     * @param z        chunk Z
     * @param runnable the work to run
     * @param delay    delay in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskLater(World world, int x, int z, Runnable runnable, long delay) {
        return runTaskLater(runnable, delay);
    }

    /**
     * @param world    the world containing the region
     * @param x        chunk X
     * @param z        chunk Z
     * @param runnable the work to run
     * @param delay    initial delay in ticks
     * @param period   period in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskTimer(World world, int x, int z, Runnable runnable, long delay, long period) {
        return runTaskTimer(runnable, delay, period);
    }

    /**
     * Runs a task off the main thread.
     *
     * @param runnable the work to run
     * @return a handle to the scheduled task
     */
    MyScheduledTask runTaskAsynchronously(Runnable runnable);

    /**
     * Runs a task off the main thread after a delay.
     *
     * @param runnable the work to run
     * @param delay    delay in ticks
     * @return a handle to the scheduled task
     */
    MyScheduledTask runTaskLaterAsynchronously(Runnable runnable, long delay);

    /**
     * Runs a repeating task off the main thread.
     *
     * @param runnable the work to run
     * @param delay    initial delay in ticks
     * @param period   period in ticks
     * @return a handle to the scheduled task
     */
    MyScheduledTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period);

    /**
     * @param plugin   ignored; retained for upstream API compatibility
     * @param runnable the work to run
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        return runTaskAsynchronously(runnable);
    }

    /**
     * @param plugin   ignored; retained for upstream API compatibility
     * @param runnable the work to run
     * @param delay    delay in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
        return runTaskLaterAsynchronously(runnable, delay);
    }

    /**
     * @param plugin   ignored; retained for upstream API compatibility
     * @param runnable the work to run
     * @param delay    initial delay in ticks
     * @param period   period in ticks
     * @return a handle to the scheduled task
     */
    default MyScheduledTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long period) {
        return runTaskTimerAsynchronously(runnable, delay, period);
    }

    /**
     * Runs a callable on the sync thread and returns its future result.
     *
     * @param callable the work to run
     * @param <T>      the result type
     * @return a future completed with the result, or completed exceptionally on failure
     */
    default <T> Future<T> callSyncMethod(Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();

        execute(() -> {
            try {
                future.complete(callable.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    /**
     * Executes work on the appropriate thread as soon as possible.
     *
     * @param runnable the work to run
     */
    void execute(Runnable runnable);

    /**
     * @param location the region context
     * @param runnable the work to run
     */
    default void execute(Location location, Runnable runnable) {
        execute(runnable);
    }

    /**
     * @param entity   the entity context
     * @param runnable the work to run
     */
    default void execute(Entity entity, Runnable runnable) {
        execute(runnable);
    }

    /**
     * Cancels all tasks owned by the plugin backing this scheduler.
     */
    void cancelTasks();

    /**
     * Cancels all tasks owned by the given plugin.
     *
     * @param plugin the owning plugin
     */
    void cancelTasks(Plugin plugin);

    /**
     * Teleports an entity in a way that is safe for the current platform.
     *
     * @param entity   the entity to move
     * @param location the destination
     * @return a handle to the scheduled task
     */
    MyScheduledTask teleport(Entity entity, Location location);
}
