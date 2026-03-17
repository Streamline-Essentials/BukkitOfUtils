package host.plas.bou.commands;

import host.plas.bou.utils.obj.StringArgument;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single argument within a command context.
 * Extends StringArgument with a fixed COMMAND context type.
 */
@Getter
public class CommandArgument extends StringArgument {
    /** The fixed context type for command arguments. */
    public static final ContextType CONTEXT_TYPE = ContextType.COMMAND;

    /**
     * Constructs a CommandArgument with a specified index, content, and optional type override.
     *
     * @param index the positional index of this argument
     * @param content the string content of the argument
     * @param overrideType an optional content type override, or null for automatic detection
     */
    public CommandArgument(int index, String content, @Nullable ContentType overrideType) {
        super(index, content, overrideType, CONTEXT_TYPE);
    }

    /**
     * Constructs a CommandArgument with a specified index and content, using automatic type detection.
     *
     * @param index the positional index of this argument
     * @param content the string content of the argument
     */
    public CommandArgument(int index, String content) {
        super(index, content, null, CONTEXT_TYPE);
    }

    /**
     * Constructs a broken/empty CommandArgument with index -1 and null content.
     * Used as a default or placeholder argument.
     */
    public CommandArgument() {
        super(-1, null, ContentType.BROKEN, CONTEXT_TYPE);
    }
}
