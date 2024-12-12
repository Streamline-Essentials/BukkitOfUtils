package host.plas.bou.scheduling;

import com.github.Anon8281.universalScheduler.foliaScheduler.FoliaScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.BetterPlugin;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.ClassHelper;
import host.plas.bou.utils.VersionTool;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

public class TaskManager {
    @Getter @Setter
    private static ConcurrentSkipListMap<Integer, BaseRunnable> currentRunnables = new ConcurrentSkipListMap<>();

    @Getter @Setter
    private static Timer timer;

    public static void start(BaseRunnable runnable) {
        currentRunnables.put(runnable.getIndex(), runnable);
    }

    public static void cancel(BaseRunnable runnable) {
        cancel(runnable.getIndex());
    }

    public static int getNextIndex() {
        return currentRunnables.size();
    }

    public static void tick() {
        for (BaseRunnable runnable : currentRunnables.values()) {
            try {
                runnable.tick();
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logDebug("Error while ticking runnable: " + runnable, e);
            }
        }
    }

    public static boolean isCancelled(int index) {
        return ! currentRunnables.containsKey(index);
    }

    public static void cancel(int index) {
        currentRunnables.remove(index);
    }

    public static BaseRunnable getRunnable(int index) {
        return currentRunnables.get(index);
    }

    public static void init() {
        int tickingFrequency = BaseManager.getBaseConfig().getTickingFrequency();
        timer = new Timer(tickingFrequency, e -> {
            if (getTimer().getDelay() != BaseManager.getBaseConfig().getTickingFrequency()) {
                getTimer().setDelay(BaseManager.getBaseConfig().getTickingFrequency());
            }

            tick();
        });
        timer.start();

        BukkitOfUtils.getInstance().logInfo("&cTaskManager &fis now initialized!");
    }

    public static void stop() {
        timer.stop();
        currentRunnables.forEach((index, runnable) -> runnable.cancel());

        BukkitOfUtils.getInstance().logInfo("&cTaskManager &fis now stopped!");
    }

    public static TaskScheduler getScheduler() {
        return BetterPlugin.getScheduler();
    }

    public static CompletableTask schedule(Runnable runnable) {
        return new CompletableTask(new InjectedRunnable(runnable));
    }

    public static CompletableTask schedule(Runnable runnable, long delay) {
        return new CompletableTask(new InjectedRunnable(runnable), delay);
    }

    public static CompletableTask schedule(Runnable runnable, long delay, long period) {
        return new CompletableTask(new InjectedRunnable(runnable), delay, period);
    }

    public static CompletableTask schedule(Entity entity, Runnable runnable) {
        return new CompletableTask(entity, new InjectedRunnable(runnable));
    }

    public static CompletableTask schedule(Entity entity, Runnable runnable, long delay) {
        return new CompletableTask(entity, new InjectedRunnable(runnable), delay);
    }

    public static CompletableTask schedule(Entity entity, Runnable runnable, long delay, long period) {
        return new CompletableTask(entity, new InjectedRunnable(runnable), delay, period);
    }

    public static CompletableTask schedule(World world, int x, int z, Runnable runnable) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable));
    }

    public static CompletableTask schedule(Chunk chunk, Runnable runnable) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable));
    }

    public static CompletableTask schedule(World world, int x, int z, Runnable runnable, long delay) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable), delay);
    }

    public static CompletableTask schedule(Chunk chunk, Runnable runnable, long delay) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable), delay);
    }

    public static CompletableTask schedule(World world, int x, int z, Runnable runnable, long delay, long period) {
        return new CompletableTask(world, x, z, new InjectedRunnable(runnable), delay, period);
    }

    public static CompletableTask schedule(Chunk chunk, Runnable runnable, long delay, long period) {
        return new CompletableTask(chunk, new InjectedRunnable(runnable), delay, period);
    }

    public static CompletableTask schedule(Entity entityToTeleport, Location location) {
        return new CompletableTask(entityToTeleport, location);
    }

    public static MyScheduledTask runTask(Runnable runnable) {
        return getScheduler().runTask(runnable);
    }

    public static MyScheduledTask runTaskLater(Runnable runnable, long delay) {
        return getScheduler().runTaskLater(runnable, delay);
    }

    public static MyScheduledTask runTaskTimer(Runnable runnable, long delay, long period) {
        return getScheduler().runTaskTimer(runnable, delay, period);
    }

    public static MyScheduledTask runTask(Entity entity, Runnable runnable) {
        return getScheduler().runTask(entity, runnable);
    }

    public static MyScheduledTask runTaskLater(Entity entity, Runnable runnable, long delay) {
        return getScheduler().runTaskLater(entity, runnable, delay);
    }

    public static MyScheduledTask runTaskTimer(Entity entity, Runnable runnable, long delay, long period) {
        return getScheduler().runTaskTimer(entity, runnable, delay, period);
    }

    public static MyScheduledTask runTask(World world, int x, int z, Runnable runnable) {
        return getScheduler().runTask(world, x, z, runnable);
    }

    public static MyScheduledTask runTask(Chunk chunk, Runnable runnable) {
        return runTask(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable);
    }

    public static MyScheduledTask runTaskLater(World world, int x, int z, Runnable runnable, long delay) {
        return getScheduler().runTaskLater(world, x, z, runnable, delay);
    }

    public static MyScheduledTask runTaskLater(Chunk chunk, Runnable runnable, long delay) {
        return runTaskLater(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay);
    }

    public static MyScheduledTask runTaskTimer(World world, int x, int z, Runnable runnable, long delay, long period) {
        return getScheduler().runTaskTimer(world, x, z, runnable, delay, period);
    }

    public static MyScheduledTask runTaskTimer(Chunk chunk, Runnable runnable, long delay, long period) {
        return runTaskTimer(chunk.getWorld(), chunk.getX(), chunk.getZ(), runnable, delay, period);
    }

    public static MyScheduledTask teleport(Entity entityToTeleport, Location location) {
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

    public static void doThis(Callable<?> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to do a callable task.");
            BukkitOfUtils.getInstance().logWarning(e);
        }
    }

    public static <C> void use(C c, Consumer<C> consumer) {
        consumer.accept(c);
    }

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

    public static boolean isThreadSync() {
        return isThreadSync(null);
    }

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

    public static ConcurrentSkipListSet<String> getTaskIdsAsStrings() {
        ConcurrentSkipListSet<String> taskIds = new ConcurrentSkipListSet<>();
        currentRunnables.forEach((index, runnable) -> taskIds.add(String.valueOf(index)));
        return taskIds;
    }

    public static ItemStack getTaskItem(int index) {
        Material material = Material.COAL;

        String name = "&cTask &f#&b" + index;

        List<String> lore = new ArrayList<>();
        lore.add("&dPeriod&7: &a" + getRunnable(index).getPeriod());
        lore.add("&dTicks Lived&7: &a" + getRunnable(index).getTicksLived());
        lore.add("&dCurrent Tick Count&7: &a" + getRunnable(index).getCurrentTickCount());
        lore.add("&dPaused&7? &a" + (getRunnable(index).isPaused() ? "&aYes" : "&cNo"));
        lore.add("&dCancelled&7? &a" + (getRunnable(index).isCancelled() ? "&aYes" : "&cNo"));

        return ItemUtils.make(material, name, lore);
    }

    public static ConcurrentSkipListMap<Integer, ItemStack> getTaskItems() {
        ConcurrentSkipListMap<Integer, ItemStack> taskItems = new ConcurrentSkipListMap<>();
        currentRunnables.forEach((index, runnable) -> taskItems.put(index, getTaskItem(index)));
        return taskItems;
    }
}
