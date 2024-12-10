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
