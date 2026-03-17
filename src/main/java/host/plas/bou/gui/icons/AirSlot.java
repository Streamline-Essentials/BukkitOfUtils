package host.plas.bou.gui.icons;

import host.plas.bou.gui.slots.Slot;
import host.plas.bou.gui.slots.SlotType;

/**
 * A slot representing an empty air position in a GUI inventory.
 */
public class AirSlot extends Slot {
    /**
     * Constructs a new AirSlot at the specified index with an air icon and empty slot type.
     *
     * @param slot the slot index
     */
    public AirSlot(int slot) {
        super(slot, AirIcon.get(), SlotType.EMPTY);
    }

    /**
     * Creates and returns a new AirSlot at the specified index.
     *
     * @param slot the slot index
     * @return a new {@link AirSlot} at the given index
     */
    public static AirSlot get(int slot) {
        return new AirSlot(slot);
    }
}
