package mc.obliviate.inventory.event.customclosevent;

import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;

/**
 * A close event fired by the API rather than the server.
 *
 * <p>Bukkit does not fire {@link InventoryCloseEvent} when one inventory is replaced by
 * another, so the API synthesises this so a menu can still run its cleanup.</p>
 *
 * <p>BOU's own implementation of the OblivateInvs API, kept at the original package name
 * so dependent plugins keep compiling unchanged.</p>
 */
public class FakeInventoryCloseEvent extends InventoryCloseEvent {
    /**
     * @param view the view being closed
     */
    public FakeInventoryCloseEvent(InventoryView view) {
        super(view);
    }
}
