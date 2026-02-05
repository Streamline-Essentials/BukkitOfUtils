package host.plas.bou.gui.icons;

import org.bukkit.Material;

public class AirIcon extends BasicIcon {
    public AirIcon() {
        super(Material.AIR);
    }

    public static AirIcon get() {
        return new AirIcon();
    }
}
