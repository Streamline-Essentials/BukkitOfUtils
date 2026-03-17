package host.plas.bou.utils.comparators;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Comparator;

/**
 * A comparator that orders entities by their distance to a reference location,
 * using squared distance for efficiency.
 */
@Getter @Setter
public class DistanceComparator implements Comparator<Entity> {
    /**
     * The reference location used to measure distances from.
     *
     * @return the reference location
     */
    private final Location referenceLocation;

    /**
     * Constructs a DistanceComparator with the given reference location.
     *
     * @param referenceLocation the location to measure distances from
     */
    public DistanceComparator(Location referenceLocation) {
        this.referenceLocation = referenceLocation;
    }

    /**
     * Compares two entities by their squared distance to the reference location.
     *
     * @param o1 the first entity
     * @param o2 the second entity
     * @return a negative integer, zero, or a positive integer as the first entity
     *         is closer to, equidistant from, or farther from the reference location than the second
     */
    @Override
    public int compare(Entity o1, Entity o2) {
        Location l1 = o1.getLocation();
        Location l2 = o2.getLocation();

        double d1 = l1.distanceSquared(referenceLocation);
        double d2 = l2.distanceSquared(referenceLocation);

        return Double.compare(d1, d2);
    }
}
