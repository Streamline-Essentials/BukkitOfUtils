package mc.obliviate.inventory;

import host.plas.bou.libs.usched.UniversalScheduler;
import host.plas.bou.libs.usched.scheduling.schedulers.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entry point for the inventory system: tracks which menu each player has open and
 * registers the listener that drives them.
 *
 * <p>BOU's own implementation of the OblivateInvs API. The published
 * {@code mc.obliviate:core} artifact is compiled to class file version 65.0 (Java 21) and
 * cannot load on the Java 11 runtimes BOU supports, so the API is reimplemented here at
 * the original package names — every dependent plugin keeps compiling unchanged.</p>
 */
public class InventoryAPI {
    private static InventoryAPI instance;
    private static TaskScheduler scheduler;

    private final JavaPlugin plugin;
    private final HashMap<UUID, Gui> players = new HashMap<>();
    private final Listener listener;
    private boolean initialized;

    /**
     * Constructing an instance replaces the one returned by {@link #getInstance()}, matching
     * the upstream behaviour that BaseManager relies on when several plugins each build one.
     *
     * @param plugin the plugin whose listener will drive the menus
     */
    public InventoryAPI(JavaPlugin plugin) {
        this.plugin = plugin;
        this.listener = new InvListener(this);
        instance = this;
    }

    /**
     * Registers the event listener. Calling this more than once is a no-op.
     */
    public void init() {
        if (initialized) return;

        Bukkit.getPluginManager().registerEvents(listener, plugin);
        initialized = true;
    }

    /**
     * Unregisters the listener and forgets all tracked menus.
     */
    public void unload() {
        HandlerList.unregisterAll(listener);
        players.clear();
        initialized = false;
    }

    /**
     * @return the tracked menus, keyed by player UUID
     */
    public HashMap<UUID, Gui> getPlayers() {
        return players;
    }

    /**
     * @param player the viewer
     * @return the menu the player has open, or null
     */
    public Gui getPlayersCurrentGui(Player player) {
        if (player == null) return null;
        return players.get(player.getUniqueId());
    }

    /**
     * @param inventory the inventory to look up
     * @return the menu backed by that inventory, or null
     */
    public Gui getGuiFromInventory(Inventory inventory) {
        if (inventory == null) return null;

        for (Map.Entry<UUID, Gui> entry : players.entrySet()) {
            Gui gui = entry.getValue();
            if (gui != null && inventory.equals(gui.getInventory())) {
                return gui;
            }
        }

        return null;
    }

    /**
     * The scheduler used for menu-bound tasks, resolved once against the current instance.
     *
     * @return a scheduler appropriate to the running platform
     */
    public static TaskScheduler getScheduler() {
        if (scheduler == null && instance != null) {
            scheduler = UniversalScheduler.getScheduler(instance.getPlugin());
        }
        return scheduler;
    }

    /**
     * @return the most recently constructed instance, or null before one exists
     */
    public static InventoryAPI getInstance() {
        return instance;
    }

    /**
     * @return the plugin backing this instance
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * @return the listener driving the menus
     */
    public Listener getListener() {
        return listener;
    }
}
