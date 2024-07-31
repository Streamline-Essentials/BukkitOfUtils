package host.plas.bou.instances;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import host.plas.bou.PluginBase;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.configs.BaseConfig;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.PluginUtils;
import lombok.Setter;
import lombok.Getter;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

public class BaseManager {
    @Getter @Setter
    private static PluginBase baseInstance;

    public static void init(PluginBase baseInstance) {
        setBaseInstance(baseInstance);

        PluginBase.setBaseConfig(new BaseConfig(baseInstance));

        PluginBase.setScheduler(UniversalScheduler.getScheduler(getBaseInstance()));

        new InventoryAPI(baseInstance).init();

        TaskManager.init();

        EntityUtils.init();
    }

    public static void otherInit(PluginBase baseInstance) {
        PluginUtils.registerPlugin(baseInstance);

        new InventoryAPI(baseInstance).init();
    }

    public static void stop() {
        TaskManager.stop();
    }

    public static World getMainWorld() {
        World world = Bukkit.getWorlds().get(0);
        if (world == null) {
            throw new NullPointerException("Main world is null.");
        } else {
            return world;
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
}
