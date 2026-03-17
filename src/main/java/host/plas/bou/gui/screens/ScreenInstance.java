package host.plas.bou.gui.screens;

import host.plas.bou.gui.GuiType;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.screens.blocks.ScreenBlock;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.ColorUtils;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Gui;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import gg.drak.thebase.objects.Identified;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Represents a GUI screen instance tied to a specific player. Extends {@link Gui}
 * and provides inventory sheet management, click handling, and screen block association.
 */
@Getter @Setter
public class ScreenInstance extends Gui implements Identified {
    /**
     * The unique identifier for this screen instance, derived from the player's UUID.
     *
     * @return the unique identifier string
     */
    private final String identifier;

    /**
     * The GUI type that defines this screen's appearance and behavior.
     *
     * @param type the GUI type to set
     * @return the GUI type
     */
    private GuiType type;

    /**
     * The optional screen block associated with this screen instance.
     *
     * @param screenBlock the screen block optional to set
     * @return an Optional containing the associated screen block, or empty if none
     */
    private Optional<ScreenBlock> screenBlock;

    /**
     * The inventory sheet defining the layout and icons for this screen.
     *
     * @param inventorySheet the inventory sheet to set
     * @return the inventory sheet
     */
    private InventorySheet inventorySheet;

    /**
     * Whether placing items in this GUI is disallowed.
     *
     * @param noPlace {@code true} to prevent item placement
     * @return {@code true} if item placement is disallowed
     */
    private boolean noPlace;

    /**
     * Constructs a new ScreenInstance for the given player with the specified type, sheet, and placement setting.
     *
     * @param player         the player who owns this screen
     * @param type           the GUI type
     * @param inventorySheet the inventory sheet defining the screen layout
     * @param noPlace        if {@code true}, prevents the player from placing items in the GUI
     */
    public ScreenInstance(@NotNull Player player, GuiType type, InventorySheet inventorySheet, boolean noPlace) {
        super(player, type.name(), ColorUtils.colorizeHard(getTitleByType(type)), inventorySheet.getRows());

        this.identifier = player.getUniqueId().toString();

        this.type = type;
        this.screenBlock = Optional.empty();
        this.inventorySheet = inventorySheet;
        this.noPlace = noPlace;
    }

    /**
     * Constructs a new ScreenInstance with item placement disabled by default.
     *
     * @param player         the player who owns this screen
     * @param type           the GUI type
     * @param inventorySheet the inventory sheet defining the screen layout
     */
    public ScreenInstance(@NotNull Player player, GuiType type, InventorySheet inventorySheet) {
        this(player, type, inventorySheet, true);
    }

    /**
     * Associates this screen instance with a screen block.
     *
     * @param block the screen block to associate
     */
    public void setBlock(ScreenBlock block) {
        this.screenBlock = Optional.of(block);
    }

    /**
     * Retrieves the display title for the given GUI type.
     *
     * @param type the GUI type
     * @return the title string
     */
    public static String getTitleByType(GuiType type) {
        return type.getTitle();
    }

    /**
     * Updates the inventory size of this screen.
     *
     * @param size the new size (number of slots)
     */
    public void updateSize(int size) {
        super.setSize(size);
    }

    /**
     * Updates the display title of this screen.
     *
     * @param title the new title
     */
    public void updateTitle(String title) {
        super.setTitle(title);
    }

    /**
     * Populates this GUI with icons from the given inventory sheet.
     *
     * @param sheet the inventory sheet containing slots to render
     */
    public void build(InventorySheet sheet) {
        sheet.getSlots().forEach(s -> {
            addItem(s.getIndex(), s.getIcon());
        });
    }

    /**
     * Returns the player who owns this screen instance.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        super.onClose(event);

        getScreenBlock().ifPresent(block -> block.onClose((Player) event.getPlayer()));
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        build(inventorySheet);

        ScreenManager.setScreen(player, this);
    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (! (event.getWhoClicked() instanceof Player)) return false;
        Player p = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        Inventory clickedInventory = event.getClickedInventory();
        Inventory playerInventory = p.getInventory();
        if (clickedInventory == null || playerInventory == null) return false;

        boolean isPlace = false;
        InventoryAction action = event.getAction();

        if (clickedInventory.equals(playerInventory)) {
            if (
                    action == InventoryAction.MOVE_TO_OTHER_INVENTORY
            ) {
                isPlace = true;
            }
        } else {

            if (
                    action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ONE_CURSOR ||
                            action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE ||
                            action == InventoryAction.PLACE_SOME
            ) {
                isPlace = true;
            }

            if (!isPlace) {
                if (cursor.getType() != Material.AIR) {
                    if (action == InventoryAction.SWAP_WITH_CURSOR) {
                        isPlace = true;
                    }
                }
            }
        }

        if (isPlace && noPlace) {
            event.setCancelled(true);
            return false; // No force uncancel as we are cancelling the event
        }

        return furtherClick(event);
    }

    /**
     * Hook method for subclasses to handle additional click logic after the default handling.
     *
     * @param event the inventory click event
     * @return {@code true} to force uncancel the event, {@code false} otherwise
     */
    public boolean furtherClick(InventoryClickEvent event) {
        // do more stuff
        return false;
    }

    /**
     * Closes the inventory for the player.
     */
    public void close() {
        player.closeInventory();
    }

    /**
     * Redraws the screen by rebuilding and reshowing the inventory.
     */
    public void redraw() {
        redraw(false);
    }

    /**
     * Redraws the screen with an option to close before reshowing.
     *
     * @param close if {@code true}, closes the inventory before reshowing
     */
    public void redraw(boolean close) {
        redraw(true, close);
    }

    /**
     * Redraws the screen by rebuilding from the inventory sheet, with options to reshow and close.
     *
     * @param reshow if {@code true}, reopens the inventory after rebuilding
     * @param close  if {@code true} and reshow is {@code true}, closes the inventory before reopening
     */
    public void redraw(boolean reshow, boolean close) {
        build(inventorySheet);

        if (reshow) reshow(close);
    }

    /**
     * Reshows the inventory to the player without closing it first.
     */
    public void reshow() {
        reshow(false);
    }

    /**
     * Reshows the inventory to the player, optionally closing it first.
     * If not on the main thread, reschedules to run synchronously.
     *
     * @param close if {@code true}, closes the inventory before reopening
     */
    public void reshow(boolean close) {
        if (! TaskManager.isThreadSync()) {
            TaskManager.runTask(player, this::reshow);
            return;
        }

        if (close) close();
        open();
    }

    /**
     * Returns a map of all human entities currently viewing this screen's inventory,
     * keyed by their UUID string.
     *
     * @return a sorted map of UUID strings to {@link HumanEntity} viewers
     */
    public ConcurrentSkipListMap<String, HumanEntity> getViewers() {
        ConcurrentSkipListMap<String, HumanEntity> map = new ConcurrentSkipListMap<>();
        List<HumanEntity> viewers = new ArrayList<>(getInventory().getViewers());

        viewers.forEach(v -> {
            if (v instanceof Player) {
                map.put(v.getUniqueId().toString(), v);
            }
        });

        return map;
    }
}
