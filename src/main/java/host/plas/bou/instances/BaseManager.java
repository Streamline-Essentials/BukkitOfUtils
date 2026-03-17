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
import lombok.Setter;
import lombok.Getter;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Central manager for the BukkitOfUtils framework, handling initialization,
 * configuration, and providing access to common server utilities such as
 * player lookups and world references.
 */
public class BaseManager {
    /**
     * Private constructor to prevent instantiation.
     */
    private BaseManager() {
        // utility class
    }

    /**
     * Returns the singleton instance of the BukkitOfUtils plugin.
     *
     * @return the BukkitOfUtils base instance
     */
    public static BukkitOfUtils getBaseInstance() {
        return BukkitOfUtils.getInstance();
    }

    /**
     * The base configuration for the BukkitOfUtils framework.
     * @param baseConfig the base configuration to set
     * @return the base configuration
     */
    @Setter
    private static BaseConfig baseConfig;

    /**
     * The main event listener for the BukkitOfUtils framework.
     * @param mainListener the main event listener to set
     * @return the main event listener
     */
    @Getter @Setter
    private static MainListener mainListener;

    /**
     * Returns the base configuration, ensuring it is initialized first.
     *
     * @return the base configuration
     */
    public static BaseConfig getBaseConfig() {
        ensureConfig();

        return baseConfig;
    }

    /**
     * Initializes the framework with the given BukkitOfUtils instance.
     * Performs pre-initialization, then sets up callbacks and version tools.
     *
     * @param baseInstance the BukkitOfUtils plugin instance
     */
    public static void init(BukkitOfUtils baseInstance) {
        preInit(baseInstance);

        CallbackManager.init();

        VersionTool.init();
    }

    /**
     * Performs pre-initialization by ensuring the configuration exists
     * and setting up the universal scheduler.
     *
     * @param baseInstance the BukkitOfUtils plugin instance
     */
    public static void preInit(BukkitOfUtils baseInstance) {
        ensureConfig();

        BetterPlugin.setScheduler(UniversalScheduler.getScheduler(baseInstance));
    }

    /**
     * Performs initialization tasks that should run when the plugin is enabled,
     * including setting up the inventory API, main listener, task manager,
     * and entity utilities.
     */
    public static void initOnEnabled() {
        new InventoryAPI(getBaseInstance()).init();

        mainListener = new MainListener();
        TaskManager.init();

        EntityUtils.init();
    }

    /**
     * Ensures that the base configuration is initialized, creating a new one if necessary.
     */
    public static void ensureConfig() {
        if (baseConfig == null) {
            setBaseConfig(new BaseConfig(BukkitOfUtils.getInstance()));
        }
    }

    /**
     * Initializes another plugin that depends on BukkitOfUtils by registering it
     * and setting up its inventory API.
     *
     * @param otherInstance the dependent plugin instance to initialize
     */
    public static void otherInit(BetterPlugin otherInstance) {
        PluginUtils.registerPlugin(otherInstance);

        new InventoryAPI(otherInstance).init();
    }

    /**
     * Stops the framework by cancelling the entity lookup timer and stopping the task manager.
     */
    public static void stop() {
        EntityUtils.getLookupTimer().cancel();
        TaskManager.stop();
    }

    /**
     * Returns the main (first) world on the server.
     *
     * @return the main world
     * @throws NullPointerException if the main world is null
     */
    public static World getMainWorld() {
        WeakReference<World> world = new WeakReference<>(Bukkit.getWorlds().get(0));
        if (world.get() == null) {
            throw new NullPointerException("Main world is null.");
        } else {
            return world.get();
        }
    }

    /**
     * Returns the console command sender.
     *
     * @return the console sender
     */
    public static CommandSender getConsole() {
        return Bukkit.getConsoleSender();
    }

    /**
     * Returns a list of all currently online players.
     *
     * @return a new list containing all online players
     */
    public static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    /**
     * Returns a map of all online players keyed by their UUID string.
     *
     * @return a sorted map of UUID strings to players
     */
    public static ConcurrentSkipListMap<String, Player> getOnlinePlayersByUUID() {
        ConcurrentSkipListMap<String, Player> players = new ConcurrentSkipListMap<>();
        for (Player player : getOnlinePlayers()) {
            players.put(player.getUniqueId().toString(), player);
        }

        return players;
    }

    /**
     * Returns a map of all online players keyed by their name.
     *
     * @return a sorted map of player names to players
     */
    public static ConcurrentSkipListMap<String, Player> getOnlinePlayersByName() {
        ConcurrentSkipListMap<String, Player> players = new ConcurrentSkipListMap<>();
        for (Player player : getOnlinePlayers()) {
            players.put(player.getName(), player);
        }

        return players;
    }

    /**
     * Finds an online player by their UUID string.
     *
     * @param uuid the UUID string to look up
     * @return the player with the given UUID, or null if not found
     */
    public static Player getPlayerByUUID(String uuid) {
        return getOnlinePlayersByUUID().get(uuid);
    }

    /**
     * Finds an online player by their name.
     *
     * @param name the player name to look up
     * @return the player with the given name, or null if not found
     */
    public static Player getPlayerByName(String name) {
        return getOnlinePlayersByName().get(name);
    }
}
