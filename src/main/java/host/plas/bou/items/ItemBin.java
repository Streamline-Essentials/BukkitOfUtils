package host.plas.bou.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * A static storage bin for {@link ConvertableItemStack} instances, providing
 * add, remove, lookup, and ID generation operations.
 */
public class ItemBin {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ItemBin() {
        // utility class
    }

    /**
     * The storage set containing all convertable item stacks.
     *
     * @param bin the item stack storage set to set
     * @return the item stack storage set
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<ConvertableItemStack> bin = new ConcurrentSkipListSet<>();
    /**
     * The atomic counter used to generate sequential stashed IDs.
     *
     * @param atomicNextId the atomic ID counter to set
     * @return the atomic ID counter
     */
    @Getter @Setter
    private static AtomicInteger atomicNextId = new AtomicInteger(0);

    /**
     * Adds a convertable item stack to the bin.
     *
     * @param itemStack the item stack to add
     */
    public static void add(ConvertableItemStack itemStack) {
        bin.add(itemStack);
    }

    /**
     * Removes a convertable item stack from the bin.
     *
     * @param itemStack the item stack to remove
     */
    public static void remove(ConvertableItemStack itemStack) {
        bin.remove(itemStack);
    }

    /**
     * Removes all items from the bin that match the given predicate.
     *
     * @param predicate the condition to determine which items to remove
     */
    public static void removeIf(Predicate<ConvertableItemStack> predicate) {
        bin.removeIf(predicate);
    }

    /**
     * Clears all items from the bin.
     */
    public static void clear() {
        bin.clear();
    }

    /**
     * Finds the first item in the bin matching the given predicate.
     *
     * @param predicate the condition to match against
     * @return an Optional containing the matching item, or empty if none found
     */
    public static Optional<ConvertableItemStack> get(Predicate<ConvertableItemStack> predicate) {
        return bin.stream().filter(predicate).findFirst();
    }

    /**
     * Finds an item in the bin by matching its Bukkit item stack.
     *
     * @param stack the Bukkit item stack to search for
     * @return an Optional containing the matching convertable item stack, or empty if none found
     */
    public static Optional<ConvertableItemStack> get(ItemStack stack) {
        return get(itemStack -> {
            AtomicBoolean found = new AtomicBoolean(false);

            if (itemStack.getItemStackOptional().isPresent()) {
                found.set(itemStack.getItemStack().equals(stack));
            }

            return found.get();
        });
    }

    /**
     * Finds an item in the bin by matching its string representation.
     *
     * @param itemString the item string to search for
     * @return an Optional containing the matching convertable item stack, or empty if none found
     */
    public static Optional<ConvertableItemStack> get(String itemString) {
        return get(itemStack -> {
            AtomicBoolean found = new AtomicBoolean(false);

            if (itemStack.getItemStringOptional().isPresent()) {
                found.set(itemStack.getItemString().equals(itemString));
            }

            return found.get();
        });
    }

    /**
     * Finds an item in the bin by its UUID.
     *
     * @param uuid the UUID to search for
     * @return an Optional containing the matching convertable item stack, or empty if none found
     */
    public static Optional<ConvertableItemStack> get(UUID uuid) {
        return get(itemStack -> itemStack.getUuid().equals(uuid));
    }

    /**
     * Finds an item in the bin by its stashed ID.
     *
     * @param stashedId the stashed ID to search for
     * @return an Optional containing the matching convertable item stack, or empty if none found
     */
    public static Optional<ConvertableItemStack> get(int stashedId) {
        return get(itemStack -> itemStack.getStashedId() == stashedId);
    }

    /**
     * Checks whether the bin contains the specified convertable item stack.
     *
     * @param itemStack the item stack to check for
     * @return true if the bin contains the item
     */
    public static boolean contains(ConvertableItemStack itemStack) {
        return bin.contains(itemStack);
    }

    /**
     * Checks whether the bin contains an item matching the given Bukkit item stack.
     *
     * @param stack the Bukkit item stack to check for
     * @return true if a matching item exists in the bin
     */
    public static boolean contains(ItemStack stack) {
        return get(stack).isPresent();
    }

    /**
     * Checks whether the bin contains an item matching the given item string.
     *
     * @param itemString the item string to check for
     * @return true if a matching item exists in the bin
     */
    public static boolean contains(String itemString) {
        return get(itemString).isPresent();
    }

    /**
     * Checks whether the bin contains an item with the given UUID.
     *
     * @param uuid the UUID to check for
     * @return true if an item with the given UUID exists in the bin
     */
    public static boolean has(UUID uuid) {
        return get(uuid).isPresent();
    }

    /**
     * Checks whether the bin contains an item with the given stashed ID.
     *
     * @param stashedId the stashed ID to check for
     * @return true if an item with the given stashed ID exists in the bin
     */
    public static boolean has(int stashedId) {
        return get(stashedId).isPresent();
    }

    /**
     * Checks whether the bin contains any item matching the given predicate.
     *
     * @param predicate the condition to match against
     * @return true if any item in the bin matches the predicate
     */
    public static boolean has(Predicate<ConvertableItemStack> predicate) {
        return get(predicate).isPresent();
    }

    /**
     * Generates a new unique UUID that does not collide with any existing item in the bin.
     *
     * @return a unique UUID
     */
    public static UUID getNextUUID() {
        AtomicReference<UUID> uuidRef = new AtomicReference<>(UUID.randomUUID());

        while (bin.stream().anyMatch(itemStack -> itemStack.getUuid().equals(uuidRef.get()))) {
            uuidRef.set(UUID.randomUUID());
        }

        return uuidRef.get();
    }

    /**
     * Returns the next sequential stashed ID and increments the counter.
     *
     * @return the next available stashed ID
     */
    public static int getNextId() {
        return atomicNextId.getAndIncrement();
    }

    /**
     * Returns a set of all stashed IDs currently in the bin.
     *
     * @return a sorted set of stashed IDs
     */
    public static ConcurrentSkipListSet<Integer> getStashedIds() {
        return bin.stream().collect(ConcurrentSkipListSet::new, (set, itemStack) -> set.add(itemStack.getStashedId()), Collection::addAll);
    }

    /**
     * Returns a set of all stashed IDs in the bin as strings.
     *
     * @return a sorted set of stashed IDs as string values
     */
    public static ConcurrentSkipListSet<String> getStashedIdsAsStrings() {
        return bin.stream().collect(ConcurrentSkipListSet::new, (set, itemStack) -> set.add(String.valueOf(itemStack.getStashedId())), Collection::addAll);
    }
}
