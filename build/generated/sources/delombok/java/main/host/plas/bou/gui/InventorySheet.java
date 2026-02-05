package host.plas.bou.gui;

import host.plas.bou.gui.slots.Slot;
import host.plas.bou.gui.slots.SlotType;
import mc.obliviate.inventory.Icon;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class InventorySheet {
    private int size; // number of slots
    private ConcurrentSkipListSet<Slot> slots; // slot, icon

    public InventorySheet(int slots) {
        this.size = slots;
        this.slots = new ConcurrentSkipListSet<>();
    }

    public void addIcon(int slot, ItemStack stack, SlotType type) {
        this.slots.add(new Slot(slot, stack, type));
    }

    public void addIcon(int slot, Icon icon) {
        this.slots.add(new Slot(slot, icon, SlotType.OTHER));
    }

    public void removeIcon(int slot) {
        this.slots.removeIf(s -> s.getIndex() == slot);
    }

    public Slot getSlot(int slot) {
        AtomicReference<Slot> slotReference = new AtomicReference<>();
        this.slots.forEach(s -> {
            if (s.getIndex() == slot) {
                slotReference.set(s);
            }
        });
        return slotReference.get();
    }

    public int getRows() {
        return (int) Math.ceil(size / 9.0);
    }

    public void forEachSlot(Consumer<Slot> consumer) {
        this.slots.forEach(consumer);
    }

    public int getSize() {
        return this.size;
    }

    public ConcurrentSkipListSet<Slot> getSlots() {
        return this.slots;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public void setSlots(final ConcurrentSkipListSet<Slot> slots) {
        this.slots = slots;
    }
}
