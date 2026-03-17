package host.plas.bou.compat.papi.expansion;

import host.plas.bou.utils.obj.StringArgument;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single argument within a placeholder expression.
 * Extends {@link StringArgument} with an associated {@link BetterExpansion}
 * and a fixed context type of {@code PLACEHOLDER}.
 */
@Getter @Setter
public class PlaceholderArgument extends StringArgument {
    /** The fixed context type for all placeholder arguments. */
    public static final ContextType CONTEXT_TYPE = ContextType.PLACEHOLDER;

    /**
     * The expansion this placeholder argument belongs to.
     *
     * @param expansion the expansion to set
     * @return the associated expansion
     */
    private BetterExpansion expansion;

    /**
     * Constructs a PlaceholderArgument with an index, content, and an optional content type override.
     *
     * @param expansion the expansion this argument belongs to
     * @param index the positional index of this argument
     * @param content the string content of this argument
     * @param overrideType the content type override, or null to auto-detect
     */
    public PlaceholderArgument(BetterExpansion expansion, int index, String content, @Nullable ContentType overrideType) {
        super(index, content, overrideType, CONTEXT_TYPE);

        this.expansion = expansion;
    }

    /**
     * Constructs a PlaceholderArgument with an index and content, auto-detecting the content type.
     *
     * @param expansion the expansion this argument belongs to
     * @param index the positional index of this argument
     * @param content the string content of this argument
     */
    public PlaceholderArgument(BetterExpansion expansion, int index, String content) {
        super(index, content, CONTEXT_TYPE);

        this.expansion = expansion;
    }

    /**
     * Constructs an empty PlaceholderArgument with default values.
     *
     * @param expansion the expansion this argument belongs to
     */
    public PlaceholderArgument(BetterExpansion expansion) {
        super(CONTEXT_TYPE);

        this.expansion = expansion;
    }
}
