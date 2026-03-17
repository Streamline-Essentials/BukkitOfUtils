package host.plas.bou.compat;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.luckperms.LPHeld;
import host.plas.bou.compat.luckperms.LPHolder;
import host.plas.bou.compat.papi.PAPIHeld;
import host.plas.bou.compat.papi.PAPIHolder;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Central manager for plugin compatibility integrations.
 * Handles registration and retrieval of held holders and compatibility managers
 * for external plugins such as PlaceholderAPI and LuckPerms.
 */
@Getter @Setter
public class CompatManager {
    /**
     * Private constructor to prevent instantiation.
     */
    private CompatManager() {
    }

    /** Identifier for the PlaceholderAPI integration. */
    public static final String PAPI_IDENTIFIER = "PlaceholderAPI";
    /** Identifier for the LuckPerms integration. */
    public static final String LP_IDENTIFIER = "LuckPerms";

    /**
     * Map of registered held holders keyed by their identifier.
     *
     * @param holders the holders map to set
     * @return the holders map
     */
    @Getter @Setter
    private static ConcurrentSkipListMap<String, HeldHolder> holders = new ConcurrentSkipListMap<>();

    /**
     * Map of registered compatibility managers keyed by their assigned ID.
     *
     * @param managers the managers map to set
     * @return the managers map
     */
    @Getter @Setter
    private static ConcurrentSkipListMap<Integer, CompatibilityManager> managers = new ConcurrentSkipListMap<>();

    /**
     * Initializes the default compatibility holders for PlaceholderAPI and LuckPerms.
     */
    public static void init() {
        registerHolder(PAPI_IDENTIFIER, PAPIHeld::new);
        registerHolder(LP_IDENTIFIER, LPHeld::new);
    }

    /**
     * Registers a compatibility manager and assigns it a unique ID.
     *
     * @param manager the compatibility manager to register
     * @return the assigned unique ID for the registered manager
     */
    public static int registerManager(CompatibilityManager manager) {
        int id = 0;
        try {
            id = managers.lastKey() + 1;
        } catch (Throwable e) {
            // ignored
        }
        managers.put(id, manager);

        return id;
    }

    /**
     * Unregisters a compatibility manager by its ID.
     *
     * @param id the ID of the manager to unregister
     */
    public static void unregisterManager(int id) {
        managers.remove(id);
    }

    /**
     * Retrieves a compatibility manager by its ID.
     *
     * @param id the ID of the manager to retrieve
     * @return the compatibility manager, or null if not found
     */
    public static CompatibilityManager getManager(int id) {
        return managers.get(id);
    }

    /**
     * Directly puts a held holder into the holders map with the given identifier.
     *
     * @param identifier the identifier key for the holder
     * @param holder the held holder to store
     */
    public static void putHolderRaw(String identifier, HeldHolder holder) {
        getHolders().put(identifier, holder);
    }

    /**
     * Registers a held holder using a creation function that accepts the identifier.
     * If creation fails, an empty holder is registered instead.
     *
     * @param identifier the identifier for the holder
     * @param creationFunction the function to create the held holder from the identifier
     */
    public static void registerHolder(String identifier, Function<String, HeldHolder> creationFunction) {
        HeldHolder holder = new EmptyHolder(identifier);
        try {
            holder = creationFunction.apply(identifier);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logInfo(identifier + " not found, skipping...");
        }
        holders.put(identifier, holder);
    }

    /**
     * Registers a held holder using a supplier.
     * If creation fails, an empty holder is registered instead.
     *
     * @param identifier the identifier for the holder
     * @param creationFunction the supplier to create the held holder
     */
    public static void registerHolder(String identifier, Supplier<HeldHolder> creationFunction) {
        HeldHolder holder = new EmptyHolder(identifier);
        try {
            holder = creationFunction.get();
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logInfo(identifier + " not found, skipping...");
        }
        holders.put(identifier, holder);
    }

    /**
     * Unregisters a held holder by its identifier.
     *
     * @param identifier the identifier of the holder to remove
     */
    public static void unregisterHolder(String identifier) {
        getHolders().remove(identifier);
    }

    /**
     * Retrieves a held holder by its identifier.
     *
     * @param identifier the identifier of the holder to retrieve
     * @return the held holder, or null if not found
     */
    public static HeldHolder getHolder(String identifier) {
        return getHolders().get(identifier);
    }

    /**
     * Checks whether a holder with the given identifier is registered and enabled.
     *
     * @param identifier the identifier to check
     * @return true if the holder exists and is enabled, false otherwise
     */
    public static boolean isEnabled(String identifier) {
        return getHolder(identifier) != null && getHolder(identifier).isEnabled();
    }

    /**
     * Retrieves the PlaceholderAPI held holder if available.
     *
     * @return an Optional containing the PAPIHeld instance, or empty if unavailable
     */
    public static Optional<PAPIHeld> getPAPIHeld() {
        HeldHolder holder = getHolder(PAPI_IDENTIFIER);
        try {
            return Optional.of((PAPIHeld) holder);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the LuckPerms held holder if available.
     *
     * @return an Optional containing the LPHeld instance, or empty if unavailable
     */
    public static Optional<LPHeld> getLPHeld() {
        HeldHolder holder = getHolder(LP_IDENTIFIER);
        try {
            return Optional.of((LPHeld) holder);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the PlaceholderAPI holder (ApiHolder) if available.
     *
     * @return an Optional containing the PAPIHolder instance, or empty if unavailable
     */
    public static Optional<PAPIHolder> getPAPIHolder() {
        Optional<PAPIHeld> held = getPAPIHeld();
        if (held.isEmpty()) return Optional.empty();
        PAPIHeld h = held.get();
        ApiHolder<?> holder = h.getHolder();
        try {
            return Optional.of((PAPIHolder) holder);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the LuckPerms holder (ApiHolder) if available.
     *
     * @return an Optional containing the LPHolder instance, or empty if unavailable
     */
    public static Optional<LPHolder> getLPHolder() {
        Optional<LPHeld> held = getLPHeld();
        if (held.isEmpty()) return Optional.empty();
        LPHeld h = held.get();
        ApiHolder<?> holder = h.getHolder();
        try {
            return Optional.of((LPHolder) holder);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    /**
     * Checks whether PlaceholderAPI is enabled.
     *
     * @return true if PlaceholderAPI is registered and enabled
     */
    public static boolean isPAPIEnabled() {
        return isEnabled(PAPI_IDENTIFIER);
    }

    /**
     * Checks whether LuckPerms is enabled.
     *
     * @return true if LuckPerms is registered and enabled
     */
    public static boolean isLPEnabled() {
        return isEnabled(LP_IDENTIFIER);
    }
}
