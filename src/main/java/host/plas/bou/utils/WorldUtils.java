package host.plas.bou.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Utility class for retrieving world names and world references from the Bukkit server.
 */
public class WorldUtils {
    /** Private constructor to prevent instantiation of this utility class. */
    private WorldUtils() {}
    /**
     * Returns a sorted set of all world names on the server.
     *
     * @return a sorted set of world names
     */
    public static ConcurrentSkipListSet<String> getWorldNames() {
        ConcurrentSkipListSet<String> worlds = new ConcurrentSkipListSet<>();

        getWorlds().forEach((worldName, world) -> worlds.add(worldName));

        return worlds;
    }

    /**
     * Returns a map of world names to thread-local world references for all loaded worlds.
     *
     * @return a sorted map of world names to ThreadLocal-wrapped World instances
     */
    public static ConcurrentSkipListMap<String, ThreadLocal<World>> getWorlds() {
        ConcurrentSkipListMap<String, ThreadLocal<World>> worlds = new ConcurrentSkipListMap<>();

        Bukkit.getWorlds().forEach(world -> worlds.put(world.getName(), ThreadLocal.withInitial(() -> world)));

        return worlds;
    }
}
