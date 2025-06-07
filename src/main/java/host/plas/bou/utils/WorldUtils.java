package host.plas.bou.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class WorldUtils {
    public static ConcurrentSkipListSet<String> getWorldNames() {
        ConcurrentSkipListSet<String> worlds = new ConcurrentSkipListSet<>();

        getWorlds().forEach((worldName, world) -> worlds.add(worldName));

        return worlds;
    }

    public static ConcurrentSkipListMap<String, ThreadLocal<World>> getWorlds() {
        ConcurrentSkipListMap<String, ThreadLocal<World>> worlds = new ConcurrentSkipListMap<>();

        Bukkit.getWorlds().forEach(world -> worlds.put(world.getName(), ThreadLocal.withInitial(() -> world)));

        return worlds;
    }
}
