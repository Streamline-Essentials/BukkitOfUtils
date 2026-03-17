package host.plas.bou.events.self;

import host.plas.bou.BukkitOfUtils;
import gg.drak.thebase.events.components.BaseEvent;

/**
 * Base event class for all BukkitOfUtils custom events.
 * Extends {@link BaseEvent} and provides access to the BukkitOfUtils instance.
 */
public class BouEvent extends BaseEvent {
    /**
     * Constructs a new BouEvent.
     */
    public BouEvent() {
        super();
    }

    /**
     * Retrieves the BukkitOfUtils plugin instance.
     *
     * @return the current BukkitOfUtils instance
     */
    public BukkitOfUtils getBou() {
        return BukkitOfUtils.getInstance();
    }
}
