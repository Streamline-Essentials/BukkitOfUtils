package host.plas.bou.libs.universalScheduler;

import host.plas.bou.libs.universalScheduler.bukkitScheduler.BukkitScheduler;
import host.plas.bou.libs.universalScheduler.foliaScheduler.FoliaScheduler;
import host.plas.bou.libs.universalScheduler.paperScheduler.PaperScheduler;
import host.plas.bou.libs.universalScheduler.scheduling.schedulers.TaskScheduler;
import host.plas.bou.libs.universalScheduler.utils.JavaUtil;
import org.bukkit.plugin.Plugin;

/**
 * Entry point that picks the right {@link TaskScheduler} for the running server.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level; the upstream
 * artifact ships class file version 65.0 (Java 21) and cannot load on the Java 11
 * runtimes BOU supports.</p>
 *
 * <p>The two flags below are probed by name rather than by referencing the classes, and
 * {@link #getScheduler(Plugin)} only mentions {@link FoliaScheduler} or
 * {@link PaperScheduler} inside a branch that has already confirmed the corresponding API
 * exists. That ordering is what keeps a legacy server from ever resolving a Folia type.</p>
 */
public class UniversalScheduler {
    /** True when running on Folia, where the server is split into ticking regions. */
    public static final boolean isFolia = JavaUtil.classExists("io.papermc.paper.threadedregions.RegionizedServer");

    /** True when Folia's scheduler API is present, which modern Paper also ships. */
    public static final boolean isExpandedSchedulingAvailable =
            JavaUtil.classExists("io.papermc.paper.threadedregions.scheduler.ScheduledTask");

    /**
     * Chooses a scheduler implementation appropriate to the current platform.
     *
     * @param plugin the plugin that will own scheduled tasks
     * @return a Folia, Paper or Bukkit scheduler
     */
    public static TaskScheduler getScheduler(Plugin plugin) {
        if (isFolia) return new FoliaScheduler(plugin);
        if (isExpandedSchedulingAvailable) return new PaperScheduler(plugin);

        return new BukkitScheduler(plugin);
    }
}
