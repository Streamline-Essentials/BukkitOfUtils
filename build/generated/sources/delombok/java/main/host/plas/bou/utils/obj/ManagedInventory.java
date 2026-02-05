package host.plas.bou.utils.obj;

import host.plas.bou.items.InventoryUtils;
import host.plas.bou.items.ItemUtils;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ManagedInventory implements Idable {
    private static AtomicLong currentId = new AtomicLong(0);

    public static long getNextId() {
        return currentId.getAndIncrement();
    }

    private final long id;
    private int maxItems;
    private ConcurrentSkipListMap<Integer, ItemStack> slots;
    private boolean ready;

    public ManagedInventory(int maxItems) {
        id = getNextId();
        this.maxItems = maxItems;
        this.slots = new ConcurrentSkipListMap<>();
        ready = false;
    }

    public int getFreeSlots() {
        return maxItems - slots.size();
    }

    public boolean isFull() {
        return slots.size() >= maxItems;
    }

    public boolean isEmpty() {
        return isEmpty(true);
    }

    public boolean isEmpty(boolean includeNothingItems) {
        if (getSlots().isEmpty()) return true;
        if (includeNothingItems) {
            return isOnlyNothingItems();
        }
        return false;
    }

    public boolean isOnlyNothingItems() {
        AtomicBoolean onlyNothing = new AtomicBoolean(true);
        getSlots().forEach((slot, item) -> {
            if (!onlyNothing.get()) return;
            if (!isNothingItem(slot)) onlyNothing.set(false);
        });
        return onlyNothing.get();
    }

    public void setItem(int slot, ItemStack item) {
        slots.put(slot, item);
    }

    public ItemStack getItem(int slot) {
        return slots.get(slot);
    }

    public void removeItem(int slot) {
        slots.remove(slot);
    }

    public boolean isNothingItem(int slot) {
        return ItemUtils.isNothingItem(getItem(slot));
    }

    public void copyFrom(Inventory inventory) {
        this.slots = InventoryUtils.getInventoryContents(inventory);
    }

    public void copyTo(Inventory inventory) {
        InventoryUtils.setInventoryContents(inventory, slots);
    }

    public void copyFrom(ManagedInventory inventory) {
        this.slots = new ConcurrentSkipListMap<>(inventory.getSlots());
    }

    public void copyTo(ManagedInventory inventory) {
        inventory.setSlots(new ConcurrentSkipListMap<>(this.slots));
    }

    public void trim() {
        ConcurrentSkipListMap<Integer, ItemStack> newSlots = new ConcurrentSkipListMap<>();
        AtomicInteger newSlot = new AtomicInteger(0);
        slots.forEach((slot, item) -> {
            if (ItemUtils.isNothingItem(item)) return;
            newSlots.put(newSlot.getAndIncrement(), item);
        });
        slots = newSlots;
    }

    public void clear() {
        slots.clear();
    }

    public void clear(boolean includeNothingItems) {
        if (includeNothingItems) {
            clear();
        } else {
            slots.forEach((slot, item) -> {
                if (!ItemUtils.isNothingItem(item)) slots.remove(slot);
            });
        }
    }

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

    public void addItem(ItemStack stack) {
        int slot = getFirstAvailableSlot();
        setItem(slot, stack);
    }

    public void removeItem(ItemStack stack) {
        slots.forEach((slot, item) -> {
            if (item.equals(stack)) {
                slots.remove(slot);
            }
        });
    }

    public void removeSimilar(ItemStack stack) {
        slots.forEach((slot, item) -> {
            if (item.isSimilar(stack)) {
                slots.remove(slot);
            }
        });
    }

    public void addItems(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            addItem(stack);
        }
    }

    public void forEachItem(Consumer<ItemStack> consumer) {
        slots.forEach((slot, item) -> {
            consumer.accept(item);
        });
    }

    public void forEachSlot(Consumer<Integer> consumer) {
        slots.forEach((slot, item) -> {
            consumer.accept(slot);
        });
    }

    public void forEachSlotItem(Consumer<Integer> slotConsumer, Consumer<ItemStack> itemConsumer) {
        slots.forEach((slot, item) -> {
            slotConsumer.accept(slot);
            itemConsumer.accept(item);
        });
    }

    public void forEach(BiConsumer<Integer, ItemStack> consumer) {
        slots.forEach(consumer);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ManagedInventory)) return false;
        ManagedInventory other = (ManagedInventory) obj;
        if (other.getId() != getId()) return false;
        AtomicBoolean equal = new AtomicBoolean(true);
        getSlots().forEach((slot, item) -> {
            if (!equal.get()) return;
            if (!item.equals(other.getItem(slot))) equal.set(false);
        });
        return equal.get();
    }

    public int size() {
        return slots.size();
    }

    public long getId() {
        return this.id;
    }

    public int getMaxItems() {
        return this.maxItems;
    }

    public ConcurrentSkipListMap<Integer, ItemStack> getSlots() {
        return this.slots;
    }

    public boolean isReady() {
        return this.ready;
    }

    public void setMaxItems(final int maxItems) {
        this.maxItems = maxItems;
    }

    public void setSlots(final ConcurrentSkipListMap<Integer, ItemStack> slots) {
        this.slots = slots;
    }

    public void setReady(final boolean ready) {
        this.ready = ready;
    }
}
