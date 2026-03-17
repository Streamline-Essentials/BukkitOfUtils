package host.plas.bou.items.droppable;

/**
 * Defines the type of a droppable item, distinguishing between
 * plain item stacks and keyed (registered) items.
 */
public enum DroppableItemType {
    /** A standard item stack drop. */
    ITEM,
    /** A keyed item resolved through the item factory. */
    KEYED,
    ;
}
