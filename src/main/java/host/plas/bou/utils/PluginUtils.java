package host.plas.bou.utils;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.helpful.HelpfulPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.AbstractCollection;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Utility class for managing registered BetterPlugin instances,
 * including registration, lookup, and NamespacedKey creation.
 */
public class PluginUtils {
    /**
     * The set of all currently loaded BOU plugin instances.
     *
     * @param loadedBOUPlugins the set of loaded plugins to set
     * @return the set of loaded BOU plugins
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<BetterPlugin> loadedBOUPlugins = new ConcurrentSkipListSet<>();

    /** Private constructor to prevent instantiation of this utility class. */
    private PluginUtils() {}

    /**
     * Registers a plugin, replacing any previously registered plugin with the same identifier.
     *
     * @param plugin the plugin to register
     * @return true if the plugin was successfully added to the set
     */
    public static boolean registerPlugin(BetterPlugin plugin) {
        if (isPluginRegistered(plugin.getIdentifier())) {
            unregisterPlugin(plugin);
        }

        return getLoadedBOUPlugins().add(plugin);
    }

    /**
     * Unregisters a plugin by removing it from the loaded plugins set.
     *
     * @param plugin the plugin to unregister
     * @return true if a matching plugin was found and removed
     */
    public static boolean unregisterPlugin(BetterPlugin plugin) {
        if (! isPluginRegistered(plugin.getIdentifier())) return false;

        return getLoadedBOUPlugins().removeIf(loadedPlugin -> loadedPlugin.getIdentifier().equals(plugin.getIdentifier()));
    }

    /**
     * Retrieves a registered plugin by its identifier (name).
     *
     * @param identifier the plugin identifier to search for
     * @return an Optional containing the plugin if found, or empty otherwise
     */
    public static Optional<BetterPlugin> getPlugin(String identifier) {
        return getLoadedBOUPlugins().stream().filter(plugin -> plugin.getName().equals(identifier)).findFirst();
    }

    /**
     * Checks whether a plugin with the given identifier is currently registered.
     *
     * @param identifier the plugin identifier to check
     * @return true if a plugin with the given identifier is registered
     */
    public static boolean isPluginRegistered(String identifier) {
        return getPlugin(identifier).isPresent();
    }

    /**
     * Creates a NamespacedKey for the given BetterPlugin and key string.
     *
     * @param plugin the plugin to create the key for
     * @param key    the key string
     * @return a new NamespacedKey
     */
    public static NamespacedKey getPluginKey(BetterPlugin plugin, String key) {
        return getPluginKey((JavaPlugin) plugin, key);
    }

    /**
     * Creates a NamespacedKey for the given JavaPlugin and key string.
     *
     * @param plugin the plugin to create the key for
     * @param key    the key string
     * @return a new NamespacedKey
     */
    public static NamespacedKey getPluginKey(JavaPlugin plugin, String key) {
        return new NamespacedKey(plugin, key);
    }

    /**
     * Returns the number of currently loaded BOU plugins.
     *
     * @return the count of loaded plugins
     */
    public static int getLoadedBOUPluginCount() {
        return getLoadedBOUPlugins().size();
    }

    /**
     * Parses a plugin name into a HelpfulPlugin, handling special cases for BukkitOfUtils itself.
     *
     * @param name the plugin name to look up
     * @return an Optional containing the HelpfulPlugin if found, or empty otherwise
     */
    public static Optional<HelpfulPlugin> parseHelpfulPlugin(String name) {
        return !name.equalsIgnoreCase("bou") && !name.equalsIgnoreCase("bukkitofutils") ? getHelpfulPlugins().stream().filter((plugin) -> plugin.getIdentifier().equalsIgnoreCase(name)).findFirst() : Optional.of(BukkitOfUtils.getInstance());
    }

    /**
     * Returns all loaded plugins that implement the HelpfulPlugin interface.
     *
     * @return a sorted set of HelpfulPlugin instances
     */
    public static ConcurrentSkipListSet<HelpfulPlugin> getHelpfulPlugins() {
        return getLoadedBOUPlugins().stream().filter((plugin) -> plugin instanceof HelpfulPlugin).map((plugin) -> (HelpfulPlugin)plugin).collect(ConcurrentSkipListSet::new, ConcurrentSkipListSet::add, AbstractCollection::addAll);
    }
}
