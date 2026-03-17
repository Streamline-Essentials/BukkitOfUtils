package host.plas.bou.gui.slots;

/**
 * Enum defining the classification types for GUI inventory slots.
 * Used to determine how items in a slot behave with respect to player interaction.
 */
public enum SlotType {
    /** A static slot that displays an item but does not respond to clicks. */
    STATIC,
    /** A button slot that responds to player click interactions. */
    BUTTON,
    /** A slot with no special behavior classification. */
    OTHER,
    /** An empty slot with no item displayed. */
    EMPTY;
}
