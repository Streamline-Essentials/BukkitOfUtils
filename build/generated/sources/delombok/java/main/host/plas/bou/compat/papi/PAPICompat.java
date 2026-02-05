package host.plas.bou.compat.papi;

import host.plas.bou.BetterPlugin;
import host.plas.bou.compat.ApiHolder;
import host.plas.bou.compat.CompatManager;
import org.bukkit.OfflinePlayer;

public class PAPICompat {
    public static void flush(BetterPlugin plugin) {
        if (! CompatManager.isPAPIEnabled()) return;

        CompatManager.getPAPIHolder().ifPresent(held -> held.flush(plugin));
    }

    public static void flushAll() {
        if (! CompatManager.isPAPIEnabled()) return;

        CompatManager.getPAPIHolder().ifPresent(PAPIHolder::flushAll);
    }

    public static void shutdown() {
        if (! CompatManager.isPAPIEnabled()) return;

        CompatManager.getPAPIHolder().ifPresent(PAPIHolder::shutdown);
    }

    public static boolean isEnabled() {
        return CompatManager.isPAPIEnabled();
    }

    public static boolean isEnabledRaw() {
        return CompatManager.getPAPIHolder().map(PAPIHolder::isEnabledRaw).orElse(false);
    }

    public static boolean isEnabledOverriden() {
        return CompatManager.getPAPIHolder().map(PAPIHolder::getEnableOverride).map(i -> i == 1).orElse(false);
    }

    public static String replace(OfflinePlayer player, String from) {
        return CompatManager.getPAPIHolder()
                .filter(ApiHolder::isEnabled)
                .map(held -> held.replace(player, from)).orElse(from);
    }

    public static String replace(String from) {
        return CompatManager.getPAPIHolder()
                .filter(ApiHolder::isEnabled)
                .map(held -> held.replace(from)).orElse(from);
    }
}
