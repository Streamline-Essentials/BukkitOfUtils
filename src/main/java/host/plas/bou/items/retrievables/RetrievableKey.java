package host.plas.bou.items.retrievables;

import gg.drak.thebase.objects.Identified;
import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * A composite key used to uniquely identify a retrievable item by plugin name and item key.
 * The identifier format is "plugin:key".
 */
@Getter @Setter
public class RetrievableKey implements Identified {
    /**
     * The name of the plugin that owns this retrievable item.
     *
     * @param plugin the plugin name to set
     * @return the plugin name
     */
    private String plugin;

    /**
     * The unique key for the item within the plugin.
     *
     * @param key the item key to set
     * @return the item key
     */
    private String key;

    /**
     * Constructs a new RetrievableKey with the given plugin name and item key.
     *
     * @param plugin the name of the plugin that owns this item
     * @param key the unique key for the item within the plugin
     */
    public RetrievableKey(String plugin, String key) {
        this.plugin = plugin;
        this.key = key;
    }

    /**
     * Returns the composite identifier in the format "plugin:key".
     *
     * @return the combined identifier string
     */
    @Override
    public String getIdentifier() {
        return plugin + ":" + key;
    }

    /**
     * Creates a new RetrievableKey from a plugin name string and item key.
     *
     * @param plugin the name of the plugin
     * @param key the item key
     * @return a new RetrievableKey instance
     */
    public static RetrievableKey of(String plugin, String key) {
        return new RetrievableKey(plugin, key);
    }

    /**
     * Creates a new RetrievableKey from a BetterPlugin instance and item key.
     *
     * @param plugin the BetterPlugin instance whose identifier will be used
     * @param key the item key
     * @return a new RetrievableKey instance
     */
    public static RetrievableKey of(BetterPlugin plugin, String key) {
        return new RetrievableKey(plugin.getIdentifier(), key);
    }
}
