package host.plas.bou.events;

import host.plas.bou.BukkitOfUtils;

public class BOUListener implements ListenerConglomerate {
    public BOUListener() {
        BukkitOfUtils.getInstance().registerListenerConglomerate(this);

        BukkitOfUtils.getInstance().logInfo("Registered BOUListener: &c" + this.getClass().getSimpleName());
    }
}
