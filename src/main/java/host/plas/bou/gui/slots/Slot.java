package host.plas.bou.gui.slots;

import host.plas.bou.gui.MenuUtils;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Icon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class Slot implements Comparable<Slot> {
    private int index;
    private Icon icon;
    private SlotType type;

    public Slot(int index, Icon icon, SlotType type) {
        this.index = index;
        this.icon = icon;
        this.type = type;
    }

    public Slot(int index) {
        this(index, (Icon) null, SlotType.OTHER);
    }

    public Slot(int index, ItemStack stack, SlotType type) {
        this(index, adjustIcon(stack, type), type);
    }

    @Override
    public int compareTo(@NotNull Slot o) {
        return Integer.compare(index, o.getIndex());
    }

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