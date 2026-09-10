package host.plas.bou.gui;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.gui.slots.Slot;
import host.plas.bou.gui.slots.SlotType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Bukkit event listener that prevents players from moving button and static items
 * within GUI inventories by cancelling click events on those items.
 *
 * <p>Protection is resolved from two independent sources, in order:</p>
 *
 * <ol>
 *     <li>The open {@link ScreenInstance}'s {@link InventorySheet}, which records a
 *     {@link SlotType} per slot in memory. This works on every supported server version and
 *     needs no item metadata.</li>
 *     <li>The item's persistent data container markers, which additionally protect tagged
 *     items that were placed outside a managed screen. This source is only consulted on
 *     servers that actually have the PDC API (1.14+).</li>
 * </ol>
 *
 * <p>The in-memory source is checked first because it is both cheaper and version-independent;
 * the metadata source exists only to catch stacks the sheet does not know about.</p>
 */
public class GuiMaintenanceListener implements Listener {
    /**
     * Constructs a new GuiMaintenanceListener and registers it with the Bukkit plugin manager.
     */
    public GuiMaintenanceListener() {
        Bukkit.getPluginManager().registerEvents(this, BukkitOfUtils.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (! (event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (item.getType() == Material.AIR) return;

        if (isProtectedSlot(player, event)) {
            event.setCancelled(true);
            return;
        }

        if (MenuUtils.isButton(item) || MenuUtils.isStatic(item)) {
            event.setCancelled(true);
        }
    }

    /**
     * Resolves whether the clicked slot is a protected slot of the player's open screen.
     *
     * <p>Only clicks inside the screen's own inventory are considered — a click in the
     * player's inventory shares the same slot numbering space and must not be matched against
     * the sheet.</p>
     *
     * @param player the clicking player
     * @param event  the click event
     * @return {@code true} when the slot is a static or button slot of the open screen
     */
    private boolean isProtectedSlot(Player player, InventoryClickEvent event) {
        Optional<ScreenInstance> screen = ScreenManager.getScreen(player);
        if (! screen.isPresent()) return false;

        if (! event.getClickedInventory().equals(event.getView().getTopInventory())) return false;

        InventorySheet sheet = screen.get().getInventorySheet();
        if (sheet == null) return false;

        int slotIndex = event.getSlot();
        if (slotIndex < 0 || slotIndex >= sheet.getSize()) return false;

        Slot slot = sheet.getSlot(slotIndex);
        if (slot == null) return false;

        SlotType type = slot.getType();
        return type == SlotType.STATIC || type == SlotType.BUTTON;
    }
}
