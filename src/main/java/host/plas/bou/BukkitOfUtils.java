package host.plas.bou;

import host.plas.bou.instances.BaseManager;
import lombok.Getter;
import lombok.Setter;

public class BukkitOfUtils extends PluginBase {
    @Getter @Setter
    private static BukkitOfUtils instance;

    public BukkitOfUtils() {
        super();
    }

    @Override
    public void onBaseEnabling() {
        BaseManager.init(this);
    }

    @Override
    public void onBaseEnabled() {
        // Plugin startup logic
        instance = this;
    }

    @Override
    public void onBaseDisable() {
        // Plugin shutdown logic
        BaseManager.stop();
    }
}
