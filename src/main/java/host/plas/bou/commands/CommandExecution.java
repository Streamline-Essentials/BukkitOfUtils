package host.plas.bou.commands;

/**
 * A functional interface for command execution logic that takes a CommandContext
 * and returns a boolean result indicating success or failure.
 */
public interface CommandExecution extends WithCommandContext<Boolean> {
    /**
     * Creates an empty command execution handler that always returns false.
     *
     * @return an EmptyCommandExecution that returns false
     */
    static EmptyCommandExecution emptyFalse() {
        return ctx -> false;
    }

    /**
     * Creates an empty command execution handler that always returns true.
     *
     * @return an EmptyCommandExecution that returns true
     */
    static EmptyCommandExecution emptyTrue() {
        return ctx -> true;
    }
}
