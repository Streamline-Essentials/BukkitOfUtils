package host.plas.bou.utils.comparators;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Comparator;

@Getter @Setter
public class DistanceComparator implements Comparator<Entity> {
    private final Location referenceLocation;

    public DistanceComparator(Location referenceLocation) {
        this.referenceLocation = referenceLocation;
    }

    @Override
    public int compare(Entity o1, Entity o2) {
        Location l1 = o1.getLocation();
        Location l2 = o2.getLocation();

        double d1 = l1.distanceSquared(referenceLocation);
        double d2 = l2.distanceSquared(referenceLocation);

        return Double.compare(d1, d2);
    }
}
