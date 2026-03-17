package host.plas.bou.commands;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A functional interface for tab completion logic that takes a CommandContext
 * and returns a set of completion suggestions.
 */
public interface CommandTabCompleter extends WithCommandContext<ConcurrentSkipListSet<String>> {
    /**
     * Creates an empty tab completer that returns no suggestions.
     *
     * @return an EmptyTabCompleter that returns an empty set
     */
    static EmptyTabCompleter empty() {
        return ctx -> new ConcurrentSkipListSet<>();
    }
}
