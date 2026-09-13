package mc.obliviate.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.entity.Player;

/**
 * Routes Bukkit inventory events to the {@link Gui} the viewer currently has open.
 *
 * <p>BOU's own implementation of the OblivateInvs API. The published
 * {@code mc.obliviate:core} artifact is compiled to class file version 65.0 (Java 21) and
 * cannot load on the Java 11 runtimes BOU supports, so the API is reimplemented here at
 * the original package names.</p>
 *
 * <p>The upstream {@code GuiPre*} wrapper events are not reproduced: nothing in BOU or the
 * plugins built on it listened for them, so events are dispatched straight to the menu.</p>
 */
public class InvListener implements Listener {
    private final InventoryAPI inventoryAPI;

    /**
     * @param inventoryAPI the API instance owning this listener
     */
    protected InvListener(InventoryAPI inventoryAPI) {
        this.inventoryAPI = inventoryAPI;
    }

    /**
     * Cancels clicks as the menu dictates, then runs the clicked icon's handler.
     *
     * @param event the click
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (! (event.getWhoClicked() instanceof Player)) return;

        Gui gui = inventoryAPI.getPlayersCurrentGui((Player) event.getWhoClicked());
        if (gui == null) return;

        // A Gui returning true from onClick forces the click to be uncancelled; returning
        // false keeps the default protection.
        boolean doNotProtect = gui.onClick(event);
        int rawSlot = event.getRawSlot();

        if (doNotProtect) {
            event.setCancelled(false);
        } else if (event.getSlot() == rawSlot) {
            // The click landed in the menu itself.
            event.setCancelled(true);
        } else {
            // The click landed in the player's own inventory; only block the actions that
            // could pull items out of, or shuffle items into, the menu.
            InventoryAction action = event.getAction();
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                    || action == InventoryAction.COLLECT_TO_CURSOR
                    || action == InventoryAction.UNKNOWN) {
                event.setCancelled(true);
            }
        }

        GuiIcon icon = gui.getItems().get(rawSlot);
        if (icon == null) return;

        icon.getClickAction().accept(event);
    }

    /**
     * Runs the menu's close hook and forgets the viewer.
     *
     * @param event the close event
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (! (event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        Gui gui = inventoryAPI.getPlayersCurrentGui(player);
        if (gui == null) return;

        // Ignore a close for some other inventory the player happens to have open.
        if (! event.getInventory().equals(gui.getInventory())) return;

        gui.onClose(event);
        gui.setClosed(true);
        inventoryAPI.getPlayers().remove(player.getUniqueId());
    }

    /**
     * Cancels drags as the menu dictates, then runs each affected icon's handler.
     *
     * @param event the drag
     */
    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (! (event.getWhoClicked() instanceof Player)) return;

        Gui gui = inventoryAPI.getPlayersCurrentGui((Player) event.getWhoClicked());
        if (gui == null) return;

        // If the menu forces an uncancel, uncancel; otherwise cancel.
        event.setCancelled(! gui.onDrag(event));

        // The first slot without an icon ends the dispatch: a drag that touches any empty
        // slot fires no further drag handlers, which is the contract dependent menus expect.
        for (int rawSlot : event.getRawSlots()) {
            GuiIcon icon = gui.getItems().get(rawSlot);
            if (icon == null) return;

            icon.getDragAction().accept(event);
        }
    }

    /**
     * Runs the menu's open hook.
     *
     * <p>Listens at {@link EventPriority#MONITOR} so the menu builds itself only after every
     * other plugin has had its say on the open, including any decision to cancel it.</p>
     *
     * @param event the open event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onOpen(InventoryOpenEvent event) {
        if (! (event.getPlayer() instanceof Player)) return;

        Gui gui = inventoryAPI.getPlayersCurrentGui((Player) event.getPlayer());
        if (gui == null) return;

        if (event.isCancelled()) return;

        gui.onOpen(event);
    }
}
