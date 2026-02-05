package host.plas.bou.instances;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.events.MainListener;
import host.plas.bou.events.callbacks.CallbackManager;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.configs.BaseConfig;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.PluginUtils;
import host.plas.bou.utils.VersionTool;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

public class BaseManager {
    public static BukkitOfUtils getBaseInstance() {
        return BukkitOfUtils.getInstance();
    }

    private static BaseConfig baseConfig;
    private static MainListener mainListener;

    public static BaseConfig getBaseConfig() {
        ensureConfig();
        return baseConfig;
    }

    public static void init(BukkitOfUtils baseInstance) {
        preInit(baseInstance);
        CallbackManager.init();
        VersionTool.init();
    }

    public static void preInit(BukkitOfUtils baseInstance) {
        ensureConfig();
        BetterPlugin.setScheduler(UniversalScheduler.getScheduler(baseInstance));
    }

    public static void initOnEnabled() {
        new InventoryAPI(getBaseInstance()).init();
        mainListener = new MainListener();
        TaskManager.init();
        EntityUtils.init();
    }

    public static void ensureConfig() {
        if (baseConfig == null) {
            setBaseConfig(new BaseConfig(BukkitOfUtils.getInstance()));
        }
    }

    public static void otherInit(BetterPlugin otherInstance) {
        PluginUtils.registerPlugin(otherInstance);
        new InventoryAPI(otherInstance).init();
    }

    public static void stop() {
        EntityUtils.getLookupTimer().cancel();
        TaskManager.stop();
    }

    public static World getMainWorld() {
        WeakReference<World> world = new WeakReference<>(Bukkit.getWorlds().get(0));
        if (world.get() == null) {
            throw new NullPointerException("Main world is null.");
        } else {
            return world.get();
        }
    }

    public static CommandSender getConsole() {
        return Bukkit.getConsoleSender();
    }

    public static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public static ConcurrentSkipListMap<String, Player> getOnlinePlayersByUUID() {
        ConcurrentSkipListMap<String, Player> players = new ConcurrentSkipListMap<>();
        for (Player player : getOnlinePlayers()) {
            players.put(player.getUniqueId().toString(), player);
        }
        return players;
    }

    public static ConcurrentSkipListMap<String, Player> getOnlinePlayersByName() {
        ConcurrentSkipListMap<String, Player> players = new ConcurrentSkipListMap<>();
        for (Player player : getOnlinePlayers()) {
            players.put(player.getName(), player);
        }
        return players;
    }

    public static Player getPlayerByUUID(String uuid) {
        return getOnlinePlayersByUUID().get(uuid);
    }

    public static Player getPlayerByName(String name) {
        return getOnlinePlayersByName().get(name);
    }

    public static void setBaseConfig(final BaseConfig baseConfig) {
        BaseManager.baseConfig = baseConfig;
    }

    public static MainListener getMainListener() {
        return BaseManager.mainListener;
    }

    public static void setMainListener(final MainListener mainListener) {
        BaseManager.mainListener = mainListener;
    }
}
