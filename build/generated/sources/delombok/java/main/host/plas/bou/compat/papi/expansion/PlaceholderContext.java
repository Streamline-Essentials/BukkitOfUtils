package host.plas.bou.compat.papi.expansion;

import host.plas.bou.utils.obj.ContextedString;
import org.bukkit.OfflinePlayer;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlaceholderContext extends ContextedString<PlaceholderArgument> {
    public static final Function<BetterExpansion, Supplier<PlaceholderArgument>> ARGUMENT_CREATOR = expansion -> () -> new PlaceholderArgument(expansion);
    public static final Function<BetterExpansion, BiFunction<Integer, String[], PlaceholderArgument>> ARGUMENT_CREATOR_INDEXED = expansion -> (i, args) -> new PlaceholderArgument(expansion, i, args[i]);
    private BetterExpansion expansion;
    private OfflinePlayer player;
    private String rawParams;

    public PlaceholderContext(BetterExpansion expansion, OfflinePlayer player, String rawParams) {
        super(ARGUMENT_CREATOR.apply(expansion), ARGUMENT_CREATOR_INDEXED.apply(expansion), split(rawParams));
        this.expansion = expansion;
        this.player = player;
        this.rawParams = rawParams;
    }

    public static ConcurrentSkipListSet<PlaceholderArgument> getArgsFrom(BetterExpansion expansion, String... args) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED.apply(expansion), args);
    }

    public static ConcurrentSkipListSet<PlaceholderArgument> getArgsFrom(BetterExpansion expansion, String string) {
        return ContextedString.getArgsFrom(ARGUMENT_CREATOR_INDEXED.apply(expansion), string, "_");
    }

    public static String[] split(String string) {
        return string.split("_");
    }

    public static PlaceholderContext of(BetterExpansion expansion, OfflinePlayer player, String rawParams) {
        return new PlaceholderContext(expansion, player, rawParams);
    }

    public BetterExpansion getExpansion() {
        return this.expansion;
    }

    public OfflinePlayer getPlayer() {
        return this.player;
    }

    public String getRawParams() {
        return this.rawParams;
    }

    public void setExpansion(final BetterExpansion expansion) {
        this.expansion = expansion;
    }

    public void setPlayer(final OfflinePlayer player) {
        this.player = player;
    }

    public void setRawParams(final String rawParams) {
        this.rawParams = rawParams;
    }
}
