package host.plas.bou.compat.papi.expansion;

import host.plas.bou.utils.obj.StringArgument;
import org.jetbrains.annotations.Nullable;

public class PlaceholderArgument extends StringArgument {
    public static final ContextType CONTEXT_TYPE = ContextType.PLACEHOLDER;
    private BetterExpansion expansion;

    public PlaceholderArgument(BetterExpansion expansion, int index, String content, @Nullable ContentType overrideType) {
        super(index, content, overrideType, CONTEXT_TYPE);
        this.expansion = expansion;
    }

    public PlaceholderArgument(BetterExpansion expansion, int index, String content) {
        super(index, content, CONTEXT_TYPE);
        this.expansion = expansion;
    }

    public PlaceholderArgument(BetterExpansion expansion) {
        super(CONTEXT_TYPE);
        this.expansion = expansion;
    }

    public BetterExpansion getExpansion() {
        return this.expansion;
    }

    public void setExpansion(final BetterExpansion expansion) {
        this.expansion = expansion;
    }
}
