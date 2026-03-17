package host.plas.bou.compat.papi;

import host.plas.bou.BetterPlugin;
import host.plas.bou.compat.ApiHolder;
import host.plas.bou.compat.CompatManager;
import org.bukkit.OfflinePlayer;

/**
 * Utility class providing static convenience methods for interacting with
 * the PlaceholderAPI integration. All methods gracefully handle the case
 * where PlaceholderAPI is not available.
 */
public class PAPICompat {
    /** Private constructor to prevent instantiation of this utility class. */
    private PAPICompat() {}

    /**
     * Flushes (unregisters) all PlaceholderAPI expansions associated with the given plugin.
     *
     * @param plugin the plugin whose expansions should be flushed
     */
    public static void flush(BetterPlugin plugin) {
        if (! CompatManager.isPAPIEnabled()) return;

        CompatManager.getPAPIHolder().ifPresent(held -> held.flush(plugin));
    }

    /**
     * Flushes (unregisters) all loaded PlaceholderAPI expansions.
     */
    public static void flushAll() {
        if (! CompatManager.isPAPIEnabled()) return;

        CompatManager.getPAPIHolder().ifPresent(PAPIHolder::flushAll);
    }

    /**
     * Shuts down the PlaceholderAPI integration, unregistering all expansions
     * and disabling the holder.
     */
    public static void shutdown() {
        if (! CompatManager.isPAPIEnabled()) return;

        CompatManager.getPAPIHolder().ifPresent(PAPIHolder::shutdown);
    }

    /**
     * Checks whether PlaceholderAPI is enabled.
     *
     * @return true if PlaceholderAPI is enabled
     */
    public static boolean isEnabled() {
        return CompatManager.isPAPIEnabled();
    }

    /**
     * Checks whether PlaceholderAPI is enabled using the raw (non-overridden) check.
     *
     * @return true if PlaceholderAPI is enabled at the raw level
     */
    public static boolean isEnabledRaw() {
        return CompatManager.getPAPIHolder().map(PAPIHolder::isEnabledRaw).orElse(false);
    }

    /**
     * Checks whether the PlaceholderAPI enabled state has been overridden to true.
     *
     * @return true if the enable override is set to true
     */
    public static boolean isEnabledOverriden() {
        return CompatManager.getPAPIHolder().map(PAPIHolder::getEnableOverride).map(i -> i == 1).orElse(false);
    }

    /**
     * Replaces PlaceholderAPI placeholders in the given string for the specified player.
     *
     * @param player the player to resolve placeholders for
     * @param from the string containing placeholders to replace
     * @return the string with placeholders replaced, or the original string if PAPI is unavailable
     */
    public static String replace(OfflinePlayer player, String from) {
        return CompatManager.getPAPIHolder()
                .filter(ApiHolder::isEnabled)
                .map(held -> held.replace(player, from)).orElse(from);
    }

    /**
     * Replaces PlaceholderAPI placeholders in the given string using a dummy player.
     *
     * @param from the string containing placeholders to replace
     * @return the string with placeholders replaced, or the original string if PAPI is unavailable
     */
    public static String replace(String from) {
        return CompatManager.getPAPIHolder()
                .filter(ApiHolder::isEnabled)
                .map(held -> held.replace(from)).orElse(from);
    }
}
