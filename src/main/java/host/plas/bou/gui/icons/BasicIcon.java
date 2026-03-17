package host.plas.bou.gui.icons;

import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * A basic GUI icon that wraps an {@link ItemStack} and extends {@link Icon}.
 * Provides convenience constructors for creating icons from item stacks, materials, or material names.
 */
public class BasicIcon extends Icon {
    /**
     * Constructs a new BasicIcon from an item stack and applies any edits.
     *
     * @param stack the item stack to use as the icon
     */
    public BasicIcon(ItemStack stack) {
        super(stack);
        this.applyEdits();
    }

    /**
     * Constructs a new BasicIcon from a material.
     *
     * @param material the material to create the icon from
     */
    public BasicIcon(Material material) {
        this(new ItemStack(material));
    }

    /**
     * Constructs a new BasicIcon from a material name string.
     *
     * @param materialName the name of the material (case-insensitive)
     */
    public BasicIcon(String materialName) {
        this(getMaterial(materialName));
    }

    /**
     * Hook method for subclasses to apply custom edits after icon construction.
     * Default implementation does nothing.
     */
    public void applyEdits() {
    }

    /**
     * Resolves a material by name (case-insensitive). Falls back to {@link Material#BARRIER}
     * if no matching material is found.
     *
     * @param materialName the name of the material to look up
     * @return the matching {@link Material}, or {@link Material#BARRIER} if not found
     */
    public static Material getMaterial(String materialName) {
        try {
            for(Material mat : Material.values()) {
                if (mat.name().equalsIgnoreCase(materialName)) {
                    return mat;
                }
            }
        } catch (Exception var5) {
        }

        return Material.BARRIER;
    }
}
