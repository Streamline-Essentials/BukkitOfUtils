package host.plas.bou.gui;

import host.plas.bou.gui.icons.AirIcon;
import host.plas.bou.gui.icons.AirSlot;
import host.plas.bou.gui.slots.Slot;
import host.plas.bou.gui.slots.SlotType;
import host.plas.bou.helpful.data.HelpfulGui;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.obj.ManagedInventory;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class InventorySheet {
    private int size; // number of slots
    private ConcurrentSkipListSet<Slot> slots; // slot, icon

    public InventorySheet(int slots, Slot... icons) {
        this.size = slots;
        setAllEmpty();
    }

    public void ensureSlots(boolean clear) {
        if (this.slots == null) this.slots = new ConcurrentSkipListSet<>();
         else {
            if (clear) this.slots.clear();
        }
    }

    public void ensureSlots() {
        ensureSlots(false);
    }

    public void withSlots(Slot... slots) {
        ensureSlots();
        this.slots.addAll(Arrays.asList(slots));
    }

    public void asSlots(Slot... slots) {
        ensureSlots(true);
        this.slots.addAll(Arrays.asList(slots));
    }

    public void setIcon(int slot, ItemStack stack, SlotType type) {
        removeIcon(slot);
        this.slots.add(new Slot(slot, stack, type));
    }

    public void setIcon(int slot, Icon icon) {
        removeIcon(slot);
        this.slots.add(new Slot(slot, icon, SlotType.OTHER));
    }

    public void addIcon(int slot, ItemStack stack, SlotType type) {
        this.slots.add(new Slot(slot, stack, type));
    }

    public void addIcon(int slot, Icon icon) {
        this.slots.add(new Slot(slot, icon, SlotType.OTHER));
    }

    public void insertSlot(Slot slot) {
        this.slots.add(slot);
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

    public void setAllEmpty() {
        ensureSlots(true);
        for (int i = 0; i < size; i++) {
            insertSlot(AirSlot.get(i));
        }
    }

    public void forEachSlot(Consumer<Slot> consumer) {
        this.slots.forEach(consumer);
    }

    public static InventorySheet empty(int size) {
        InventorySheet sheet = new InventorySheet(size);
        sheet.setAllEmpty();
        return sheet;
    }

    public static InventorySheet of(ManagedInventory inventory) {
        return of(inventory, SlotType.OTHER);
    }

    public static InventorySheet of(ManagedInventory inventory, SlotType type) {
        InventorySheet sheet = empty(inventory.size());
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;
            sheet.setIcon(i, item, type);
        }
        return sheet;
    }

    public static InventorySheet of(HelpfulGui gui, int startIndex, Material material) {
        InventorySheet sheet = empty(3 * 9);
        AtomicInteger index = new AtomicInteger(startIndex);
        AtomicInteger pageCount = new AtomicInteger(0);
        gui.getHelpful().getDocument().getPages().forEach((integer, textPage) -> {
            ItemStack stack = ItemUtils.make(material, "&e&lHint &b#&a" + (pageCount.incrementAndGet()), textPage.asLore());
            Slot slot = new Slot(index.getAndIncrement(), stack, SlotType.STATIC);
            sheet.insertSlot(slot);
        });
        return sheet;
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
