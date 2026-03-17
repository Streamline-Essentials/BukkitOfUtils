package host.plas.bou.commands;

import java.util.function.Function;

/**
 * A functional interface that extends Function to accept a CommandContext and return a result
 * of the specified type. Serves as the base for command execution and tab completion handlers.
 *
 * @param <R> the return type of the function
 */
public interface WithCommandContext<R> extends Function<CommandContext, R> {
    /**
     * Checks whether this handler is empty (a no-op placeholder).
     *
     * @return false by default; overridden by empty implementations to return true
     */
    default boolean isEmpty() {
        return false;
    }
}
