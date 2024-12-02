package host.plas.bou.commands;

import host.plas.bou.utils.obj.StringArgument;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
public class CommandArgument extends StringArgument {
    public static final ContextType CONTEXT_TYPE = ContextType.COMMAND;

    public CommandArgument(int index, String content, @Nullable ContentType overrideType) {
        super(index, content, overrideType, CONTEXT_TYPE);
    }

    public CommandArgument(int index, String content) {
        super(index, content, null, CONTEXT_TYPE);
    }

    public CommandArgument() {
        super(-1, null, ContentType.BROKEN, CONTEXT_TYPE);
    }
}
