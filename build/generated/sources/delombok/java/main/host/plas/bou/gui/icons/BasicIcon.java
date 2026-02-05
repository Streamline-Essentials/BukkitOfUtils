package host.plas.bou.gui.icons;

import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BasicIcon extends Icon {
    public BasicIcon(ItemStack stack) {
        super(stack);
        applyEdits();
    }

    public BasicIcon(Material material) {
        this(new ItemStack(material));
    }

    public BasicIcon(String materialName) {
        this(getMaterial(materialName));
    }

    public void applyEdits() {
    }

    public static Material getMaterial(String materialName) {
        try {
            for (Material mat : Material.values()) {
                if (mat.name().equalsIgnoreCase(materialName)) {
                    return mat;
                }
            }
        } catch (Exception e) {
        }
        /* Ignored */ return Material.BARRIER;
    }
}
