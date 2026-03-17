package host.plas.bou.bstats;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.PluginUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * Manages bStats metrics integration for BukkitOfUtils.
 * Handles initialization and shutdown of the Metrics instance
 * and registers custom charts for tracking plugin usage data.
 */
public class BStats {
    /**
     * The bStats plugin ID for BukkitOfUtils.
     * @param pluginId the bStats plugin ID to set
     * @return the bStats plugin ID
     * @see <a href="https://bstats.org/plugin/bukkit/BukkitOfUtils/26228">bStats page</a>
     */
    @Getter @Setter
    private static int pluginId = 26228; // https://bstats.org/plugin/bukkit/BukkitOfUtils/26228

    /**
     * The active bStats Metrics instance, or {@code null} if not yet initialized.
     * @param metrics the Metrics instance to set
     * @return the active Metrics instance
     */
    @Getter @Setter
    private static Metrics metrics;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private BStats() {
        // utility class
    }

    /**
     * Initializes the bStats Metrics instance and registers custom charts.
     * Should be called during plugin enable.
     */
    public static void onEnable() {
        metrics = new Metrics(BukkitOfUtils.getInstance(), pluginId);

        metrics.addCustomChart(new Metrics.SingleLineChart("bou_plugins", PluginUtils::getLoadedBOUPluginCount));
    }

    /**
     * Shuts down the bStats Metrics instance if it is active.
     * Should be called during plugin disable.
     */
    public static void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }
    }
}
