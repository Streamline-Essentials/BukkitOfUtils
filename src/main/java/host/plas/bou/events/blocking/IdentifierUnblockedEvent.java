package host.plas.bou.events.blocking;

import host.plas.bou.blocking.BlockedString;
import host.plas.bou.events.self.BouEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * Event fired when a blocked identifier becomes unblocked.
 */
@Getter @Setter
public class IdentifierUnblockedEvent extends BouEvent {
    /**
     * The blocked string that was unblocked, triggering this event.
     *
     * @param blocked the blocked string to set
     * @return the blocked string associated with this event
     */
    private BlockedString blocked;

    /**
     * Constructs a new IdentifierUnblockedEvent for the given blocked string.
     *
     * @param blocked the blocked string that has been unblocked
     */
    public IdentifierUnblockedEvent(BlockedString blocked) {
        super();
        this.blocked = blocked;
    }

    /**
     * Retrieves the identifier of the unblocked string.
     *
     * @return the identifier string
     */
    public String getIdentifier() {
        return getBlocked().getIdentifier();
    }
}
