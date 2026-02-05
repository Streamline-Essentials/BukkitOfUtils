package host.plas.bou.blocking;

import gg.drak.thebase.objects.Identifiable;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.events.blocking.IdentifierUnblockedEvent;
import host.plas.bou.scheduling.BaseDelayedRunnable;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BlockedString extends BaseDelayedRunnable implements Identifiable {
    private String identifier;
    /**
     * The number of ticks this string is blocked for.
     * Should not be updated after object creation. (Would do nothing.)
     */
    private long blockedTicks;
    /**
     * A callback to be executed when the string is unblocked.
     * true = cancel, false = do not cancel
     */
    @Nullable
    private Supplier<Boolean> callback;

    public BlockedString(String identifier, long blockedTicks, @Nullable Supplier<Boolean> callback) {
        super(blockedTicks);
        this.identifier = identifier;
        this.blockedTicks = blockedTicks;
        this.callback = callback;
    }

    public BlockedString(String identifier, long blockedTicks) {
        super(blockedTicks);
        this.identifier = identifier;
        this.blockedTicks = blockedTicks;
        this.callback = () -> {
            IdentifierUnblockedEvent event = new IdentifierUnblockedEvent(this).fire();
            return event.isCancelled();
        };
    }

    @Override
    public void runDelayed() {
        onUnblocked();
    }

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

    public void clear() {
        if (!isCancelled()) cancel();
        IdentifiedBlocking.unblock(getIdentifier());
    }

    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * The number of ticks this string is blocked for.
     * Should not be updated after object creation. (Would do nothing.)
     */
    public long getBlockedTicks() {
        return this.blockedTicks;
    }

    /**
     * A callback to be executed when the string is unblocked.
     * true = cancel, false = do not cancel
     */
    @Nullable
    public Supplier<Boolean> getCallback() {
        return this.callback;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * The number of ticks this string is blocked for.
     * Should not be updated after object creation. (Would do nothing.)
     */
    public void setBlockedTicks(final long blockedTicks) {
        this.blockedTicks = blockedTicks;
    }

    /**
     * A callback to be executed when the string is unblocked.
     * true = cancel, false = do not cancel
     */
    public void setCallback(@Nullable final Supplier<Boolean> callback) {
        this.callback = callback;
    }
}
