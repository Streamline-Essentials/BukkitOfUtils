package host.plas.bou.blocking;

import gg.drak.thebase.objects.Identifiable;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.events.blocking.IdentifierUnblockedEvent;
import host.plas.bou.scheduling.BaseDelayedRunnable;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Getter @Setter
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
        if (! isCancelled()) cancel();

        IdentifiedBlocking.unblock(getIdentifier());
    }
}
