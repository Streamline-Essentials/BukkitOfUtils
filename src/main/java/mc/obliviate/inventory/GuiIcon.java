package mc.obliviate.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * A single clickable entry in a {@link Gui}.
 *
 * <p>BOU's own implementation of the OblivateInvs API. The published
 * {@code mc.obliviate:core} artifact is compiled to class file version 65.0 (Java 21) and
 * cannot load on the Java 11 runtimes BOU supports, so the API is reimplemented here at
 * the original package names — every dependent plugin keeps compiling unchanged.</p>
 */
public interface GuiIcon {
    /**
     * @return the handler invoked when this icon is clicked, never null
     */
    Consumer<InventoryClickEvent> getClickAction();

    /**
     * @return the handler invoked when this icon is dragged, never null
     */
    Consumer<InventoryDragEvent> getDragAction();

    /**
     * @return the item shown in the inventory
     */
    ItemStack getItem();
}
