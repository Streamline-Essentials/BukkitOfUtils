package host.plas.bou;

import host.plas.bou.bstats.BStats;
import host.plas.bou.compat.CompatManager;
import host.plas.bou.firestring.FireStringManager;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.owncmd.*;
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
        new FireStringCMD();
        new MessageCMD();
        new TitleCMD();

        ClassHelper.init();
        ScreenManager.init();
        FireStringManager.init();

        CompatManager.init();

        BStats.onEnable();
    }

    @Override
    public void onBaseDisable() {
        BStats.onDisable();

        BaseManager.stop();
    }
}
