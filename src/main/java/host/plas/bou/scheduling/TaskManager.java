package host.plas.bou.scheduling;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.BetterPlugin;
import host.plas.bou.instances.BaseManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
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
        return getScheduler().teleport(entityToTeleport, location);
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
}
