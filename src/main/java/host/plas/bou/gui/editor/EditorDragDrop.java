package host.plas.bou.gui.editor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * Helpers for editor inventories that accept cursor/shift/drag placement into editable slots.
 */
public final class EditorDragDrop {
    private EditorDragDrop() {
    }

    public static boolean isTopInventorySlot(InventoryClickEvent event, int rawSlot) {
        return rawSlot >= 0 && rawSlot < event.getView().getTopInventory().getSize();
    }

    public static ItemStack cursorItem(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) {
            return null;
        }
        return cursor;
    }

    public static boolean tryPlaceCursor(InventoryClickEvent event, int rawSlot, IntConsumer onPlace) {
        ItemStack cursor = cursorItem(event);
        if (cursor == null) {
            return false;
        }

        event.setCancelled(true);
        event.setCursor(null);
        onPlace.accept(rawSlot);
        return true;
    }

    public static boolean tryShiftMoveFromPlayer(InventoryClickEvent event, int rawSlot, IntConsumer onPlace) {
        if (event.getClickedInventory() instanceof PlayerInventory) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return false;
            }
            event.setCancelled(true);
            onPlace.accept(rawSlot);
            return true;
        }
        return false;
    }

    public static boolean isSwapFromPlayer(InventoryClickEvent event, int rawSlot) {
        if (!isTopInventorySlot(event, rawSlot)) {
            return false;
        }
        InventoryAction action = event.getAction();
        return action == InventoryAction.PLACE_ALL
                || action == InventoryAction.PLACE_ONE
                || action == InventoryAction.PLACE_SOME
                || action == InventoryAction.SWAP_WITH_CURSOR;
    }

    public static boolean handleDrag(InventoryDragEvent event, Set<Integer> editableSlots, IntConsumer onEachSlot) {
        Inventory top = event.getView().getTopInventory();
        Set<Integer> affectedTop = new HashSet<>();

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < top.getSize()) {
                affectedTop.add(rawSlot);
            }
        }

        if (affectedTop.isEmpty()) {
            return false;
        }

        for (int slot : affectedTop) {
            if (!editableSlots.contains(slot)) {
                event.setCancelled(true);
                return true;
            }
        }

        ItemStack source = event.getOldCursor();
        if (source == null || source.getType() == Material.AIR) {
            event.setCancelled(true);
            return true;
        }

        event.setCancelled(true);
        ItemStack remaining = source.clone();
        for (int slot : affectedTop) {
            if (remaining.getType() == Material.AIR) {
                break;
            }
            ItemStack placed = remaining.clone();
            placed.setAmount(1);
            top.setItem(slot, placed);
            onEachSlot.accept(slot);
            remaining.setAmount(remaining.getAmount() - 1);
        }

        event.setCursor(remaining.getAmount() > 0 ? remaining : null);
        return true;
    }

    public static void takeOneFromPlayerHand(Player player, ItemStack template) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            return;
        }
        if (template != null && hand.isSimilar(template)) {
            hand.setAmount(hand.getAmount() - 1);
            if (hand.getAmount() <= 0) {
                player.getInventory().setItemInMainHand(null);
            }
        }
    }
}
