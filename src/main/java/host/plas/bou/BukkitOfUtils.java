package host.plas.bou;

import host.plas.bou.compat.CompatManager;
import host.plas.bou.firestring.FireStringManager;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.owncmd.EntityCountCMD;
import host.plas.bou.owncmd.MessageCMD;
import host.plas.bou.owncmd.TitleCMD;
import host.plas.bou.utils.ClassHelper;
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
        new MessageCMD();
        new TitleCMD();

        ClassHelper.init();
        ScreenManager.init();
        FireStringManager.init();

        CompatManager.init();
    }

    @Override
    public void onBaseDisable() {
        // Plugin shutdown logic
//        BaseManager.stop(); // Set earlier.
    }
}
