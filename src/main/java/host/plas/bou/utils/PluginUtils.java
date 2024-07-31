package host.plas.bou.utils;

import host.plas.bou.BetterPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class PluginUtils {
    @Getter @Setter
    private static ConcurrentSkipListSet<BetterPlugin> loadedBOUPlugins = new ConcurrentSkipListSet<>();

    public static boolean registerPlugin(BetterPlugin plugin) {
        if (isPluginRegistered(plugin.getIdentifier())) {
            unregisterPlugin(plugin);
        }

        return getLoadedBOUPlugins().add(plugin);
    }

    public static boolean unregisterPlugin(BetterPlugin plugin) {
        if (! isPluginRegistered(plugin.getIdentifier())) return false;

        return getLoadedBOUPlugins().removeIf(loadedPlugin -> loadedPlugin.getIdentifier().equals(plugin.getIdentifier()));
    }

    public static Optional<BetterPlugin> getPlugin(String identifier) {
        return getLoadedBOUPlugins().stream().filter(plugin -> plugin.getName().equals(identifier)).findFirst();
    }

    public static boolean isPluginRegistered(String identifier) {
        return getPlugin(identifier).isPresent();
    }

    public static NamespacedKey getPluginKey(BetterPlugin plugin, String key) {
        return getPluginKey((JavaPlugin) plugin, key);
    }

    public static NamespacedKey getPluginKey(JavaPlugin plugin, String key) {
        return new NamespacedKey(plugin, key);
    }
}
