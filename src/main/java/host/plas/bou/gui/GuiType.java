package host.plas.bou.gui;

/**
 * Interface representing a GUI type with a name and display title.
 * Implementations define specific GUI categories or screens.
 */
public interface GuiType {
    /**
     * Returns the name of this GUI type.
     *
     * @return the name identifier
     */
    String name();

    /**
     * Returns a string representation of this GUI type.
     *
     * @return the string representation
     */
    String toString();

    /**
     * Returns the display title for this GUI type.
     *
     * @return the title string
     */
    String getTitle();
}
