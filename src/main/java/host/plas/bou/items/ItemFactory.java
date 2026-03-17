package host.plas.bou.items;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.items.retrievables.RetrievableItem;
import host.plas.bou.items.retrievables.RetrievableKey;
import host.plas.bou.utils.PluginUtils;
import java.util.AbstractCollection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

/**
 * Factory for registering, unregistering, and retrieving custom items
 * associated with plugins via {@link RetrievableKey} identifiers.
 */
public class ItemFactory {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ItemFactory() {
        // utility class
    }

    /**
     * The map of registered retrievable items keyed by their retrievable keys.
     *
     * @param retreivableItems the map of retrievable items to set
     * @return the current map of retrievable items
     */
    @Getter @Setter
    private static ConcurrentSkipListMap<RetrievableKey, RetrievableItem> retreivableItems = new ConcurrentSkipListMap<>();

    /**
     * Registers a retrievable item with the given key.
     *
     * @param key  the retrievable key identifying the item
     * @param item the retrievable item supplier
     */
    public static void registerFactory(RetrievableKey key, RetrievableItem item) {
        retreivableItems.put(key, item);
    }

    /**
     * Registers a retrievable item for a specific plugin and key.
     *
     * @param plugin the plugin that owns the item
     * @param key    the string key identifying the item within the plugin
     * @param item   the retrievable item supplier
     */
    public static void registerFactory(BukkitOfUtils plugin, String key, RetrievableItem item) {
        retreivableItems.put(RetrievableKey.of(plugin.getIdentifier(), key), item);
    }

    /**
     * Unregisters a retrievable item by its key.
     *
     * @param key the retrievable key to remove
     */
    public static void unregisterFactory(RetrievableKey key) {
        retreivableItems.remove(key);
    }

    /**
     * Unregisters a retrievable item for a specific plugin and key.
     *
     * @param plugin the plugin that owns the item
     * @param key    the string key identifying the item within the plugin
     */
    public static void unregisterFactory(BukkitOfUtils plugin, String key) {
        retreivableItems.remove(RetrievableKey.of(plugin.getIdentifier(), key));
    }

    /**
     * Retrieves a registered retrievable item by its key.
     *
     * @param key the retrievable key to look up
     * @return an Optional containing the retrievable item, or empty if not registered
     */
    public static Optional<RetrievableItem> getFactory(RetrievableKey key) {
        return Optional.ofNullable((RetrievableItem)retreivableItems.get(key));
    }

    /**
     * Retrieves an item stack from a registered factory by its key.
     *
     * @param key the retrievable key to look up
     * @return an Optional containing the produced item stack, or empty if not registered
     */
    public static Optional<ItemStack> getItem(RetrievableKey key) {
        return getFactory(key).map(Supplier::get);
    }

    /**
     * Returns the set of all plugins that have registered items in the factory.
     *
     * @return a sorted set of plugins with registered items
     */
    public static ConcurrentSkipListSet<BetterPlugin> getPluginsWithItems() {
        ConcurrentSkipListSet<BetterPlugin> plugins = new ConcurrentSkipListSet<>();

        for(RetrievableKey key : retreivableItems.keySet()) {
            Optional<BetterPlugin> var10000 = PluginUtils.getPlugin(key.getPlugin());
            Objects.requireNonNull(plugins);
            var10000.ifPresent(plugins::add);
        }

        return plugins;
    }

    /**
     * Returns the names of all plugins that have registered items in the factory.
     *
     * @return a sorted set of plugin identifier strings
     */
    public static ConcurrentSkipListSet<String> getPluginsWithItemsNames() {
        return getPluginsWithItems().stream().map(BetterPlugin::getIdentifier).collect(ConcurrentSkipListSet::new, ConcurrentSkipListSet::add, AbstractCollection::addAll);
    }

    /**
     * Returns all registered items for a specific plugin.
     *
     * @param pluginName the plugin identifier to filter by
     * @return a sorted map of item keys to retrievable items for the given plugin
     */
    public static ConcurrentSkipListMap<String, RetrievableItem> getItemsForPlugin(String pluginName) {
        ConcurrentSkipListMap<String, RetrievableItem> items = new ConcurrentSkipListMap<>();

        for(RetrievableKey key : retreivableItems.keySet()) {
            if (key.getPlugin().equals(pluginName)) {
                items.put(key.getKey(), (RetrievableItem)retreivableItems.get(key));
            }
        }

        return items;
    }

    /**
     * Returns all registered item keys for a specific plugin.
     *
     * @param pluginName the plugin identifier to filter by
     * @return a sorted set of item key strings for the given plugin
     */
    public static ConcurrentSkipListSet<String> getItemKeysForPlugin(String pluginName) {
        return new ConcurrentSkipListSet<>(getItemsForPlugin(pluginName).keySet());
    }
}
