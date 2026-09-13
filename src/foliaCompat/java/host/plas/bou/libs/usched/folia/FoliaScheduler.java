package host.plas.bou.libs.usched.folia;

import host.plas.bou.libs.usched.scheduling.schedulers.TaskScheduler;
import host.plas.bou.libs.usched.scheduling.tasks.MyScheduledTask;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * {@link TaskScheduler} backed by Folia's regionised schedulers.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level.</p>
 *
 * <p>The fields below are Folia-only types, so loading this class on a non-Folia server
 * throws {@link NoClassDefFoundError}. That is why nothing outside this package may name
 * it in a signature and why
 * {@link host.plas.bou.libs.usched.UniversalScheduler} only constructs it
 * after detecting Folia.</p>
 */
public class FoliaScheduler implements TaskScheduler {
    final Plugin plugin;
    private final RegionScheduler regionScheduler;
    private final GlobalRegionScheduler globalRegionScheduler;
    private final AsyncScheduler asyncScheduler;

    /**
     * @param plugin the plugin that will own scheduled tasks
     */
    public FoliaScheduler(Plugin plugin) {
        this.regionScheduler = Bukkit.getServer().getRegionScheduler();
        this.globalRegionScheduler = Bukkit.getServer().getGlobalRegionScheduler();
        this.asyncScheduler = Bukkit.getServer().getAsyncScheduler();
        this.plugin = plugin;
    }

    @Override
    public boolean isGlobalThread() {
        return Bukkit.getServer().isGlobalTickThread();
    }

    @Override
    public boolean isTickThread() {
        return Bukkit.getServer().isPrimaryThread();
    }

    @Override
    public boolean isEntityThread(Entity entity) {
        return Bukkit.getServer().isOwnedByCurrentRegion(entity);
    }

    @Override
    public boolean isRegionThread(Location location) {
        return Bukkit.getServer().isOwnedByCurrentRegion(location);
    }

    /**
     * Folia rejects a period of zero or less, so clamp to a single tick.
     *
     * @param value the requested delay or period in ticks
     * @return the value, or 1 when it is not positive
     */
    private long getOneIfNotPositive(long value) {
        return value > 0 ? value : 1;
    }

    @Override
    public MyScheduledTask runTask(Runnable runnable) {
        return runTask(plugin, runnable);
    }

    @Override
    public MyScheduledTask runTaskLater(Runnable runnable, long delay) {
        return runTaskLater(plugin, runnable, delay);
    }

    @Override
    public MyScheduledTask runTaskTimer(Runnable runnable, long delay, long period) {
        return runTaskTimer(plugin, runnable, delay, period);
    }

    @Override
    public MyScheduledTask runTask(Plugin plugin, Runnable runnable) {
        return new FoliaScheduledTask(globalRegionScheduler.run(plugin, task -> runnable.run()));
    }

    @Override
    public MyScheduledTask runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        if (delay <= 0) return runTask(plugin, runnable);

        return new FoliaScheduledTask(globalRegionScheduler.runDelayed(plugin, task -> runnable.run(), delay));
    }

    @Override
    public MyScheduledTask runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
        return new FoliaScheduledTask(globalRegionScheduler.runAtFixedRate(plugin, task -> runnable.run(),
                getOneIfNotPositive(delay), getOneIfNotPositive(period)));
    }

    @Override
    public MyScheduledTask runTask(Location location, Runnable runnable) {
        return new FoliaScheduledTask(regionScheduler.run(plugin, location, task -> runnable.run()));
    }

    @Override
    public MyScheduledTask runTaskLater(Location location, Runnable runnable, long delay) {
        if (delay <= 0) return runTask(location, runnable);

        return new FoliaScheduledTask(regionScheduler.runDelayed(plugin, location, task -> runnable.run(), delay));
    }

    @Override
    public MyScheduledTask runTaskTimer(Location location, Runnable runnable, long delay, long period) {
        return new FoliaScheduledTask(regionScheduler.runAtFixedRate(plugin, location, task -> runnable.run(),
                getOneIfNotPositive(delay), getOneIfNotPositive(period)));
    }

    @Override
    public MyScheduledTask runTask(Entity entity, Runnable runnable) {
        return new FoliaScheduledTask(entity.getScheduler().run(plugin, task -> runnable.run(), null));
    }

    @Override
    public MyScheduledTask runTaskLater(Entity entity, Runnable runnable, long delay) {
        if (delay <= 0) return runTask(entity, runnable);

        return new FoliaScheduledTask(entity.getScheduler().runDelayed(plugin, task -> runnable.run(), null, delay));
    }

    @Override
    public MyScheduledTask runTaskTimer(Entity entity, Runnable runnable, long delay, long period) {
        return new FoliaScheduledTask(entity.getScheduler().runAtFixedRate(plugin, task -> runnable.run(), null,
                getOneIfNotPositive(delay), getOneIfNotPositive(period)));
    }

    @Override
    public MyScheduledTask runTask(World world, int x, int z, Runnable runnable) {
        return new FoliaScheduledTask(regionScheduler.run(plugin, world, x, z, task -> runnable.run()));
    }

    @Override
    public MyScheduledTask runTaskLater(World world, int x, int z, Runnable runnable, long delay) {
        if (delay <= 0) return runTask(world, x, z, runnable);

        return new FoliaScheduledTask(regionScheduler.runDelayed(plugin, world, x, z, task -> runnable.run(), delay));
    }

    @Override
    public MyScheduledTask runTaskTimer(World world, int x, int z, Runnable runnable, long delay, long period) {
        return new FoliaScheduledTask(regionScheduler.runAtFixedRate(plugin, world, x, z, task -> runnable.run(),
                getOneIfNotPositive(delay), getOneIfNotPositive(period)));
    }

    @Override
    public MyScheduledTask runTaskAsynchronously(Runnable runnable) {
        return runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public MyScheduledTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        return runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    @Override
    public MyScheduledTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        return runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    @Override
    public MyScheduledTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        return new FoliaScheduledTask(asyncScheduler.runNow(plugin, task -> runnable.run()));
    }

    @Override
    public MyScheduledTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
        // Folia's async scheduler takes wall-clock time, not ticks; upstream converts at
        // 50ms per tick and expresses the result in milliseconds.
        return new FoliaScheduledTask(asyncScheduler.runDelayed(plugin, task -> runnable.run(),
                getOneIfNotPositive(delay) * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public MyScheduledTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long period) {
        return new FoliaScheduledTask(asyncScheduler.runAtFixedRate(plugin, task -> runnable.run(),
                getOneIfNotPositive(delay) * 50, getOneIfNotPositive(period) * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public void execute(Runnable runnable) {
        globalRegionScheduler.execute(plugin, runnable);
    }

    @Override
    public void execute(Location location, Runnable runnable) {
        regionScheduler.execute(plugin, location, runnable);
    }

    @Override
    public void execute(Entity entity, Runnable runnable) {
        entity.getScheduler().execute(plugin, runnable, null, 1);
    }

    @Override
    public void cancelTasks() {
        globalRegionScheduler.cancelTasks(plugin);
        asyncScheduler.cancelTasks(plugin);
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        globalRegionScheduler.cancelTasks(plugin);
        asyncScheduler.cancelTasks(plugin);
    }

    @Override
    public MyScheduledTask teleport(Entity entity, Location location) {
        return new FoliaScheduledTask(entity.getScheduler().run(plugin,
                task -> entity.teleportAsync(location), null));
    }
}
