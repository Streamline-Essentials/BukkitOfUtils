package host.plas.bou.events.blocking;

import host.plas.bou.blocking.BlockedString;
import host.plas.bou.events.self.BouEvent;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class IdentifierUnblockedEvent extends BouEvent {
    private BlockedString blocked;

    public IdentifierUnblockedEvent(BlockedString blocked) {
        super();
        this.blocked = blocked;
    }

    public String getIdentifier() {
        return getBlocked().getIdentifier();
    }
}
