package host.plas.bou.utils.obj;

/**
 * Interface for objects that have a unique long ID and can be compared by that ID.
 */
public interface Idable extends Comparable<Idable> {
    /**
     * Returns the unique identifier for this object.
     *
     * @return the ID
     */
    long getId();

    /**
     * Compares this object with another Idable by their IDs.
     *
     * @param other the other Idable to compare to
     * @return a negative integer, zero, or a positive integer as this ID
     *         is less than, equal to, or greater than the other ID
     */
    default int compareTo(Idable other) {
        return Long.compare(getId(), other.getId());
    }
}
