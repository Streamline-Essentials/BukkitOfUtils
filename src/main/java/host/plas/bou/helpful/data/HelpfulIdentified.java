package host.plas.bou.helpful.data;

import gg.drak.thebase.objects.Identifiable;

/**
 * Interface for objects that are identifiable through a {@link HelpfulInfo} instance.
 * Extends {@link Identifiable} by delegating identifier operations to the underlying info.
 */
public interface HelpfulIdentified extends Identifiable {
    /**
     * Retrieves the helpful info associated with this object.
     *
     * @return the helpful info metadata
     */
    HelpfulInfo getInfo();

    /**
     * Sets the helpful info for this object.
     *
     * @param var1 the helpful info to set
     */
    void setInfo(HelpfulInfo var1);

    /**
     * Returns the identifier from the underlying helpful info.
     *
     * @return the identifier string
     */
    default String getIdentifier() {
        return this.getInfo().getIdentifier();
    }

    /**
     * Sets the identifier on the underlying helpful info.
     *
     * @param s the identifier string to set
     */
    default void setIdentifier(String s) {
        this.getInfo().setIdentifier(s);
    }
}
