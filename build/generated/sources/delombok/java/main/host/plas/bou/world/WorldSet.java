package host.plas.bou.world;

import gg.drak.thebase.objects.Identifiable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class WorldSet implements Identifiable {
    private String identifier;
    private ConcurrentSkipListSet<String> worldNames;

    public WorldSet(String identifier, String... worldNames) {
        this.identifier = identifier;
        this.worldNames = new ConcurrentSkipListSet<>();
        if (worldNames.length > 0) {
            this.worldNames.addAll(Arrays.asList(worldNames));
        }
    }

    public WorldSet add(String... worldNames) {
        if (worldNames.length > 0) {
            this.worldNames.addAll(Arrays.asList(worldNames));
        }
        return this;
    }

    public WorldSet remove(String... worldNames) {
        for (String worldName : worldNames) {
            this.worldNames.remove(worldName);
        }
        return this;
    }

    public WorldSet clear() {
        this.worldNames.clear();
        return this;
    }

    public WorldSet withIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public World getDefaultWorld() {
        if (worldNames.isEmpty()) {
            return Bukkit.getWorlds().get(0); // Should return the MC server's default world
        } else {
            return filterByEnvironment(World.Environment.NORMAL).get(0);
        }
    }

    public List<World> asWorldList() {
        return worldNames.stream().map(Bukkit::getWorld).collect(Collectors.toList());
    }

    public List<World> filterByEnvironment(World.Environment environment) {
        return asWorldList().stream().filter(world -> world.getEnvironment() == environment).collect(Collectors.toList());
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public ConcurrentSkipListSet<String> getWorldNames() {
        return this.worldNames;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public void setWorldNames(final ConcurrentSkipListSet<String> worldNames) {
        this.worldNames = worldNames;
    }
}
