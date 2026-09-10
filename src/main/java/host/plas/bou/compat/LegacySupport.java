package host.plas.bou.compat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime capability detection for legacy servers (1.8.x and other pre-1.13 platforms).
 *
 * <p>BukkitOfUtils compiles against the 1.16.5 API but is expected to run on servers as old
 * as 1.8.9. Two API surfaces used throughout the GUI system simply do not exist there:</p>
 *
 * <ul>
 *     <li>{@code ItemMeta.getPersistentDataContainer()} — added in 1.14. Calling it on 1.8
 *     throws {@link NoSuchMethodError}.</li>
 *     <li>The flattened {@link Material} names (e.g. {@code BLACK_STAINED_GLASS_PANE},
 *     {@code OAK_DOOR}) — added in 1.13. Referencing one on 1.8 throws
 *     {@link NoSuchFieldError} at class-initialization time.</li>
 * </ul>
 *
 * <p>All detection here is by <em>member presence</em> rather than by version number or class
 * presence. A hybrid server (Mohist, Arclight) or a shaded API jar can expose the persistence
 * classes while {@link ItemMeta} still lacks the accessor, so probing for the class alone is
 * not a reliable discriminator — the crash this guards against was a {@code NoSuchMethodError},
 * not a {@code NoClassDefFoundError}.</p>
 */
public final class LegacySupport {
    private LegacySupport() {
    }

    /**
     * Whether {@code ItemMeta.getPersistentDataContainer()} exists on this server.
     * Resolved once at class-initialization time and never re-probed.
     */
    private static final boolean PDC_AVAILABLE = probePersistentDataContainer();

    /** Cache of resolved material lookups, keyed by the modern (flattened) name. */
    private static final ConcurrentHashMap<String, Material> MATERIAL_CACHE = new ConcurrentHashMap<>();

    private static boolean probePersistentDataContainer() {
        try {
            ItemMeta.class.getMethod("getPersistentDataContainer");
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    /**
     * Returns whether this server supports the persistent data container API (1.14+).
     *
     * <p>Callers must consult this before touching anything in
     * {@code host.plas.bou.items.PdcTags} or any other persistence-typed code path.</p>
     *
     * @return {@code true} when {@code ItemMeta.getPersistentDataContainer()} is present
     */
    public static boolean hasPersistentDataContainer() {
        return PDC_AVAILABLE;
    }

    /**
     * Returns whether this server predates the 1.13 "flattening" of material names.
     *
     * @return {@code true} when modern flattened material names are unavailable
     */
    public static boolean isLegacyMaterials() {
        return material("BLACK_STAINED_GLASS_PANE", null) == null;
    }

    /**
     * Resolves a {@link Material} by name without risking a {@link NoSuchFieldError}.
     *
     * <p>Each candidate name is tried in order and the first one that exists on the running
     * server is returned. This lets callers write
     * {@code material(null, "BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")} and get the
     * right constant on both modern and legacy platforms.</p>
     *
     * @param fallback   the value to return when no candidate resolves; may be {@code null}
     * @param candidates material names to try, most-preferred first
     * @return the first resolvable material, or {@code fallback} when none exist
     */
    public static Material material(Material fallback, String... candidates) {
        if (candidates == null) return fallback;

        for (String candidate : candidates) {
            Material resolved = material(candidate, null);
            if (resolved != null) return resolved;
        }
        return fallback;
    }

    /**
     * Resolves a single {@link Material} by name, returning {@code fallback} when absent.
     *
     * @param name     the material name to resolve
     * @param fallback the value to return when the name does not exist on this server
     * @return the resolved material, or {@code fallback}
     */
    public static Material material(String name, Material fallback) {
        if (name == null) return fallback;

        Material cached = MATERIAL_CACHE.get(name);
        if (cached != null) return cached;

        try {
            Material resolved = Material.valueOf(name);
            MATERIAL_CACHE.put(name, resolved);
            return resolved;
        } catch (Throwable throwable) {
            return fallback;
        }
    }

    /**
     * Applies a legacy data value (durability) to a stack when running on a pre-1.13 server.
     *
     * <p>On 1.8 all sixteen stained-glass-pane colors share the single
     * {@code STAINED_GLASS_PANE} material and are distinguished by their damage value. On
     * modern servers the color lives in the material itself and this call is a no-op.</p>
     *
     * @param stack the stack to modify; ignored when {@code null}
     * @param data  the legacy data value to apply
     * @return the same stack, for chaining
     */
    @SuppressWarnings("deprecation")
    public static ItemStack applyLegacyData(ItemStack stack, int data) {
        if (stack == null) return null;
        if (! isLegacyMaterials()) return stack;

        stack.setDurability((short) data);
        return stack;
    }
}
