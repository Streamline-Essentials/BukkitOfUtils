package host.plas.bou.gui.icons;

import host.plas.bou.gui.slots.Slot;
import host.plas.bou.gui.slots.SlotType;

public class AirSlot extends Slot {
    public AirSlot(int slot) {
        super(slot, AirIcon.get(), SlotType.EMPTY);
    }

    public static AirSlot get(int slot) {
        return new AirSlot(slot);
    }
}
