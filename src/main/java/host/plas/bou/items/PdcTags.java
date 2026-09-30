package host.plas.bou.items;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The single place in BukkitOfUtils where the persistent data container API is touched.
 *
 * <p>Every reference to {@link NamespacedKey}, {@link PersistentDataType} and
 * {@code PersistentDataContainer} is confined to this class so that legacy servers never
 * load them. Guarding a PDC call with an {@code if} inside an otherwise-normal class is not
 * sufficient: an {@code if} guards <em>execution</em>, but the JVM may eagerly resolve classes
 * named in a method's descriptor or in its field references, so a method that merely
 * <em>returns</em> a {@code NamespacedKey} can fail before its first statement runs.</p>
 *
 * <p><strong>Callers must check
 * {@link host.plas.bou.compat.LegacySupport#hasPersistentDataContainer()} before invoking
 * anything here.</strong> No method in this class performs that check itself, because doing so
 * would require this class to be loaded in order to discover that it must not be loaded.</p>
 */
public final class PdcTags {
    private PdcTags() {
    }

    /**
     * Upper bound on {@link #KEYS}. Plugins normally use a small fixed set of tag names, but a caller
     * building key names from dynamic data must not be able to grow the cache without limit; past
     * this size, keys are still created, just not cached.
     */
    private static final int MAX_CACHED_KEYS = 4096;

    /**
     * Namespaced keys by owning plugin name and key name. Item tag reads run on hot paths
     * (inventory clicks, per-item checks), and constructing a key validates and lowercases both
     * parts every time, so each distinct key is built once.
     */
    private static final Map<String, NamespacedKey> KEYS = new ConcurrentHashMap<>();

    /**
     * Returns the namespaced key owned by the given plugin.
     *
     * @param plugin the owning plugin
     * @param key    the key name
     * @return the namespaced key
     */
    public static NamespacedKey key(JavaPlugin plugin, String key) {
        String cacheKey = plugin.getName() + '\0' + key;
        NamespacedKey cached = KEYS.get(cacheKey);
        if (cached != null) return cached;

        NamespacedKey created = new NamespacedKey(plugin, key);
        if (KEYS.size() < MAX_CACHED_KEYS) KEYS.putIfAbsent(cacheKey, created);
        return created;
    }

    /**
     * Writes an integer marker into the stack's persistent data container.
     *
     * @param stack the stack to tag
     * @param key   the key to write
     */
    public static void setMarker(ItemStack stack, NamespacedKey key) {
        if (stack == null || key == null) return;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
        stack.setItemMeta(meta);
    }

    /**
     * Checks whether the stack carries the given integer marker.
     *
     * @param stack the stack to inspect
     * @param key   the key to look for
     * @return {@code true} when the marker is present
     */
    public static boolean hasMarker(ItemStack stack, NamespacedKey key) {
        if (stack == null || key == null) return false;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    /**
     * Writes a string value into the stack's persistent data container.
     *
     * @param stack the stack to tag
     * @param key   the key to write
     * @param value the value to store
     */
    public static void setString(ItemStack stack, NamespacedKey key, String value) {
        if (stack == null || key == null) return;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        stack.setItemMeta(meta);
    }

    /**
     * Reads a string value from the stack's persistent data container.
     *
     * @param stack the stack to read from
     * @param key   the key to read
     * @return the stored value, or {@code null} when absent
     */
    public static String getString(ItemStack stack, NamespacedKey key) {
        if (stack == null || key == null) return null;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;

        if (! meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    /**
     * Removes every persistent data container key in the given namespace from the stack.
     *
     * @param stack     the stack to strip
     * @param namespace the namespace whose keys should be removed
     */
    public static void stripNamespace(ItemStack stack, String namespace) {
        if (stack == null || namespace == null) return;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        // Collect first: removing while iterating the container's key set risks a
        // ConcurrentModificationException on some implementations.
        List<NamespacedKey> doomed = new ArrayList<>();
        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
            if (key.getNamespace().equalsIgnoreCase(namespace)) {
                doomed.add(key);
            }
        }
        if (doomed.isEmpty()) return;

        for (NamespacedKey key : doomed) {
            meta.getPersistentDataContainer().remove(key);
        }
        stack.setItemMeta(meta);
    }
}
