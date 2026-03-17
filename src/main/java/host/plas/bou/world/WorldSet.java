package host.plas.bou.world;

import gg.drak.thebase.objects.Identifiable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * Represents a named set of world names with utility methods for
 * world lookups and filtering by environment.
 */
@Getter @Setter
public class WorldSet implements Identifiable {
    /**
     * The unique identifier for this world set.
     *
     * @param identifier the identifier to set
     * @return the identifier
     */
    private String identifier;
    /**
     * The set of world names contained in this world set.
     *
     * @param worldNames the set of world names to set
     * @return the set of world names
     */
    private ConcurrentSkipListSet<String> worldNames;

    /**
     * Constructs a new WorldSet with the given identifier and optional world names.
     *
     * @param identifier the unique identifier for this world set
     * @param worldNames the world names to include in this set
     */
    public WorldSet(String identifier, String... worldNames) {
        this.identifier = identifier;
        this.worldNames = new ConcurrentSkipListSet<>();

        if (worldNames.length > 0) {
            this.worldNames.addAll(Arrays.asList(worldNames));
        }
    }

    /**
     * Adds world names to this set.
     *
     * @param worldNames the world names to add
     * @return this WorldSet for chaining
     */
    public WorldSet add(String... worldNames) {
        if (worldNames.length > 0) {
            this.worldNames.addAll(Arrays.asList(worldNames));
        }
        return this;
    }

    /**
     * Removes world names from this set.
     *
     * @param worldNames the world names to remove
     * @return this WorldSet for chaining
     */
    public WorldSet remove(String... worldNames) {
        for (String worldName : worldNames) {
            this.worldNames.remove(worldName);
        }
        return this;
    }

    /**
     * Clears all world names from this set.
     *
     * @return this WorldSet for chaining
     */
    public WorldSet clear() {
        this.worldNames.clear();
        return this;
    }

    /**
     * Sets the identifier for this world set.
     *
     * @param identifier the new identifier
     * @return this WorldSet for chaining
     */
    public WorldSet withIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * Returns the default world from this set, preferring NORMAL environment worlds.
     * If the set is empty, returns the server's default world.
     *
     * @return the default world
     */
    public World getDefaultWorld() {
        if (worldNames.isEmpty()) {
            return Bukkit.getWorlds().get(0); // Should return the MC server's default world
        } else {
            return filterByEnvironment(World.Environment.NORMAL).get(0);
        }
    }

    /**
     * Converts this set's world names into a list of World instances.
     *
     * @return a list of World instances corresponding to the world names in this set
     */
    public List<World> asWorldList() {
        return worldNames.stream()
                .map(Bukkit::getWorld)
                .collect(Collectors.toList());
    }

    /**
     * Filters the worlds in this set by the specified environment type.
     *
     * @param environment the environment type to filter by
     * @return a list of worlds matching the specified environment
     */
    public List<World> filterByEnvironment(World.Environment environment) {
        return asWorldList().stream().filter(world -> world.getEnvironment() == environment).collect(Collectors.toList());
    }
}
