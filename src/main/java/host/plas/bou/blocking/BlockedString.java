package host.plas.bou.blocking;

import gg.drak.thebase.objects.Identifiable;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.events.blocking.IdentifierUnblockedEvent;
import host.plas.bou.scheduling.BaseDelayedRunnable;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Represents a string identifier that is blocked for a specified number of ticks.
 * When the block duration expires, an optional callback is invoked to determine
 * whether the block should be renewed or cleared. Extends {@link BaseDelayedRunnable}
 * to leverage scheduled delayed execution.
 */
@Getter @Setter
public class BlockedString extends BaseDelayedRunnable implements Identifiable {
    private String identifier;
    /**
     * The number of ticks this string is blocked for.
     * Should not be updated after object creation. (Would do nothing.)
     * @param blockedTicks the number of ticks to block for
     * @return the number of ticks this string is blocked for
     */
    private long blockedTicks;

    /**
     * A callback to be executed when the string is unblocked.
     * true = cancel, false = do not cancel
     * @param callback the callback supplier to set
     * @return the callback supplier, or null if none
     */
    @Nullable
    private Supplier<Boolean> callback;

    /**
     * Constructs a new BlockedString with a custom callback.
     *
     * @param identifier the string identifier to block
     * @param blockedTicks the number of ticks this string should be blocked for
     * @param callback a supplier that returns {@code true} to re-block or {@code false} to clear; may be null
     */
    public BlockedString(String identifier, long blockedTicks, @Nullable Supplier<Boolean> callback) {
        super(blockedTicks);
        this.identifier = identifier;
        this.blockedTicks = blockedTicks;
        this.callback = callback;
    }

    /**
     * Constructs a new BlockedString with a default callback that fires an
     * {@link IdentifierUnblockedEvent} when unblocked.
     *
     * @param identifier the string identifier to block
     * @param blockedTicks the number of ticks this string should be blocked for
     */
    public BlockedString(String identifier, long blockedTicks) {
        super(blockedTicks);
        this.identifier = identifier;
        this.blockedTicks = blockedTicks;
        this.callback = () -> {
            IdentifierUnblockedEvent event = new IdentifierUnblockedEvent(this).fire();

            return event.isCancelled();
        };
    }

    /**
     * {@inheritDoc}
     * Called when the delay expires; delegates to {@link #onUnblocked()}.
     */
    @Override
    public void runDelayed() {
        onUnblocked();
    }

    /**
     * Handles the unblocking logic. Invokes the callback if present; if the callback
     * returns {@code true}, the identifier is re-blocked for the same duration.
     * Otherwise, the block is cleared.
     */
    public void onUnblocked() {
        try {
            if (callback != null) {
                boolean cancel = callback.get();

                if (cancel) {
                    IdentifiedBlocking.block(getIdentifier(), getBlockedTicks());
                } else {
                    clear();
                }
            }
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning(e);
        }
    }

    /**
     * Cancels the delayed task if still running and removes this identifier
     * from the blocked strings set.
     */
    public void clear() {
        if (! isCancelled()) cancel();

        IdentifiedBlocking.unblock(getIdentifier());
    }
}
