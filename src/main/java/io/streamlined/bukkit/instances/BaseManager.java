package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.BukkitBase;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

public class BaseManager {
    @Getter @Setter
    private static BukkitBase baseInstance;

    public static void init(BukkitBase baseInstance) {
        setBaseInstance(baseInstance);
        new InventoryAPI(baseInstance).init();
    }

    public static CommandSender getConsole() {
        return getBaseInstance().getServer().getConsoleSender();
    }

    public static List<Player> getOnlinePlayers() {
        return new ArrayList<>(getBaseInstance().getServer().getOnlinePlayers());
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
