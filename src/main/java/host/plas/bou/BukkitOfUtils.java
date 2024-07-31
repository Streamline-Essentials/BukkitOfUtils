package host.plas.bou;

import host.plas.bou.instances.BaseManager;
import host.plas.bou.owncmd.EntityCountCMD;
import lombok.Getter;
import lombok.Setter;

public class BukkitOfUtils extends BetterPlugin {
    @Getter @Setter
    private static BukkitOfUtils instance;

    public BukkitOfUtils() {
        super();
    }

    @Override
    public void onBaseEnabled() {
        // Plugin startup logic
//        instance = this; // Set earlier.
        new EntityCountCMD();
    }

    @Override
    public void onBaseDisable() {
        // Plugin shutdown logic
//        BaseManager.stop(); // Set earlier.
    }
}
