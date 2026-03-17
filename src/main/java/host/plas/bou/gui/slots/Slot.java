package host.plas.bou.gui.slots;

import host.plas.bou.gui.MenuUtils;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Icon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a single slot in a GUI inventory, containing an index, icon, and type.
 * Implements {@link Comparable} for ordering by slot index.
 */
@Getter @Setter
public class Slot implements Comparable<Slot> {
    /**
     * The slot index in the inventory.
     *
     * @param index the slot index to set
     * @return the slot index
     */
    private int index;
    /**
     * The icon displayed in this slot.
     *
     * @param icon the icon to set
     * @return the icon
     */
    private Icon icon;
    /**
     * The type classification of this slot.
     *
     * @param type the slot type to set
     * @return the slot type
     */
    private SlotType type;

    /**
     * Constructs a new Slot with the given index, icon, and type.
     *
     * @param index the slot index in the inventory
     * @param icon  the icon to display in this slot
     * @param type  the slot type classification
     */
    public Slot(int index, Icon icon, SlotType type) {
        this.index = index;
        this.icon = icon;
        this.type = type;
    }

    /**
     * Constructs a new Slot with the given index, no icon, and the OTHER type.
     *
     * @param index the slot index in the inventory
     */
    public Slot(int index) {
        this(index, (Icon) null, SlotType.OTHER);
    }

    /**
     * Constructs a new Slot with the given index, item stack, and type.
     * The item stack is adjusted based on the slot type (e.g., injecting button or static metadata).
     *
     * @param index the slot index in the inventory
     * @param stack the item stack to display
     * @param type  the slot type classification
     */
    public Slot(int index, ItemStack stack, SlotType type) {
        this(index, adjustIcon(stack, type), type);
    }

    @Override
    public int compareTo(@NotNull Slot o) {
        return Integer.compare(index, o.getIndex());
    }

    /**
     * Adjusts an item stack into an icon based on the slot type, injecting
     * appropriate persistent data markers for button or static types.
     *
     * @param stack the item stack to adjust
     * @param type  the slot type determining what metadata to inject
     * @return the adjusted {@link Icon}
     */
    public static Icon adjustIcon(ItemStack stack, SlotType type) {
        switch (type) {
            case BUTTON:
                MenuUtils.injectButton(stack);

                return new Icon(stack);
            case STATIC:
                MenuUtils.injectStatic(stack);

                return new Icon(stack);
            case OTHER:
                return new Icon(stack);
        }

        return new Icon(stack);
    }
}
