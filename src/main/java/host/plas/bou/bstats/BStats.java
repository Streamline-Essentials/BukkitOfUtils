package host.plas.bou.bstats;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.PluginUtils;
import lombok.Getter;
import lombok.Setter;

public class BStats {
    @Getter @Setter
    private static int pluginId = 26228; // https://bstats.org/plugin/bukkit/BukkitOfUtils/26228

    @Getter @Setter
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
}
