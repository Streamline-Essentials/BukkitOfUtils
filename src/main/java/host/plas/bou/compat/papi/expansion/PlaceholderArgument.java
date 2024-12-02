package host.plas.bou.compat.papi.expansion;

import host.plas.bou.utils.obj.StringArgument;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
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
}
