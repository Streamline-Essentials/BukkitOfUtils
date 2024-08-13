package host.plas.bou.events.self;

import host.plas.bou.BukkitOfUtils;
import tv.quaint.events.components.BaseEvent;

public class BouEvent extends BaseEvent {
    public BouEvent() {
        super();
    }

    public BukkitOfUtils getBou() {
        return BukkitOfUtils.getInstance();
    }
}
