package host.plas.bou.compat.papi.expansion.own;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.papi.PAPIHolder;
import host.plas.bou.compat.papi.expansion.BetterExpansion;
import host.plas.bou.compat.papi.expansion.PlaceholderContext;
import host.plas.bou.utils.ColorUtils;
import host.plas.bou.utils.PluginUtils;
import org.jetbrains.annotations.Nullable;

/**
 * The built-in PlaceholderAPI expansion for BukkitOfUtils.
 * Provides placeholders for loaded expansion counts and text colorization.
 */
public class BouExpansion extends BetterExpansion {
    /**
     * Constructs the BOU expansion with metadata derived from the BukkitOfUtils plugin instance.
     */
    public BouExpansion() {
        super(BukkitOfUtils.getInstance(), "bou",
                () -> BukkitOfUtils.getInstance().getDescription().getAuthors().get(0),
                () -> BukkitOfUtils.getInstance().getDescription().getVersion());
    }

    /**
     * Resolves BOU-specific placeholders based on the given context.
     * Supported placeholders include:
     * <ul>
     *   <li>{@code expansions_papi_loaded} - number of loaded PAPI expansions</li>
     *   <li>{@code expansions_loaded} - number of loaded BOU plugins</li>
     *   <li>{@code colored_<text>} - colorizes the given text</li>
     * </ul>
     *
     * @param context the placeholder context containing the player and parameters
     * @return the resolved placeholder value, or null if the placeholder is not recognized
     */
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
