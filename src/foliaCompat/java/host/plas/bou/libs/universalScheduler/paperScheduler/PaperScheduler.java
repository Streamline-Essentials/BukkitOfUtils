package host.plas.bou.libs.universalScheduler.paperScheduler;

import host.plas.bou.libs.universalScheduler.foliaScheduler.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Scheduler for modern Paper servers that expose Folia's scheduling API without being
 * regionised.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level.</p>
 *
 * <p>Extends {@link FoliaScheduler}, so it inherits that class's Folia type references and
 * is only safe to load when the expanded scheduling API is present.</p>
 */
public class PaperScheduler extends FoliaScheduler {
    /**
     * @param plugin the plugin that will own scheduled tasks
     */
    public PaperScheduler(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isGlobalThread() {
        // Paper is not regionised, so isGlobalTickThread() does not apply here.
        return Bukkit.getServer().isPrimaryThread();
    }
}
