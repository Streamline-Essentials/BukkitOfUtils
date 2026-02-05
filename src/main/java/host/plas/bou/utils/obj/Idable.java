package host.plas.bou.utils.obj;

public interface Idable extends Comparable<Idable> {
    long getId();

    default int compareTo(Idable other) {
        return Long.compare(getId(), other.getId());
    }
}
