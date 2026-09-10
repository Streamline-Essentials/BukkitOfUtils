package host.plas.bou.libs.universalScheduler.bukkitScheduler;

import host.plas.bou.libs.universalScheduler.scheduling.schedulers.TaskScheduler;
import host.plas.bou.libs.universalScheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * {@link TaskScheduler} backed by the classic single-threaded Bukkit scheduler.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level. This is the
 * implementation used on every non-Folia server, including legacy 1.8.x.</p>
 */
public class BukkitScheduler implements TaskScheduler {
    final Plugin plugin;

    /**
     * @param plugin the plugin that will own scheduled tasks
     */
    public BukkitScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isGlobalThread() {
        return Bukkit.getServer().isPrimaryThread();
    }

    @Override
    public boolean isEntityThread(Entity entity) {
        return Bukkit.getServer().isPrimaryThread();
    }

    @Override
    public boolean isRegionThread(Location location) {
        return Bukkit.getServer().isPrimaryThread();
    }

    @Override
    public MyScheduledTask runTask(Runnable runnable) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    @Override
    public MyScheduledTask runTaskLater(Runnable runnable, long delay) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
    }

    @Override
    public MyScheduledTask runTaskTimer(Runnable runnable, long delay, long period) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period), true);
    }

    @Override
    public MyScheduledTask runTaskAsynchronously(Runnable runnable) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    @Override
    public MyScheduledTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    @Override
    public MyScheduledTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period), true);
    }

    @Override
    public MyScheduledTask runTask(Plugin plugin, Runnable runnable) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    @Override
    public MyScheduledTask runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
    }

    @Override
    public MyScheduledTask runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period), true);
    }

    @Override
    public MyScheduledTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    @Override
    public MyScheduledTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    @Override
    public MyScheduledTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long period) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period), true);
    }

    @Override
    public void execute(Runnable runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable);
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    @Override
    public MyScheduledTask teleport(Entity entity, Location location) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTask(plugin, () -> entity.teleport(location)));
    }
}
