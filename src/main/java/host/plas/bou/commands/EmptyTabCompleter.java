package host.plas.bou.commands;

/**
 * A marker interface for empty tab completer handlers.
 * Implementations always report themselves as empty via {@link #isEmpty()}.
 */
public interface EmptyTabCompleter extends CommandTabCompleter {
    /**
     * Returns true to indicate this is an empty tab completer.
     *
     * @return always returns true
     */
    @Override
    default boolean isEmpty() {
        return true;
    }
}
