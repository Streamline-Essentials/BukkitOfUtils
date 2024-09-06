package host.plas.bou.gui.icons;

import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Icon;
import org.bukkit.inventory.ItemStack;

@Getter @Setter
public class BasicIcon extends Icon {
    public BasicIcon(ItemStack stack) {
        super(stack);

        applyEdits();
    }

    public void applyEdits() {
    }
}
