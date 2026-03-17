//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package host.plas.bou.gui;

import host.plas.bou.gui.icons.AirSlot;
import host.plas.bou.gui.slots.Slot;
import host.plas.bou.gui.slots.SlotType;
import host.plas.bou.helpful.data.HelpfulGui;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.obj.ManagedInventory;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a sheet of inventory slots used to build and manage GUI layouts.
 * Provides methods for setting, adding, removing, and iterating over slots.
 */
public class InventorySheet {
    private int size;
    private ConcurrentSkipListSet<Slot> slots;

    /**
     * Constructs a new InventorySheet with the given number of slots and initializes all slots as empty.
     *
     * @param slots the total number of slots in the sheet
     * @param icons optional initial icons (currently unused in constructor body)
     */
    public InventorySheet(int slots, Slot... icons) {
        this.size = slots;
        this.setAllEmpty();
    }

    /**
     * Ensures the internal slot set is initialized, optionally clearing it.
     *
     * @param clear if {@code true}, clears any existing slots after ensuring initialization
     */
    public void ensureSlots(boolean clear) {
        if (this.slots == null) {
            this.slots = new ConcurrentSkipListSet();
        } else if (clear) {
            this.slots.clear();
        }

    }

    /**
     * Ensures the internal slot set is initialized without clearing existing slots.
     */
    public void ensureSlots() {
        this.ensureSlots(false);
    }

    /**
     * Adds the given slots to the existing slot set without clearing.
     *
     * @param slots the slots to add
     */
    public void withSlots(Slot... slots) {
        this.ensureSlots();
        this.slots.addAll(Arrays.asList(slots));
    }

    /**
     * Replaces all existing slots with the given slots.
     *
     * @param slots the slots to set as the new contents
     */
    public void asSlots(Slot... slots) {
        this.ensureSlots(true);
        this.slots.addAll(Arrays.asList(slots));
    }

    /**
     * Sets an icon at the specified slot index, replacing any existing icon at that position.
     *
     * @param slot  the slot index
     * @param stack the item stack to place
     * @param type  the slot type classification
     */
    public void setIcon(int slot, ItemStack stack, SlotType type) {
        this.removeIcon(slot);
        this.slots.add(new Slot(slot, stack, type));
    }

    /**
     * Sets an icon at the specified slot index, replacing any existing icon at that position.
     *
     * @param slot the slot index
     * @param icon the icon to place
     */
    public void setIcon(int slot, Icon icon) {
        this.removeIcon(slot);
        this.slots.add(new Slot(slot, icon, SlotType.OTHER));
    }

    /**
     * Adds an icon at the specified slot index without removing existing icons at that position.
     *
     * @param slot  the slot index
     * @param stack the item stack to add
     * @param type  the slot type classification
     */
    public void addIcon(int slot, ItemStack stack, SlotType type) {
        this.slots.add(new Slot(slot, stack, type));
    }

    /**
     * Adds an icon at the specified slot index without removing existing icons at that position.
     *
     * @param slot the slot index
     * @param icon the icon to add
     */
    public void addIcon(int slot, Icon icon) {
        this.slots.add(new Slot(slot, icon, SlotType.OTHER));
    }

    /**
     * Sets a slot, replacing any existing slot at the same index.
     *
     * @param slot the slot to set, or {@code null} to do nothing
     */
    public void setSlot(Slot slot) {
        if (slot != null) {
            int index = slot.getIndex();
            this.removeIcon(index);
            this.addSlot(slot);
        }
    }

    /**
     * Adds a slot to the sheet without removing existing slots at the same index.
     *
     * @param slot the slot to add, or {@code null} to do nothing
     */
    public void addSlot(Slot slot) {
        if (slot != null) {
            this.slots.add(slot);
        }
    }

    /**
     * Removes any icon at the specified slot index.
     *
     * @param slot the slot index to clear
     */
    public void removeIcon(int slot) {
        this.slots.removeIf((s) -> s.getIndex() == slot);
    }

    /**
     * Retrieves the slot at the specified index.
     *
     * @param slot the slot index to look up
     * @return the {@link Slot} at the given index, or {@code null} if not found
     */
    public Slot getSlot(int slot) {
        AtomicReference<Slot> slotReference = new AtomicReference();
        this.slots.forEach((s) -> {
            if (s.getIndex() == slot) {
                slotReference.set(s);
            }

        });
        return (Slot)slotReference.get();
    }

    /**
     * Calculates the number of rows needed based on the total size.
     *
     * @return the number of rows (each row contains 9 slots)
     */
    public int getRows() {
        return (int)Math.ceil((double)this.size / (double)9.0F);
    }

    /**
     * Clears all slots and fills every position with an empty air slot.
     */
    public void setAllEmpty() {
        this.ensureSlots(true);

        for(int i = 0; i < this.size; ++i) {
            this.setSlot(AirSlot.get(i));
        }

    }

    /**
     * Iterates over all slots and applies the given consumer to each.
     *
     * @param consumer the consumer to apply to each slot
     */
    public void forEachSlot(Consumer<Slot> consumer) {
        this.slots.forEach(consumer);
    }

    /**
     * Creates an empty inventory sheet of the given size with all air slots.
     *
     * @param size the total number of slots
     * @return a new empty {@link InventorySheet}
     */
    public static InventorySheet empty(int size) {
        InventorySheet sheet = new InventorySheet(size, new Slot[0]);
        sheet.setAllEmpty();
        return sheet;
    }

    /**
     * Creates an inventory sheet from a managed inventory using the default slot type.
     *
     * @param inventory the managed inventory to convert
     * @return a new {@link InventorySheet} containing the inventory items
     */
    public static InventorySheet of(ManagedInventory inventory) {
        return of(inventory, SlotType.OTHER);
    }

    /**
     * Creates an inventory sheet from a managed inventory with the specified slot type.
     *
     * @param inventory the managed inventory to convert
     * @param type      the slot type to assign to each item
     * @return a new {@link InventorySheet} containing the inventory items
     */
    public static InventorySheet of(ManagedInventory inventory, SlotType type) {
        InventorySheet sheet = empty(inventory.size());

        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                sheet.setIcon(i, item, type);
            }
        }

        return sheet;
    }

    /**
     * Creates an inventory sheet from a helpful GUI's document pages, rendering each page as an item.
     *
     * @param gui        the helpful GUI containing document pages
     * @param startIndex the starting slot index for placing page items
     * @param material   the material to use for the page item icons
     * @return a new {@link InventorySheet} containing the page items
     */
    public static InventorySheet of(HelpfulGui gui, int startIndex, Material material) {
        InventorySheet sheet = empty(27);
        AtomicInteger index = new AtomicInteger(startIndex);
        AtomicInteger pageCount = new AtomicInteger(0);
        gui.getHelpful().getDocument().getPages().forEach((integer, textPage) -> {
            ItemStack stack = ItemUtils.make(material, "&e&lHint &b#&a" + pageCount.incrementAndGet(), textPage.asLore());
            Slot slot = new Slot(index.getAndIncrement(), stack, SlotType.STATIC);
            sheet.setSlot(slot);
        });
        return sheet;
    }

    /**
     * Returns the total number of slots in this sheet.
     *
     * @return the size of the sheet
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Returns the set of all slots in this sheet.
     *
     * @return the concurrent set of slots
     */
    public ConcurrentSkipListSet<Slot> getSlots() {
        return this.slots;
    }

    /**
     * Sets the total number of slots in this sheet.
     *
     * @param size the new size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Sets the slot set for this sheet.
     *
     * @param slots the new set of slots
     */
    public void setSlots(ConcurrentSkipListSet<Slot> slots) {
        this.slots = slots;
    }
}
