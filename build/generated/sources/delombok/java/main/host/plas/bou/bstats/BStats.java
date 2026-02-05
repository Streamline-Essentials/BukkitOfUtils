package host.plas.bou.bstats;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.PluginUtils;

public class BStats {
    private static int pluginId = 26228; // https://bstats.org/plugin/bukkit/BukkitOfUtils/26228
    private static Metrics metrics;

    public static void onEnable() {
        metrics = new Metrics(BukkitOfUtils.getInstance(), pluginId);
        metrics.addCustomChart(new Metrics.SingleLineChart("bou_plugins", PluginUtils::getLoadedBOUPluginCount));
    }

    public static void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }
    }

    public static int getPluginId() {
        return BStats.pluginId;
    }

    public static void setPluginId(final int pluginId) {
        BStats.pluginId = pluginId;
    }

    public static Metrics getMetrics() {
        return BStats.metrics;
    }

    public static void setMetrics(final Metrics metrics) {
        BStats.metrics = metrics;
    }
}
