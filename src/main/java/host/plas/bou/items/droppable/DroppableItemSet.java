package host.plas.bou.items.droppable;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.math.CosmicMath;
import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.Identifiable;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Represents a set of droppable items associated with one or more arenas.
 * Provides weighted random selection of items based on their individual chances.
 */
@Getter @Setter
public class DroppableItemSet implements Identifiable {
    private String identifier;

    /**
     * The set of arena identifiers this droppable item set applies to.
     *
     * @param arenaSet the arena set to set
     * @return the arena set
     */
    private ConcurrentSkipListSet<String> arenaSet;

    /**
     * The set of droppable items in this set.
     *
     * @param itemSet the item set to set
     * @return the item set
     */
    private ConcurrentSkipListSet<DroppableItem> itemSet;

    /**
     * Constructs a new DroppableItemSet with the specified identifier, arena set, and item set.
     *
     * @param identifier the unique identifier for this set
     * @param arenaSet   the set of arena identifiers this set applies to
     * @param itemSet    the set of droppable items in this set
     */
    public DroppableItemSet(String identifier, ConcurrentSkipListSet<String> arenaSet, ConcurrentSkipListSet<DroppableItem> itemSet) {
        this.identifier = identifier;
        this.arenaSet = arenaSet;
        this.itemSet = itemSet;
    }

    /**
     * Constructs a new DroppableItemSet with the specified identifier, arena set, and items.
     *
     * @param identifier the unique identifier for this set
     * @param arenaSet   the set of arena identifiers this set applies to
     * @param items      the droppable items to include in this set
     */
    public DroppableItemSet(String identifier, ConcurrentSkipListSet<String> arenaSet, DroppableItem... items) {
        this(identifier, arenaSet, createItemSet(items));
    }

    /**
     * Constructs a new DroppableItemSet for a single arena with the specified items.
     *
     * @param identifier the unique identifier for this set
     * @param arena      the arena identifier this set applies to
     * @param items      the droppable items to include in this set
     */
    public DroppableItemSet(String identifier, String arena, DroppableItem... items) {
        this(identifier, createArenaSet(arena), createItemSet(items));
    }

    /**
     * Creates a set of arena identifiers from the given strings.
     *
     * @param arenas the arena identifier strings
     * @return a new sorted set containing the arena identifiers
     */
    public static ConcurrentSkipListSet<String> createArenaSet(String... arenas) {
        return new ConcurrentSkipListSet<>(Arrays.asList(arenas));
    }

    /**
     * Creates a set of droppable items from the given items.
     *
     * @param items the droppable items
     * @return a new sorted set containing the droppable items
     */
    public static ConcurrentSkipListSet<DroppableItem> createItemSet(DroppableItem... items) {
        return new ConcurrentSkipListSet<>(Arrays.asList(items));
    }

    // Returns a single DroppableItem that passes the roll
    // DroppableItem chances will be added together in a map,
    // then a random number will be generated and checked against the chances
    // So if you have 2 items with 50% chance each, you will have a 50% chance of getting one of them
    /**
     * Performs a weighted random selection of an item from this set.
     * Item chances are accumulated, and a random value is compared against
     * the cumulative range to select an item.
     *
     * @return an Optional containing the selected droppable item, or empty if selection fails
     */
    public Optional<DroppableItem> pollItems() {
        ConcurrentSkipListMap<Double, DroppableItem> itemChances = new ConcurrentSkipListMap<>();

        getItemSet().forEach(item -> {
            double lastChance = itemChances.isEmpty() ? 0 : itemChances.lastKey();
            double itemChance = item.getChance();

            double chance = lastChance + itemChance;

            itemChances.put(chance, item);
        });

        Double lastChance = itemChances.lastKey();
        if (lastChance == null) {
            return Optional.empty();
        }
        double random = CosmicMath.getRandomDouble(0, lastChance);

        try {
            return Optional.ofNullable(itemChances.ceilingEntry(random).getValue());
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Failed to poll item from DroppableItemSet: " + getIdentifier(), e);
            return Optional.empty();
        }
    }

    /**
     * Checks whether this item set applies to the specified arena.
     *
     * @param arena the arena identifier to check
     * @return true if this set's arena set contains the given arena
     */
    public boolean checkArena(String arena) {
        return getArenaSet().contains(arena);
    }
}
