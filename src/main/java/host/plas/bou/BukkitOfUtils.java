package host.plas.bou;

import host.plas.bou.compat.CompatManager;
import host.plas.bou.firestring.FireStringManager;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.owncmd.DebugCMD;
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
    public void onLoad() {
        setInstance(this);

        BaseManager.init(this);
    }

    @Override
    public void onBaseEnabled() {
        BaseManager.initOnEnabled();

        // Plugin startup logic
//        instance = this; // Set earlier.
        new DebugCMD();
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
        BaseManager.stop();
    }
}
