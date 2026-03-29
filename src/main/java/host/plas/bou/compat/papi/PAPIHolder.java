package host.plas.bou.compat.papi;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.ApiHolder;
import host.plas.bou.compat.CompatManager;
import host.plas.bou.compat.papi.expansion.BetterExpansion;
import host.plas.bou.compat.papi.expansion.own.BouExpansion;
import host.plas.bou.utils.EntityUtils;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * API holder for the PlaceholderAPI plugin.
 * Manages the lifecycle of placeholder expansions, including registration,
 * unregistration, and placeholder replacement.
 */
public class PAPIHolder extends ApiHolder<PlaceholderAPIPlugin> {
    /**
     * The set of all loaded BetterExpansion instances registered with PlaceholderAPI.
     *
     * @param loadedExpansions the set of loaded expansions to set
     * @return the set of loaded expansions
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<BetterExpansion> loadedExpansions = new ConcurrentSkipListSet<>();

    /**
     * The built-in BOU placeholder expansion instance.
     *
     * @param ownExpansion the BOU expansion to set
     * @return the BOU expansion instance
     */
    @Getter @Setter
    private static BouExpansion ownExpansion;

    /**
     * Override flag for the enabled state: -1 = unset, 0 = disabled, 1 = enabled.
     *
     * @param enableOverride the enable override value to set
     * @return the enable override value
     */
    @Getter @Setter
    private int enableOverride;

    /**
     * Loads a BetterExpansion into the set of loaded expansions,
     * replacing any existing expansion with the same identifier.
     *
     * @param expansion the expansion to load
     */
    public static void loadExpansion(BetterExpansion expansion) {
        unloadExpansion(expansion);

        loadedExpansions.add(expansion);
    }

    /**
     * Unloads an expansion by its identifier, removing it from the loaded expansions set.
     *
     * @param identifier the identifier of the expansion to unload
     */
    public static void unloadExpansion(String identifier) {
        loadedExpansions.removeIf(expansion -> expansion.getIdentifier().equalsIgnoreCase(identifier));
    }

    /**
     * Unloads the given expansion from the loaded expansions set.
     *
     * @param expansion the expansion to unload
     */
    public static void unloadExpansion(BetterExpansion expansion) {
        unloadExpansion(expansion.getIdentifier());
    }

    /**
     * Retrieves a loaded expansion by its identifier.
     *
     * @param identifier the identifier of the expansion to find
     * @return an Optional containing the expansion if found, or empty otherwise
     */
    public static Optional<BetterExpansion> getExpansion(String identifier) {
        return loadedExpansions.stream().filter(expansion -> expansion.getIdentifier().equalsIgnoreCase(identifier)).findFirst();
    }

    /**
     * Checks whether an expansion with the given identifier is currently loaded.
     *
     * @param identifier the identifier to check
     * @return true if the expansion is loaded, false otherwise
     */
    public static boolean isExpansionLoaded(String identifier) {
        return getExpansion(identifier).isPresent();
    }

    /**
     * Constructs a new PAPIHolder, retrieving the PlaceholderAPI plugin instance.
     * Initializes the holder and sets the enable override to unset (-1).
     */
    public PAPIHolder() {
        super(CompatManager.PAPI_IDENTIFIER, (v) -> PlaceholderAPIPlugin.getInstance());

        try {
            init();
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Failed to initialize PlaceholderAPI holder.");
        }

        enableOverride = -1; // not set
    }

    /**
     * Registers a PlaceholderExpansion with the PlaceholderAPI.
     *
     * @param expansion the PlaceholderExpansion to register
     */
    public void register(PlaceholderExpansion expansion) {
        expansion.register();
    }

    /**
     * Registers a BetterExpansion with the PlaceholderAPI.
     *
     * @param expansion the BetterExpansion to register
     */
    public void register(BetterExpansion expansion) {
        expansion.register();
    }

    /**
     * Unregisters a PlaceholderExpansion from the PlaceholderAPI.
     *
     * @param expansion the PlaceholderExpansion to unregister
     */
    public void unregister(PlaceholderExpansion expansion) {
        expansion.unregister();
    }

    /**
     * Unregisters a BetterExpansion from the PlaceholderAPI.
     *
     * @param expansion the BetterExpansion to unregister
     */
    public void unregister(BetterExpansion expansion) {
        expansion.unregister();
    }

    /**
     * Unregisters all currently loaded expansions from the PlaceholderAPI.
     */
    public void unregisterAll() {
        loadedExpansions.forEach(this::unregister);
    }

    /**
     * Registers all currently loaded expansions with the PlaceholderAPI.
     */
    public void registerAll() {
        loadedExpansions.forEach(this::register);
    }

    /**
     * Initializes this holder by creating the built-in BOU expansion
     * if PlaceholderAPI is enabled.
     */
    public void init() {
        if (isEnabled()) {
            ownExpansion = new BouExpansion();
        }
    }

    /**
     * Retrieves all loaded expansions that belong to the given plugin.
     *
     * @param plugin the plugin to filter expansions by
     * @return a set of expansions belonging to the given plugin
     */
    public ConcurrentSkipListSet<BetterExpansion> getOfPlugin(BetterPlugin plugin) {
        ConcurrentSkipListSet<BetterExpansion> expansions = new ConcurrentSkipListSet<>();

        getLoadedExpansions().forEach(expansion -> {
            if (expansion.getBetterPlugin().getIdentifier().equals(plugin.getIdentifier())) {
                expansions.add(expansion);
            }
        });

        return expansions;
    }

    /**
     * Flushes (unregisters) all expansions belonging to the given plugin.
     *
     * @param plugin the plugin whose expansions should be flushed
     */
    public void flush(BetterPlugin plugin) {
        getOfPlugin(plugin).forEach(expansion -> {
            try {
                unregister(expansion);
                if (! expansion.unregister()) {
//                    BukkitOfUtils.getInstance().logWarning("Failed to unregister expansion " + expansion.getIdentifier() + " from PlaceholderAPI.");
                }
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logWarning("Failed to unregister expansion " + expansion.getIdentifier() + " from PlaceholderAPI.", e);
            }
        });
    }

    /**
     * Flushes (unregisters) all loaded expansions.
     */
    public void flushAll() {
        getLoadedExpansions().forEach(expansion -> {
            try {
                unregister(expansion);
                if (! expansion.unregister()) {
//                    BukkitOfUtils.getInstance().logWarning("Failed to unregister expansion " + expansion.getIdentifier() + " from PlaceholderAPI.");
                }
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logWarning("Failed to unregister expansion " + expansion.getIdentifier() + " from PlaceholderAPI.", e);
            }
        });
    }

    /**
     * Checks whether this holder is enabled, considering both the API availability
     * and any enable override that may have been set.
     *
     * @return true if the API is present and the override allows it
     */
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && (getEnableOverride() == -1 || getEnableOverride() == 1);
    }

    /**
     * Shuts down the PlaceholderAPI integration by flushing all expansions,
     * unregistering the built-in expansion, clearing the loaded set,
     * and setting the enable override to disabled.
     */
    public void shutdown() {
        if (! isEnabledRaw()) return;

        flushAll();

        if (ownExpansion.isRegistered()) {
            ownExpansion.unregister();
        }

        loadedExpansions.clear();

        enableOverride = 0; // set to false

        BukkitOfUtils.getInstance().logInfo("&cPlaceholderAPI &fholder has been shut down.");
    }

    /**
     * Checks whether the API is enabled at the raw level, without considering
     * the enable override.
     *
     * @return true if the underlying API is present
     */
    public boolean isEnabledRaw() {
        return super.isEnabled();
    }

    /**
     * Replaces PlaceholderAPI placeholders in the given string for the specified player.
     *
     * @param player the player to resolve placeholders for
     * @param from the string containing placeholders to replace
     * @return the string with placeholders replaced, or the original string if disabled
     */
    public String replace(OfflinePlayer player, String from) {
        if (! isEnabled()) return from;
        return PlaceholderAPI.setPlaceholders(player, from);
    }

    /**
     * Replaces PlaceholderAPI placeholders in the given string using a dummy player.
     *
     * @param from the string containing placeholders to replace
     * @return the string with placeholders replaced
     */
    public String replace(String from) {
        return replace(EntityUtils.getDummyOfflinePlayer(), from);
    }
}
