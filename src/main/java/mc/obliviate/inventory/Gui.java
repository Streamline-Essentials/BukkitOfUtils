package mc.obliviate.inventory;

import host.plas.bou.libs.usched.scheduling.tasks.MyScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import mc.obliviate.inventory.event.customclosevent.FakeInventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class for a menu backed by a Bukkit inventory.
 *
 * <p>BOU's own implementation of the OblivateInvs API. The published
 * {@code mc.obliviate:core} artifact is compiled to class file version 65.0 (Java 21) and
 * cannot load on the Java 11 runtimes BOU supports, so the API is reimplemented here at
 * the original package names — every dependent plugin keeps compiling unchanged.</p>
 *
 * <p>Titles are plain {@link String}s rather than Adventure components: Adventure is not
 * shaded into BOU and is absent from 1.8 servers, and the {@code Bukkit.createInventory}
 * overload taking a component only exists on Paper 1.16+.</p>
 */
public abstract class Gui implements InventoryHolder {
    /** 1.8 refuses to open an inventory whose title exceeds 32 characters. */
    private static final int LEGACY_TITLE_LIMIT = 32;

    private final Map<Integer, GuiIcon> registeredIcons = new HashMap<>();
    private final List<MyScheduledTask> taskList = new ArrayList<>();
    private final String id;
    private final InventoryType inventoryType;

    /** The viewer this menu belongs to. Public to match the original API. */
    public final Player player;

    private Inventory inventory;
    private String title;
    private int size;
    private boolean isClosed;

    /**
     * Creates a chest-style menu.
     *
     * @param player the viewer
     * @param id     an identifier for this menu
     * @param title  the inventory title, colour codes already translated
     * @param rows   the number of rows
     */
    public Gui(Player player, String id, String title, int rows) {
        this.player = player;
        this.id = id;
        this.title = title;
        this.size = rows * 9;
        this.inventoryType = InventoryType.CHEST;
    }

    /**
     * Creates a menu using one of the built-in inventory shapes.
     *
     * @param player        the viewer
     * @param id            an identifier for this menu
     * @param title         the inventory title, colour codes already translated
     * @param inventoryType the inventory shape
     */
    public Gui(Player player, String id, String title, InventoryType inventoryType) {
        this.player = player;
        this.id = id;
        this.title = title;
        this.inventoryType = inventoryType;
        this.size = 0;
    }

    /**
     * @return the plugin that owns the inventory API
     */
    public Plugin getPlugin() {
        return InventoryAPI.getInstance().getPlugin();
    }

    /**
     * Called when the viewer clicks a slot, before the clicked icon's handler runs.
     *
     * @param event the click
     * @return true to keep the default protection (clicks in the menu are cancelled);
     *         false to let the player move items freely
     */
    public boolean onClick(InventoryClickEvent event) {
        return true;
    }

    /**
     * Called when the viewer drags across slots, before the icons' handlers run.
     *
     * @param event the drag
     * @return true to cancel the drag, false to allow it
     */
    public boolean onDrag(InventoryDragEvent event) {
        return true;
    }

    /**
     * Called after the inventory is shown to the viewer.
     *
     * @param event the open event
     */
    public void onOpen(InventoryOpenEvent event) {
    }

    /**
     * Called when the inventory is closed. Overrides must call {@code super} so this menu's
     * scheduled tasks are cancelled; skipping it leaks every task started by
     * {@link #updateTask} or {@link #runTaskLater}.
     *
     * @param event the close event
     */
    public void onClose(InventoryCloseEvent event) {
        // A synthetic close means this menu is being replaced by another, not torn down,
        // so its tasks must survive.
        if (event instanceof FakeInventoryCloseEvent) return;

        InventoryAPI api = InventoryAPI.getInstance();
        if (api == null) return;

        // Only clean up when the inventory actually closing is the one backing this menu.
        Gui closing = api.getGuiFromInventory(event.getPlayer().getOpenInventory().getTopInventory());
        if (closing == null) return;
        if (! closing.equals(this)) return;

        stopAllTasks();
    }

    /**
     * Builds the inventory if needed and shows it to the viewer. Also used to apply a new
     * title or size, since Bukkit cannot change either in place on legacy servers.
     */
    public void open() {
        InventoryAPI api = InventoryAPI.getInstance();
        if (api == null) {
            throw new IllegalStateException("InventoryAPI is not initialized");
        }

        // Reopening over an existing menu must let the old one clean up first. The real
        // close event does not fire for a replaced inventory, so synthesise one.
        if (api.getPlayersCurrentGui(player) != null) {
            Bukkit.getPluginManager().callEvent(new FakeInventoryCloseEvent(player.getOpenInventory()));
        }

        api.getPlayers().put(player.getUniqueId(), this);

        isClosed = false;
        inventory = inventoryType == InventoryType.CHEST
                ? createInventory(this, size, title)
                : createInventory(this, inventoryType, title);

        for (Map.Entry<Integer, GuiIcon> entry : registeredIcons.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, entry.getValue().getItem());
            }
        }

        player.openInventory(inventory);
    }

    /**
     * Fills every slot with the given icon.
     *
     * @param icon the icon to place
     */
    public void fillGui(GuiIcon icon) {
        for (int slot = 0; slot <= getLastSlot(); slot++) {
            addItem(slot, icon);
        }
    }

    /**
     * @param item the item to place in every slot
     */
    public void fillGui(ItemStack item) {
        fillGui(new Icon(item));
    }

    /**
     * @param material the material to place in every slot
     */
    public void fillGui(Material material) {
        fillGui(new Icon(material));
    }

    /**
     * @param icon  the icon to place
     * @param slots the slots to fill
     */
    public void fillGui(GuiIcon icon, Iterable<Integer> slots) {
        for (int slot : slots) {
            addItem(slot, icon);
        }
    }

    /**
     * @param icon the icon to place
     * @param row  the zero-based row
     */
    public void fillRow(GuiIcon icon, int row) {
        for (int i = 0; i < 9; i++) {
            addItem(row * 9 + i, icon);
        }
    }

    /**
     * @param icon   the icon to place
     * @param column the zero-based column
     */
    public void fillColumn(GuiIcon icon, int column) {
        for (int row = 0; row * 9 + column <= getLastSlot(); row++) {
            addItem(row * 9 + column, icon);
        }
    }

    /**
     * Registers an icon at a slot, updating the open inventory if there is one.
     *
     * @param slot the slot index
     * @param icon the icon to place
     */
    public void addItem(int slot, GuiIcon icon) {
        registeredIcons.put(slot, icon);
        if (inventory != null && slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, icon.getItem());
        }
    }

    /**
     * @param icon  the icon to place
     * @param slots the slots to place it in
     */
    public void addItem(GuiIcon icon, Integer... slots) {
        for (int slot : slots) {
            addItem(slot, icon);
        }
    }

    /**
     * @param icon  the icon to place
     * @param slots the slots to place it in
     */
    public void addItem(GuiIcon icon, Iterable<Integer> slots) {
        for (int slot : slots) {
            addItem(slot, icon);
        }
    }

    /**
     * @param slot the slot index
     * @param item the item to place
     */
    public void addItem(int slot, ItemStack item) {
        addItem(slot, new Icon(item));
    }

    /**
     * @param slot     the slot index
     * @param material the material to place
     */
    public void addItem(int slot, Material material) {
        addItem(slot, new Icon(material));
    }

    /**
     * Places an icon in the first slot that has no icon registered.
     *
     * @param icon the icon to place
     */
    public void addItem(GuiIcon icon) {
        for (int slot = 0; slot <= getLastSlot(); slot++) {
            if (! registeredIcons.containsKey(slot)) {
                addItem(slot, icon);
                return;
            }
        }
    }

    /**
     * @param item the item to place in the first free slot
     */
    public void addItem(ItemStack item) {
        addItem(new Icon(item));
    }

    /**
     * @param material the material to place in the first free slot
     */
    public void addItem(Material material) {
        addItem(new Icon(material));
    }

    /**
     * Runs a repeating task tied to this menu. The task is cancelled when the menu closes.
     *
     * @param delay    initial delay in ticks
     * @param period   period in ticks
     * @param consumer receives the scheduled task so it can cancel itself
     */
    public void updateTask(long delay, long period, Consumer<MyScheduledTask> consumer) {
        // The task must be able to reference itself, so hand the consumer a holder that is
        // populated as soon as the scheduler returns.
        MyScheduledTask[] holder = new MyScheduledTask[1];
        holder[0] = InventoryAPI.getScheduler().runTaskTimer(() -> consumer.accept(holder[0]), delay, period);
        taskList.add(holder[0]);
    }

    /**
     * Runs a delayed task tied to this menu. The task is cancelled when the menu closes.
     *
     * @param delay    delay in ticks
     * @param consumer receives the scheduled task
     */
    public void runTaskLater(long delay, Consumer<MyScheduledTask> consumer) {
        MyScheduledTask[] holder = new MyScheduledTask[1];
        holder[0] = InventoryAPI.getScheduler().runTaskLater(() -> consumer.accept(holder[0]), delay);
        taskList.add(holder[0]);
    }

    /**
     * @return the registered icons, keyed by slot
     */
    public Map<Integer, GuiIcon> getItems() {
        return registeredIcons;
    }

    /**
     * @return this menu's identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * @return the inventory title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title without refreshing an open inventory.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Applies a new title by rebuilding and reopening the inventory. Bukkit offers no way
     * to retitle an open inventory on legacy servers.
     *
     * @param title the new title
     */
    public void sendTitleUpdate(String title) {
        this.title = title;
        open();
    }

    /**
     * Applies a new size by rebuilding and reopening the inventory.
     *
     * @param size the new slot count
     */
    public void sendSizeUpdate(int size) {
        this.size = size;
        open();
    }

    /**
     * @return the configured slot count
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the highest usable slot index
     */
    public int getLastSlot() {
        return size - 1;
    }

    /**
     * @param size the slot count, applied on the next open
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return true if the menu has been closed
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * @param closed the new closed state
     */
    public void setClosed(boolean closed) {
        this.isClosed = closed;
    }

    /**
     * @return the tasks tied to this menu
     */
    public List<MyScheduledTask> getTaskList() {
        return taskList;
    }

    /**
     * Cancels one task and stops tracking it.
     *
     * @param task the task to cancel
     */
    public void stopTask(MyScheduledTask task) {
        if (task == null) return;
        try {
            task.cancel();
        } catch (Throwable ignored) {
            // A task that is already gone is not an error worth surfacing.
        }
        taskList.remove(task);
    }

    /**
     * Cancels every task tied to this menu.
     */
    public void stopAllTasks() {
        for (MyScheduledTask task : new ArrayList<>(taskList)) {
            stopTask(task);
        }
        taskList.clear();
    }

    /**
     * Creates a chest-style inventory, clamping the title to what legacy servers accept.
     *
     * @param holder the holder to attach
     * @param size   the slot count
     * @param title  the title
     * @return the new inventory
     */
    protected Inventory createInventory(InventoryHolder holder, int size, String title) {
        return Bukkit.createInventory(holder, size, clampTitle(title));
    }

    /**
     * Creates a typed inventory, clamping the title to what legacy servers accept.
     *
     * @param holder the holder to attach
     * @param type   the inventory shape
     * @param title  the title
     * @return the new inventory
     */
    protected Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return Bukkit.createInventory(holder, type, clampTitle(title));
    }

    /**
     * 1.8 throws when a title exceeds 32 characters, so truncate rather than fail to open.
     *
     * @param title the requested title
     * @return a title the server will accept
     */
    private static String clampTitle(String title) {
        if (title == null) return "";
        return title.length() > LEGACY_TITLE_LIMIT ? title.substring(0, LEGACY_TITLE_LIMIT) : title;
    }

    /**
     * @param inventory the backing inventory
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
