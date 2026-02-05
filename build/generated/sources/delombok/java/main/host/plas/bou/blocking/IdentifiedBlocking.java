package host.plas.bou.blocking;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Manages blocking of
 */
public class IdentifiedBlocking {
    // Map of identified objects to their block expiration timestamps
    private static ConcurrentSkipListSet<BlockedString> blockedStrings = new ConcurrentSkipListSet<>();

    public static void block(BlockedString item) {
        unblock(item);
        getBlockedStrings().add(item);
    }

    public static void block(String identifier, long forTicks) {
        block(new BlockedString(identifier, forTicks));
    }

    public static void unblock(BlockedString item) {
        unblock(item.getIdentifier());
    }

    public static void unblock(String identifier) {
        getBlockedStrings().removeIf(b -> b.getIdentifier().equals(identifier));
    }

    public static Optional<BlockedString> getBlockedItem(String identifier) {
        return getBlockedStrings().stream().filter(b -> b.getIdentifier().equals(identifier)).findFirst();
    }

    public static boolean isBlocked(String identifier) {
        return getBlockedItem(identifier).isPresent();
    }

    public static ConcurrentSkipListSet<BlockedString> getBlockedStrings() {
        return IdentifiedBlocking.blockedStrings;
    }

    public static void setBlockedStrings(final ConcurrentSkipListSet<BlockedString> blockedStrings) {
        IdentifiedBlocking.blockedStrings = blockedStrings;
    }
}
