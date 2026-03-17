package host.plas.bou.utils.obj;

import host.plas.bou.items.InventoryUtils;
import host.plas.bou.items.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents a managed inventory with indexed item slots, providing operations
 * for item manipulation, copying, trimming, and iteration.
 */
@Getter @Setter
public class ManagedInventory implements Idable {
    /**
     * The global atomic counter used to assign unique IDs to ManagedInventory instances.
     *
     * @param currentId the atomic counter for generating IDs
     * @return the current atomic ID counter
     */
    private static AtomicLong currentId = new AtomicLong(0);

    /**
     * Returns the next unique ID for a new ManagedInventory instance.
     *
     * @return the next available ID
     */
    public static long getNextId() {
        return currentId.getAndIncrement();
    }

    /**
     * The unique identifier for this managed inventory instance.
     *
     * @return the unique ID of this inventory
     */
    private final long id;

    /**
     * The maximum number of items this inventory can hold.
     *
     * @param maxItems the maximum item capacity
     * @return the maximum item capacity
     */
    private int maxItems;
    /**
     * The map of slot indices to item stacks in this inventory.
     *
     * @param slots the slot-to-item mapping
     * @return the slot-to-item mapping
     */
    private ConcurrentSkipListMap<Integer, ItemStack> slots;

    /**
     * Whether this inventory is ready for use.
     *
     * @param ready true if the inventory is ready
     * @return true if the inventory is ready
     */
    private boolean ready;

    /**
     * Constructs a new ManagedInventory with the specified maximum item capacity.
     *
     * @param maxItems the maximum number of items this inventory can hold
     */
    public ManagedInventory(int maxItems) {
        id = getNextId();

        this.maxItems = maxItems;
        this.slots = new ConcurrentSkipListMap<>();

        ready = false;
    }

    /**
     * Returns the number of free (unoccupied) slots in this inventory.
     *
     * @return the number of free slots
     */
    public int getFreeSlots() {
        return maxItems - slots.size();
    }

    /**
     * Checks whether this inventory is full (all slots occupied).
     *
     * @return true if the inventory is full
     */
    public boolean isFull() {
        return slots.size() >= maxItems;
    }

    /**
     * Checks whether this inventory is empty, treating "nothing" items as empty.
     *
     * @return true if the inventory is empty
     */
    public boolean isEmpty() {
        return isEmpty(true);
    }

    /**
     * Checks whether this inventory is empty.
     *
     * @param includeNothingItems if true, an inventory containing only "nothing" items is considered empty
     * @return true if the inventory is empty according to the specified criteria
     */
    public boolean isEmpty(boolean includeNothingItems) {
        if (getSlots().isEmpty()) return true;

        if (includeNothingItems) {
            return isOnlyNothingItems();
        }
        return false;
    }

    /**
     * Checks whether all items in this inventory are "nothing" items (air or null-like).
     *
     * @return true if all items are "nothing" items
     */
    public boolean isOnlyNothingItems() {
        AtomicBoolean onlyNothing = new AtomicBoolean(true);
        getSlots().forEach((slot, item) -> {
            if (! onlyNothing.get()) return;
            if (! isNothingItem(slot)) onlyNothing.set(false);
        });
        return onlyNothing.get();
    }

    /**
     * Sets an item in the specified slot.
     *
     * @param slot the slot index
     * @param item the item to place in the slot
     */
    public void setItem(int slot, ItemStack item) {
        slots.put(slot, item);
    }

    /**
     * Gets the item in the specified slot.
     *
     * @param slot the slot index
     * @return the item in the slot, or null if the slot is empty
     */
    public ItemStack getItem(int slot) {
        return slots.get(slot);
    }

    /**
     * Removes the item from the specified slot.
     *
     * @param slot the slot index to clear
     */
    public void removeItem(int slot) {
        slots.remove(slot);
    }

    /**
     * Checks whether the item in the specified slot is a "nothing" item (air or null-like).
     *
     * @param slot the slot index to check
     * @return true if the item is a "nothing" item
     */
    public boolean isNothingItem(int slot) {
        return ItemUtils.isNothingItem(getItem(slot));
    }

    /**
     * Copies all contents from a Bukkit Inventory into this managed inventory.
     *
     * @param inventory the Bukkit inventory to copy from
     */
    public void copyFrom(Inventory inventory) {
        this.slots = InventoryUtils.getInventoryContents(inventory);
    }

    /**
     * Copies all contents from this managed inventory into a Bukkit Inventory.
     *
     * @param inventory the Bukkit inventory to copy to
     */
    public void copyTo(Inventory inventory) {
        InventoryUtils.setInventoryContents(inventory, slots);
    }

    /**
     * Copies all contents from another ManagedInventory into this one.
     *
     * @param inventory the ManagedInventory to copy from
     */
    public void copyFrom(ManagedInventory inventory) {
        this.slots = new ConcurrentSkipListMap<>(inventory.getSlots());
    }

    /**
     * Copies all contents from this managed inventory into another ManagedInventory.
     *
     * @param inventory the ManagedInventory to copy to
     */
    public void copyTo(ManagedInventory inventory) {
        inventory.setSlots(new ConcurrentSkipListMap<>(this.slots));
    }

    /**
     * Removes all "nothing" items and re-indexes the remaining items sequentially starting from 0.
     */
    public void trim() {
        ConcurrentSkipListMap<Integer, ItemStack> newSlots = new ConcurrentSkipListMap<>();
        AtomicInteger newSlot = new AtomicInteger(0);
        slots.forEach((slot, item) -> {
            if (ItemUtils.isNothingItem(item)) return;

            newSlots.put(newSlot.getAndIncrement(), item);
        });

        slots = newSlots;
    }

    /**
     * Clears all items from this inventory.
     */
    public void clear() {
        slots.clear();
    }

    /**
     * Clears items from this inventory, optionally keeping "nothing" items.
     *
     * @param includeNothingItems if true, clears all items; if false, only removes non-nothing items
     */
    public void clear(boolean includeNothingItems) {
        if (includeNothingItems) {
            clear();
        } else {
            slots.forEach((slot, item) -> {
                if (! ItemUtils.isNothingItem(item)) slots.remove(slot);
            });
        }
    }

    /**
     * Returns the index of the first available slot (containing a "nothing" item or unoccupied).
     *
     * @return the first available slot index
     */
    public int getFirstAvailableSlot() {
        AtomicInteger slot = new AtomicInteger(0);
        AtomicBoolean found = new AtomicBoolean(false);
        getSlots().forEach((s, item) -> {
            if (found.get()) return;
            if (isNothingItem(s)) {
                slot.set(s);
                found.set(true);
            }
            slot.incrementAndGet();
        });

        return slot.get();
    }

    /**
     * Adds an item to the first available slot.
     *
     * @param stack the item to add
     */
    public void addItem(ItemStack stack) {
        int slot = getFirstAvailableSlot();
        setItem(slot, stack);
    }

    /**
     * Removes the first occurrence of an item that equals the specified item stack.
     *
     * @param stack the item to remove
     */
    public void removeItem(ItemStack stack) {
        slots.forEach((slot, item) -> {
            if (item.equals(stack)) {
                slots.remove(slot);
            }
        });
    }

    /**
     * Removes all items that are similar to the specified item stack.
     *
     * @param stack the item to match against using similarity comparison
     */
    public void removeSimilar(ItemStack stack) {
        slots.forEach((slot, item) -> {
            if (item.isSimilar(stack)) {
                slots.remove(slot);
            }
        });
    }

    /**
     * Adds multiple items to the inventory, each in the next available slot.
     *
     * @param stacks the items to add
     */
    public void addItems(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            addItem(stack);
        }
    }

    /**
     * Iterates over each item in the inventory, applying the given consumer.
     *
     * @param consumer the consumer to apply to each item
     */
    public void forEachItem(Consumer<ItemStack> consumer) {
        slots.forEach((slot, item) -> {
            consumer.accept(item);
        });
    }

    /**
     * Iterates over each slot index in the inventory, applying the given consumer.
     *
     * @param consumer the consumer to apply to each slot index
     */
    public void forEachSlot(Consumer<Integer> consumer) {
        slots.forEach((slot, item) -> {
            consumer.accept(slot);
        });
    }

    /**
     * Iterates over each slot, applying separate consumers for the slot index and the item.
     *
     * @param slotConsumer the consumer to apply to each slot index
     * @param itemConsumer the consumer to apply to each item
     */
    public void forEachSlotItem(Consumer<Integer> slotConsumer, Consumer<ItemStack> itemConsumer) {
        slots.forEach((slot, item) -> {
            slotConsumer.accept(slot);
            itemConsumer.accept(item);
        });
    }

    /**
     * Iterates over each slot-item pair in the inventory.
     *
     * @param consumer the bi-consumer accepting slot index and item
     */
    public void forEach(BiConsumer<Integer, ItemStack> consumer) {
        slots.forEach(consumer);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof ManagedInventory)) return false;
        ManagedInventory other = (ManagedInventory) obj;
        if (other.getId() != getId()) return false;

        AtomicBoolean equal = new AtomicBoolean(true);
        getSlots().forEach((slot, item) -> {
            if (! equal.get()) return;
            if (! item.equals(other.getItem(slot))) equal.set(false);
        });

        return equal.get();
    }

    /**
     * Returns the number of occupied slots in this inventory.
     *
     * @return the number of items
     */
    public int size() {
        return slots.size();
    }
}
