package host.plas.bou.scheduling;

import com.github.Anon8281.universalScheduler.foliaScheduler.FoliaScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.BetterPlugin;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.ClassHelper;
import host.plas.bou.utils.VersionTool;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import gg.drak.thebase.async.AsyncTask;
import gg.drak.thebase.async.AsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Central manager for scheduling and tracking runnables, async tasks, and Bukkit scheduled tasks.
 * Provides utility methods for running tasks on entities, regions, chunks, and the global scheduler.
 */
public class TaskManager {
    /** Private constructor to prevent instantiation of this utility class. */
    private TaskManager() {}

    /**
     * A sorted map of all currently registered runnables, keyed by their index.
     *
     * @param currentRunnables the map of runnables to set
     * @return the map of currently registered runnables
     */
    @Getter @Setter
    private static ConcurrentSkipListMap<Integer, BaseRunnable> currentRunnables = new ConcurrentSkipListMap<>();

    /**
     * Registers a runnable in the current runnables map.
     *
     * @param runnable the runnable to register
     */
    public static void load(BaseRunnable runnable) {
        getCurrentRunnables().put(runnable.getIndex(), runnable);
    }

    /**
     * Unregisters a runnable by its index from the current runnables map.
     *
     * @param index the index of the runnable to remove
     */
    public static void unload(int index) {
        getCurrentRunnables().remove(index);
    }

    /**
     * Unregisters the given runnable from the current runnables map.
     *
     * @param runnable the runnable to remove
     */
    public static void unload(BaseRunnable runnable) {
        unload(runnable.getIndex());
    }

    /**
     * Registers and starts the given runnable.
     *
     * @param runnable the runnable to register and start
     */
    public static void start(BaseRunnable runnable) {
        load(runnable);
        runnable.start();
    }

    /**
     * Cancels a runnable by its index. Logs a warning if the runnable is not found.
     *
     * @param index the index of the runnable to cancel
     */
    public static void cancel(int index) {
        BaseRunnable runnable = getRunnable(index);
        if (runnable == null) {
            BukkitOfUtils.getInstance().logWarning("Failed to cancel runnable with index: " + index);
            return;
        }

        cancel(runnable);
    }

    /**
     * Stops and unregisters the given runnable.
     *
     * @param runnable the runnable to cancel
     */
    public static void cancel(BaseRunnable runnable) {
        runnable.stop();
        unload(runnable.getIndex());
    }

    /**
     * Returns the next available index for a new runnable.
     *
     * @return the next index based on the current map size
     */
    public static int getNextIndex() {
        return currentRunnables.size();
    }

    /**
     * Checks whether a runnable with the given index has been cancelled.
     *
     * @param index the index to check
     * @return true if no runnable with the given index exists
     */
    public static boolean isCancelled(int index) {
        return ! currentRunnables.containsKey(index);
    }

    /**
     * Retrieves the runnable registered at the given index.
     *
     * @param index the index of the runnable
     * @return the BaseRunnable at the given index, or null if not found
     */
    public static BaseRunnable getRunnable(int index) {
        return currentRunnables.get(index);
    }

    /**
     * Initializes the TaskManager by starting the async utilities, enabling ticking,
     * and setting up the menu updater.
     */
    public static void init() {
        try {
            AsyncUtils.init();
        } catch (Throwable t) {
            BukkitOfUtils.getInstance().logWarning("Failed to initialize AsyncUtils.", t);
        }

        enableTicking();

        setupMenuUpdater();

        BukkitOfUtils.getInstance().logInfo("&cTaskManager &fis now initialized!");
    }

    /**
     * An atomic flag indicating whether the ticking mechanism is enabled.
     *
     * @param tickingEnabled the atomic boolean to set
     * @return the atomic boolean indicating ticking state
     */
    @Getter @Setter
    private static AtomicBoolean tickingEnabled = new AtomicBoolean(false);

    /**
     * Updates the ticking enabled state to the specified value.
     *
     * @param bool true to enable ticking, false to disable
     */
    public static void updateTickingEnabled(boolean bool) {
        tickingEnabled.set(bool);
    }

    /**
     * Enables the ticking mechanism for all registered runnables.
     */
    public static void enableTicking() {
        updateTickingEnabled(true);
    }

    /**
     * Disables the ticking mechanism for all registered runnables.
     */
    public static void disableTicking() {
        updateTickingEnabled(false);
    }

    /**
     * Checks whether ticking is currently enabled.
     *
     * @return true if ticking is enabled
     */
    public static boolean isTickingEnabled() {
        return tickingEnabled.get();
    }

    /**
     * Stops the TaskManager by disabling ticking, cancelling all runnables,
     * and removing all queued async tasks.
     */
    public static void stop() {
        disableTicking();

        if (getTaskMenuUpdater() != null) getTaskMenuUpdater().cancel();

        currentRunnables.forEach((index, runnable) -> runnable.cancel());
        AsyncUtils.getQueuedTasks().forEach(AsyncTask::remove);

        BukkitOfUtils.getInstance().logInfo("&cTaskManager &fis now stopped!");
    }

    /**
     * Checks whether the TaskManager is able to run tasks (plugin enabled and ticking active).
     *
     * @return true if tasks can be executed
     */
    public static boolean isAbleToRun() {
        return BukkitOfUtils.getInstance().isEnabled() && isTickingEnabled();
    }

    /**
     * Gets the universal task scheduler from the BetterPlugin instance.
     *
     * @return the task scheduler
     */
    public static TaskScheduler getScheduler() {
        return BetterPlugin.getScheduler();
    }

    /**
     * Schedules a runnable to execute immediately.
     *
     * @param runnable the runnable to schedule
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(Runnable runnable) {
        return new CompletableTask(new InjectedRunnable(runnable));
    }

    /**
     * Schedules a runnable to execute after a delay.
     *
     * @param runnable the runnable to schedule
     * @param delay    the delay in ticks before execution
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(Runnable runnable, long delay) {
        return new CompletableTask(new InjectedRunnable(runnable), delay);
    }

    /**
     * Schedules a runnable to execute repeatedly after a delay.
     *
     * @param runnable the runnable to schedule
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(Runnable runnable, long delay, long period) {
        return new CompletableTask(new InjectedRunnable(runnable), delay, period);
    }

    /**
     * Schedules a runnable bound to an entity to execute immediately.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to schedule
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(Entity entity, Runnable runnable) {
        return new CompletableTask(entity, new InjectedRunnable(runnable));
    }

    /**
     * Schedules a runnable bound to an entity to execute after a delay.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to schedule
     * @param delay    the delay in ticks before execution
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(Entity entity, Runnable runnable, long delay) {
        return new CompletableTask(entity, new InjectedRunnable(runnable), delay);
    }

    /**
     * Schedules a runnable bound to an entity to execute repeatedly.
     *
     * @param entity   the entity context for the task
     * @param runnable the runnable to schedule
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(Entity entity, Runnable runnable, long delay, long period) {
        return new CompletableTask(entity, new InjectedRunnable(runnable), delay, period);
    }

    /**
     * Schedules a runnable bound to a region to execute immediately.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to schedule
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(World world, int x, int z, Runnable runnable) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable));
    }

    /**
     * Schedules a runnable bound to a chunk to execute immediately.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to schedule
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(Chunk chunk, Runnable runnable) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable));
    }

    /**
     * Schedules a runnable bound to a region to execute after a delay.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to schedule
     * @param delay    the delay in ticks before execution
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(World world, int x, int z, Runnable runnable, long delay) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable), delay);
    }

    /**
     * Schedules a runnable bound to a chunk to execute after a delay.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to schedule
     * @param delay    the delay in ticks before execution
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(Chunk chunk, Runnable runnable, long delay) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable), delay);
    }

    /**
     * Schedules a runnable bound to a region to execute repeatedly.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to schedule
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(World world, int x, int z, Runnable runnable, long delay, long period) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable), delay, period);
    }

    /**
     * Schedules a runnable bound to a chunk to execute repeatedly.
     *
     * @param chunk    the chunk context for the task
     * @param runnable the runnable to schedule
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return a CompletableTask wrapping the scheduled execution
     */
    public static CompletableTask schedule(Chunk chunk, Runnable runnable, long delay, long period) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable), delay, period);
    }

    /**
     * Schedules a teleportation of an entity to the given location.
     *
     * @param entityToTeleport the entity to teleport
     * @param location         the target location
     * @return a CompletableTask wrapping the teleportation
     */
    public static CompletableTask schedule(Entity entityToTeleport, Location location) {
        return new CompletableTask(entityToTeleport, location);
    }

    /**
     * Runs a task immediately on the scheduler, if the manager is able to run.
     *
     * @param runnable the runnable to execute
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTask(Runnable runnable) {
        if (! isAbleToRun()) return null;

        return getScheduler().runTask(runnable);
    }

    /**
     * Runs a task after a delay on the scheduler, if the manager is able to run.
     *
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTaskLater(Runnable runnable, long delay) {
        if (! isAbleToRun()) return null;

        return getScheduler().runTaskLater(runnable, delay);
    }

    /**
     * Runs a repeating task on the scheduler, if the manager is able to run.
     *
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTaskTimer(Runnable runnable, long delay, long period) {
        if (! isAbleToRun()) return null;

        return getScheduler().runTaskTimer(runnable, delay, period);
    }

    /**
     * Runs a task bound to an entity immediately, if the manager is able to run.
     *
     * @param entity   the entity context
     * @param runnable the runnable to execute
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTask(Entity entity, Runnable runnable) {
        if (! isAbleToRun()) return null;

        return getScheduler().runTask(entity, runnable);
    }

    /**
     * Runs a task bound to an entity after a delay, if the manager is able to run.
     *
     * @param entity   the entity context
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTaskLater(Entity entity, Runnable runnable, long delay) {
        if (! isAbleToRun()) return null;

        return getScheduler().runTaskLater(entity, runnable, delay);
    }

    /**
     * Runs a repeating task bound to an entity, if the manager is able to run.
     *
     * @param entity   the entity context
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTaskTimer(Entity entity, Runnable runnable, long delay, long period) {
        if (! isAbleToRun()) return null;

        return getScheduler().runTaskTimer(entity, runnable, delay, period);
    }

    /**
     * Runs a task bound to a region immediately, if the manager is able to run.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to execute
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTask(World world, int x, int z, Runnable runnable) {
        if (! isAbleToRun()) return null;

        return getScheduler().runTask(world, x, z, runnable);
    }

    /**
     * Runs a task bound to a chunk immediately, if the manager is able to run.
     *
     * @param chunk    the chunk context
     * @param runnable the runnable to execute
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTask(Chunk chunk, Runnable runnable) {
        if (! isAbleToRun()) return null;

        return runTask(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable);
    }

    /**
     * Runs a task bound to a region after a delay, if the manager is able to run.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTaskLater(World world, int x, int z, Runnable runnable, long delay) {
        if (! isAbleToRun()) return null;

        return getScheduler().runTaskLater(world, x, z, runnable, delay);
    }

    /**
     * Runs a task bound to a chunk after a delay, if the manager is able to run.
     *
     * @param chunk    the chunk context
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before execution
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTaskLater(Chunk chunk, Runnable runnable, long delay) {
        if (! isAbleToRun()) return null;

        return runTaskLater(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay);
    }

    /**
     * Runs a repeating task bound to a region, if the manager is able to run.
     *
     * @param world    the world containing the region
     * @param x        the chunk X coordinate
     * @param z        the chunk Z coordinate
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTaskTimer(World world, int x, int z, Runnable runnable, long delay, long period) {
        if (! isAbleToRun()) return null;

        return getScheduler().runTaskTimer(world, x, z, runnable, delay, period);
    }

    /**
     * Runs a repeating task bound to a chunk, if the manager is able to run.
     *
     * @param chunk    the chunk context
     * @param runnable the runnable to execute
     * @param delay    the delay in ticks before the first execution
     * @param period   the period in ticks between subsequent executions
     * @return the scheduled task, or null if the manager cannot run
     */
    public static MyScheduledTask runTaskTimer(Chunk chunk, Runnable runnable, long delay, long period) {
        if (! isAbleToRun()) return null;

        return runTaskTimer(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay, period);
    }

    /**
     * Teleports an entity to the given location using the scheduler, with fallback methods.
     * Falls back to synchronous teleport and then async teleport if the scheduler fails.
     *
     * @param entityToTeleport the entity to teleport
     * @param location         the target location
     * @return the scheduled task, or a no-op task if fallback was used, or null if unable to run
     */
    public static MyScheduledTask teleport(Entity entityToTeleport, Location location) {
        if (! isAbleToRun()) return null;

        try {
            return getScheduler().teleport(entityToTeleport, location);
        } catch (Throwable e) {
            try {
                entityToTeleport.teleport(location);
            } catch (Throwable e2) {
                try {
                    VersionTool.teleportAsync(entityToTeleport, location);
                } catch (Throwable e3) {
                    BukkitOfUtils.getInstance().logWarning("Failed to teleport entity: " + entityToTeleport + " to location: " + location, e3);
                }
            }

            return getScheduler().runTask(() -> {});
        }
    }

    /**
     * Executes a callable, catching and logging any exceptions.
     *
     * @param callable the callable to execute
     */
    public static void doThis(Callable<?> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to do a callable task.");
            BukkitOfUtils.getInstance().logWarning(e);
        }
    }

    /**
     * Applies a consumer to the given object. A convenience method for inline usage.
     *
     * @param c        the object to consume
     * @param consumer the consumer to apply
     * @param <C>      the type of the object
     */
    public static <C> void use(C c, Consumer<C> consumer) {
        consumer.accept(c);
    }

    /**
     * Checks whether the current thread is synchronized with the appropriate context.
     * On Folia servers, checks entity/region thread ownership; on standard servers,
     * checks whether this is the primary thread.
     *
     * @param obj an optional context object (Entity or Location) for thread checking, or null
     * @return true if the current thread is the correct sync thread for the given context
     */
    public static boolean isThreadSync(Object obj) {
        Entity e = null;
        Location l = null;
        if (obj != null) {
            if (obj instanceof Entity) {
                e = (Entity) obj;
            } else if (obj instanceof Location) {
                l = (Location) obj;
            }
        }

        final Entity finalE = e;
        final Location finalL = l;

        return ClassHelper.ifFoliaOrElse(
                () -> {
                    TaskScheduler scheduler = getScheduler();
                    if (scheduler == null) {
                        return Bukkit.isPrimaryThread();
                    }
                    try {
                        FoliaScheduler foliaScheduler = (FoliaScheduler) scheduler;
                        if (finalE != null) {
                            return foliaScheduler.isEntityThread(finalE);
                        } else if (finalL != null) {
                            return foliaScheduler.isRegionThread(finalL);
                        }

                        return foliaScheduler.isTickThread() || foliaScheduler.isGlobalThread();
                    } catch (Throwable t) {
                        BukkitOfUtils.getInstance().logWarning("Failed to cast scheduler to FoliaScheduler.", t);
                        if (finalE != null) {
                            return scheduler.isEntityThread(finalE);
                        } else if (finalL != null) {
                            return scheduler.isRegionThread(finalL);
                        }

                        return scheduler.isTickThread() || scheduler.isGlobalThread();
                    }
                },
                Bukkit::isPrimaryThread
        );
    }

    /**
     * Checks whether the current thread is the primary/tick thread with no specific context.
     *
     * @return true if the current thread is synchronized
     */
    public static boolean isThreadSync() {
        return isThreadSync(null);
    }

    /**
     * Builds a formatted string with detailed information about the given runnable.
     *
     * @param runnable the runnable to describe
     * @return a formatted multi-line string with task details
     */
    public static String buildTaskInfo(BaseRunnable runnable) {
        StringBuilder sb = new StringBuilder();

        sb
                .append("  &2- &bTask &e(&a").append(runnable.getIndex()).append(" &9- &3").append(runnable.getStartedAt()).append("&e)&7:").append("\n")
                .append("    &dPeriod&7: &a").append(runnable.getPeriod()).append(" &9| &dTicks Lived&7: &a").append(runnable.getTicksLived()).append("\n")
                .append("    &dCurrent Tick Count&7: &a").append(runnable.getCurrentTickCount()).append(" &9| &dPaused&7? &a").append(runnable.isPaused() ? "&aYes" : "&cNo").append("\n")
                .append("    &dCancelled&7? &a").append(runnable.isCancelled() ? "&aYes" : "&cNo")
                ;

        return sb.toString();
    }

    /**
     * Builds a formatted string listing all currently registered tasks and their details.
     *
     * @return a formatted multi-line string of all current tasks
     */
    public static String listTasks() {
        StringBuilder sb = new StringBuilder("&cCurrent Tasks &e(&a").append(currentRunnables.size()).append("&e)&7:\n");

        for (BaseRunnable runnable : currentRunnables.values()) {
            sb.append(buildTaskInfo(runnable)).append("\n");
        }

        while (sb.toString().endsWith("\n")) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * Gets a set of all current task indices as strings.
     *
     * @return a sorted set of task index strings
     */
    public static ConcurrentSkipListSet<String> getTaskIdsAsStrings() {
        ConcurrentSkipListSet<String> taskIds = new ConcurrentSkipListSet<>();
        currentRunnables.forEach((index, runnable) -> taskIds.add(String.valueOf(index)));
        return taskIds;
    }

    /**
     * Creates an ItemStack representing the task at the given index for use in a GUI menu.
     *
     * @param index the index of the task
     * @return an ItemStack with the task's details in its lore
     */
    public static ItemStack getTaskItem(int index) {
        Material material = Material.COAL;

        String name = "&cTask &f(&dIndex&7: &b" + index + "&f)";

        BaseRunnable runnable = getRunnable(index);
        if (runnable == null) {
            return ItemUtils.make(material, name, "&cTask not found.");
        }

        String className = runnable.getClass().getSimpleName();
        name = "&c" + className + " &f(&dIndex&7: &b" + index + "&f)";

        List<String> lore = new ArrayList<>();
        lore.add("&dPeriod&7: &a" + runnable.getPeriod());
        lore.add("&dTicks Lived&7: &a" + runnable.getTicksLived());
        lore.add("&dCurrent Tick Count&7: &a" + runnable.getCurrentTickCount());
        lore.add("&dPaused&7? &a" + (runnable.isPaused() ? "&aYes" : "&cNo"));
        lore.add("&dCancelled&7? &a" + (runnable.isCancelled() ? "&aYes" : "&cNo"));

        return ItemUtils.make(material, name, lore);
    }

    /**
     * Creates a map of ItemStacks representing all currently registered tasks.
     *
     * @return a sorted map of task index to task ItemStack
     */
    public static ConcurrentSkipListMap<Integer, ItemStack> getTaskItems() {
        ConcurrentSkipListMap<Integer, ItemStack> taskItems = new ConcurrentSkipListMap<>();
        currentRunnables.forEach((index, runnable) -> taskItems.put(index, getTaskItem(index)));
        return taskItems;
    }

    /**
     * Creates an ItemStack representing an async task at the given index for use in a GUI menu.
     *
     * @param index the index of the async task
     * @return an ItemStack with the async task's details in its lore
     */
    public static ItemStack getAsyncItem(long index) {
        Material material = Material.DIAMOND;

        String name = "&cTask &f(&dIndex&7: &b" + index + "&f)";

        Optional<AsyncTask> optional = AsyncUtils.getTask(index);
        if (optional.isEmpty()) {
            return ItemUtils.make(material, name, "&cTask not found.");
        }
        AsyncTask asyncTask = optional.get();

        String className = asyncTask.getClass().getSimpleName();
        name = "&c" + className + " &f(&dIndex&7: &b" + index + "&f)";

        List<String> lore = new ArrayList<>();
        lore.add("&dPeriod&7: &a" + asyncTask.getPeriod());
        lore.add("&dTicks Lived&7: &a" + asyncTask.getTicksLived());
        lore.add("&dCurrent Delay&7: &a" + asyncTask.getCurrentDelay());
        lore.add("&dNeeded Ticks&7: &a" + asyncTask.getNeededTicks());
        lore.add("&dCompleted&7? &a" + (asyncTask.isCompleted() ? "&aYes" : "&cNo"));
        lore.add("&dRepeatable&7? &a" + (asyncTask.isRepeatable() ? "&aYes" : "&cNo"));

        return ItemUtils.make(material, name, lore);
    }

    /**
     * Creates a map of ItemStacks representing all queued async tasks.
     *
     * @return a sorted map of async task index to task ItemStack
     */
    public static ConcurrentSkipListMap<Integer, ItemStack> getAsyncItems() {
        ConcurrentSkipListMap<Integer, ItemStack> taskItems = new ConcurrentSkipListMap<>();
        AsyncUtils.getQueuedTasks().forEach((task) -> taskItems.put((int) task.getId(), getAsyncItem(task.getId())));
        return taskItems;
    }

    /**
     * The runnable responsible for periodically updating the task menu GUI.
     *
     * @param taskMenuUpdater the task menu updater runnable to set
     * @return the task menu updater runnable
     */
    @Getter @Setter
    private static BaseRunnable taskMenuUpdater;

    /**
     * Sets up or restarts the task menu updater runnable.
     * Cancels any existing updater before creating a new one.
     */
    public static void setupMenuUpdater() {
        try {
            if (taskMenuUpdater != null) {
                taskMenuUpdater.cancel();
            }

            taskMenuUpdater = new TaskMenuUpdater();
        } catch (Throwable t) {
            BukkitOfUtils.getInstance().logWarning("Failed to setup task menu updater.", t);
        }
    }
}
