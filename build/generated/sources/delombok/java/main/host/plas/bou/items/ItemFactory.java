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
import org.bukkit.inventory.ItemStack;

public class ItemFactory {
    private static ConcurrentSkipListMap<RetrievableKey, RetrievableItem> retreivableItems = new ConcurrentSkipListMap<>();

    public static void registerFactory(RetrievableKey key, RetrievableItem item) {
        retreivableItems.put(key, item);
    }

    public static void registerFactory(BukkitOfUtils plugin, String key, RetrievableItem item) {
        retreivableItems.put(RetrievableKey.of(plugin.getIdentifier(), key), item);
    }

    public static void unregisterFactory(RetrievableKey key) {
        retreivableItems.remove(key);
    }

    public static void unregisterFactory(BukkitOfUtils plugin, String key) {
        retreivableItems.remove(RetrievableKey.of(plugin.getIdentifier(), key));
    }

    public static Optional<RetrievableItem> getFactory(RetrievableKey key) {
        return Optional.ofNullable((RetrievableItem) retreivableItems.get(key));
    }

    public static Optional<ItemStack> getItem(RetrievableKey key) {
        return getFactory(key).map(Supplier::get);
    }

    public static ConcurrentSkipListSet<BetterPlugin> getPluginsWithItems() {
        ConcurrentSkipListSet<BetterPlugin> plugins = new ConcurrentSkipListSet<>();
        for (RetrievableKey key : retreivableItems.keySet()) {
            Optional<BetterPlugin> var10000 = PluginUtils.getPlugin(key.getPlugin());
            Objects.requireNonNull(plugins);
            var10000.ifPresent(plugins::add);
        }
        return plugins;
    }

    public static ConcurrentSkipListSet<String> getPluginsWithItemsNames() {
        return getPluginsWithItems().stream().map(BetterPlugin::getIdentifier).collect(ConcurrentSkipListSet::new, ConcurrentSkipListSet::add, AbstractCollection::addAll);
    }

    public static ConcurrentSkipListMap<String, RetrievableItem> getItemsForPlugin(String pluginName) {
        ConcurrentSkipListMap<String, RetrievableItem> items = new ConcurrentSkipListMap<>();
        for (RetrievableKey key : retreivableItems.keySet()) {
            if (key.getPlugin().equals(pluginName)) {
                items.put(key.getKey(), (RetrievableItem) retreivableItems.get(key));
            }
        }
        return items;
    }

    public static ConcurrentSkipListSet<String> getItemKeysForPlugin(String pluginName) {
        return new ConcurrentSkipListSet<>(getItemsForPlugin(pluginName).keySet());
    }

    public static ConcurrentSkipListMap<RetrievableKey, RetrievableItem> getRetreivableItems() {
        return ItemFactory.retreivableItems;
    }

    public static void setRetreivableItems(final ConcurrentSkipListMap<RetrievableKey, RetrievableItem> retreivableItems) {
        ItemFactory.retreivableItems = retreivableItems;
    }
}
