package host.plas.bou.blocking;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Manages blocking of string identifiers for a specified duration.
 * Provides static methods to block, unblock, and query the blocked state
 * of identifiers using a thread-safe set of {@link BlockedString} instances.
 */
public class IdentifiedBlocking {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private IdentifiedBlocking() {
        // utility class
    }

    /**
     * The set of currently blocked string identifiers with their expiration data.
     *
     * @param blockedStrings the set of blocked strings to use
     * @return the current set of blocked strings
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<BlockedString> blockedStrings = new ConcurrentSkipListSet<>();

    /**
     * Blocks the given item, first removing any existing block with the same identifier.
     *
     * @param item the BlockedString to add to the blocked set
     */
    public static void block(BlockedString item) {
        unblock(item);

        getBlockedStrings().add(item);
    }

    /**
     * Blocks the given identifier for the specified number of ticks.
     *
     * @param identifier the string identifier to block
     * @param forTicks the number of ticks to block the identifier for
     */
    public static void block(String identifier, long forTicks) {
        block(new BlockedString(identifier, forTicks));
    }

    /**
     * Unblocks the identifier associated with the given BlockedString.
     *
     * @param item the BlockedString whose identifier should be unblocked
     */
    public static void unblock(BlockedString item) {
        unblock(item.getIdentifier());
    }

    /**
     * Removes all blocked entries matching the given identifier.
     *
     * @param identifier the string identifier to unblock
     */
    public static void unblock(String identifier) {
        getBlockedStrings().removeIf(b -> b.getIdentifier().equals(identifier));
    }

    /**
     * Retrieves the BlockedString for the given identifier, if it is currently blocked.
     *
     * @param identifier the string identifier to look up
     * @return an Optional containing the BlockedString if found, or empty otherwise
     */
    public static Optional<BlockedString> getBlockedItem(String identifier) {
        return getBlockedStrings().stream()
                .filter(b -> b.getIdentifier().equals(identifier))
                .findFirst();
    }

    /**
     * Checks whether the given identifier is currently blocked.
     *
     * @param identifier the string identifier to check
     * @return {@code true} if the identifier is blocked, {@code false} otherwise
     */
    public static boolean isBlocked(String identifier) {
        return getBlockedItem(identifier).isPresent();
    }
}
