package host.plas.bou.commands;

/**
 * A marker interface for empty command execution handlers.
 * Implementations always report themselves as empty via {@link #isEmpty()}.
 */
public interface EmptyCommandExecution extends CommandExecution {
    /**
     * Returns true to indicate this is an empty execution handler.
     *
     * @return always returns true
     */
    @Override
    default boolean isEmpty() {
        return true;
    }
}
