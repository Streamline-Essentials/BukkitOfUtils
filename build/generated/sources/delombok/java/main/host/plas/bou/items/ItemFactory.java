package host.plas.bou.items;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.items.retreivables.RetreivableItem;
import host.plas.bou.items.retreivables.RetrievableKey;
import host.plas.bou.utils.PluginUtils;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ItemFactory {
    private static ConcurrentSkipListMap<RetrievableKey, RetreivableItem> retreivableItems = new ConcurrentSkipListMap<>();

    public static void registerFactory(RetrievableKey key, RetreivableItem item) {
        retreivableItems.put(key, item);
    }

    public static void registerFactory(BukkitOfUtils plugin, String key, RetreivableItem item) {
        retreivableItems.put(RetrievableKey.of(plugin.getIdentifier(), key), item);
    }

    public static void unregisterFactory(RetrievableKey key) {
        retreivableItems.remove(key);
    }

    public static void unregisterFactory(BukkitOfUtils plugin, String key) {
        retreivableItems.remove(RetrievableKey.of(plugin.getIdentifier(), key));
    }

    public static Optional<RetreivableItem> getFactory(RetrievableKey key) {
        return Optional.ofNullable(retreivableItems.get(key));
    }

    public static Optional<ItemStack> getItem(RetrievableKey key) {
        return getFactory(key).map(RetreivableItem::get);
    }

    public static ConcurrentSkipListSet<BetterPlugin> getPluginsWithItems() {
        ConcurrentSkipListSet<BetterPlugin> plugins = new ConcurrentSkipListSet<>();
        for (RetrievableKey key : retreivableItems.keySet()) {
            PluginUtils.getPlugin(key.getPlugin()).ifPresent(plugins::add);
        }
        return plugins;
    }

    public static ConcurrentSkipListSet<String> getPluginsWithItemsNames() {
        return getPluginsWithItems().stream().map(p -> p.getIdentifier()).collect(ConcurrentSkipListSet::new, ConcurrentSkipListSet::add, ConcurrentSkipListSet::addAll);
    }

    public static ConcurrentSkipListMap<String, RetreivableItem> getItemsForPlugin(String pluginName) {
        ConcurrentSkipListMap<String, RetreivableItem> items = new ConcurrentSkipListMap<>();
        for (RetrievableKey key : retreivableItems.keySet()) {
            if (key.getPlugin().equals(pluginName)) {
                items.put(key.getKey(), retreivableItems.get(key));
            }
        }
        return items;
    }

    public static ConcurrentSkipListSet<String> getItemKeysForPlugin(String pluginName) {
        return new ConcurrentSkipListSet<>(getItemsForPlugin(pluginName).keySet());
    }

    public static ConcurrentSkipListMap<RetrievableKey, RetreivableItem> getRetreivableItems() {
        return ItemFactory.retreivableItems;
    }

    public static void setRetreivableItems(final ConcurrentSkipListMap<RetrievableKey, RetreivableItem> retreivableItems) {
        ItemFactory.retreivableItems = retreivableItems;
    }
}
