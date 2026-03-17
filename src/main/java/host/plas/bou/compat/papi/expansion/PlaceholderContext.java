package host.plas.bou.compat.papi.expansion;

import host.plas.bou.utils.obj.ContextedString;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents the context of a placeholder request, including the expansion,
 * the requesting player, and the raw parameter string. Parses the parameters
 * into {@link PlaceholderArgument} instances by splitting on underscores.
 */
@Getter @Setter
public class PlaceholderContext extends ContextedString<PlaceholderArgument> {
    /**
     * Factory function that creates a supplier of default PlaceholderArgument instances for a given expansion.
     */
    public static final Function<BetterExpansion, Supplier<PlaceholderArgument>> ARGUMENT_CREATOR =
            expansion -> () -> new PlaceholderArgument(expansion);

    /**
     * Factory function that creates an indexed PlaceholderArgument constructor for a given expansion.
     */
    public static final Function<BetterExpansion, BiFunction<Integer, String[], PlaceholderArgument>> ARGUMENT_CREATOR_INDEXED =
            expansion -> (i, args) -> new PlaceholderArgument(expansion, i, args[i]);

    /**
     * The expansion handling this placeholder request.
     *
     * @param expansion the expansion to set
     * @return the expansion
     */
    private BetterExpansion expansion;

    /**
     * The player the placeholder is being resolved for.
     *
     * @param player the player to set
     * @return the player
     */
    private OfflinePlayer player;

    /**
     * The raw parameter string passed to this placeholder context.
     *
     * @param rawParams the raw parameter string to set
     * @return the raw parameter string
     */
    private String rawParams;

    /**
     * Constructs a PlaceholderContext by parsing the raw parameters into arguments.
     *
     * @param expansion the expansion handling this placeholder request
     * @param player the player the placeholder is being resolved for
     * @param rawParams the raw parameter string to parse
     */
    public PlaceholderContext(BetterExpansion expansion, OfflinePlayer player, String rawParams) {
        super(ARGUMENT_CREATOR.apply(expansion), ARGUMENT_CREATOR_INDEXED.apply(expansion), split(rawParams));

        this.expansion = expansion;
        this.player = player;
        this.rawParams = rawParams;
    }

    /**
     * Creates a set of PlaceholderArguments from the given string array for the specified expansion.
     *
     * @param expansion the expansion to associate with the arguments
     * @param args the string arguments to convert
     * @return a sorted set of PlaceholderArgument instances
     */
    public static ConcurrentSkipListSet<PlaceholderArgument> getArgsFrom(BetterExpansion expansion, String... args) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED.apply(expansion), args);
    }

    /**
     * Creates a set of PlaceholderArguments by splitting the given string on underscores.
     *
     * @param expansion the expansion to associate with the arguments
     * @param string the string to split and convert into arguments
     * @return a sorted set of PlaceholderArgument instances
     */
    public static ConcurrentSkipListSet<PlaceholderArgument> getArgsFrom(BetterExpansion expansion, String string) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED.apply(expansion), string, "_");
    }

    /**
     * Splits a parameter string by underscores.
     *
     * @param string the string to split
     * @return an array of split segments
     */
    public static String[] split(String string) {
        return string.split("_");
    }

    /**
     * Factory method to create a new PlaceholderContext.
     *
     * @param expansion the expansion handling this placeholder request
     * @param player the player the placeholder is being resolved for
     * @param rawParams the raw parameter string
     * @return a new PlaceholderContext instance
     */
    public static PlaceholderContext of(BetterExpansion expansion, OfflinePlayer player, String rawParams) {
        return new PlaceholderContext(expansion, player, rawParams);
    }
}
