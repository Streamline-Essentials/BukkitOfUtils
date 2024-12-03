package host.plas.bou.compat.papi.expansion.own;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.papi.PAPIHolder;
import host.plas.bou.compat.papi.expansion.BetterExpansion;
import host.plas.bou.compat.papi.expansion.PlaceholderContext;
import host.plas.bou.utils.ColorUtils;
import host.plas.bou.utils.PluginUtils;
import org.jetbrains.annotations.Nullable;

public class BouExpansion extends BetterExpansion {
    public BouExpansion() {
        super(BukkitOfUtils.getInstance(), "bou",
                () -> BukkitOfUtils.getInstance().getDescription().getAuthors().get(0),
                () -> BukkitOfUtils.getInstance().getDescription().getVersion());
    }

    @Override
    public @Nullable String replace(PlaceholderContext context) {
        String raw = context.getRawParams();
        String rawLower = raw.toLowerCase();

        switch (rawLower) {
            case "expansions_papi_loaded":
                return String.valueOf(PAPIHolder.getLoadedExpansions().size());
            case "expansions_loaded":
                return String.valueOf(PluginUtils.getLoadedBOUPlugins().size());
            default:
                if (rawLower.startsWith("colored_")) {
                    String s = raw.substring("colored_".length());
                    return ColorUtils.colorizeHard(s);
                }
                return null;
        }
    }
}
