package host.plas.bou.gui.icons;

import org.bukkit.Material;

/**
 * An icon representing an air (empty) slot in a GUI inventory.
 */
public class AirIcon extends BasicIcon {
    /**
     * Constructs a new AirIcon using the {@link Material#AIR} material.
     */
    public AirIcon() {
        super(Material.AIR);
    }

    /**
     * Creates and returns a new AirIcon instance.
     *
     * @return a new {@link AirIcon}
     */
    public static AirIcon get() {
        return new AirIcon();
    }
}
