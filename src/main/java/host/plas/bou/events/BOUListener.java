package host.plas.bou.events;

import host.plas.bou.BukkitOfUtils;

/**
 * Base listener class for BukkitOfUtils events.
 * Automatically registers itself as a listener conglomerate upon construction.
 */
public class BOUListener implements ListenerConglomerate {
    /**
     * Constructs a new BOUListener and registers it with the BukkitOfUtils plugin instance.
     */
    public BOUListener() {
        BukkitOfUtils.getInstance().registerListenerConglomerate(this);

        BukkitOfUtils.getInstance().logInfo("Registered BOUListener: &c" + this.getClass().getSimpleName());
    }
}
