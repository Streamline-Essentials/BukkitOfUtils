package host.plas.bou.configs.bits;

import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.Identifiable;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;

/**
 * A configurable whitelist (or blacklist) that manages a set of comparable items.
 * When operating in blacklist mode, the containment logic is inverted.
 *
 * @param <T> the type of elements in this whitelist, must be {@link Comparable}
 */
@Getter @Setter
public class ConfigurableWhitelist<T extends Comparable<T>> implements Identifiable {
    private String identifier;

    /**
     * The set of whitelisted items.
     *
     * @param whitelist the whitelist set to set
     * @return the whitelist set
     */
    private ConcurrentSkipListSet<T> whitelist;

    /**
     * Whether this whitelist operates in blacklist mode (inverted containment logic).
     *
     * @param blacklist true to enable blacklist mode
     * @return true if blacklist mode is enabled
     */
    private boolean blacklist;

    /**
     * Constructs a new ConfigurableWhitelist with the given identifier.
     * Defaults to whitelist mode (not blacklist) with an empty set.
     *
     * @param identifier the unique identifier for this whitelist
     */
    public ConfigurableWhitelist(String identifier) {
        this.identifier = identifier;

        whitelist = new ConcurrentSkipListSet<>();

        blacklist = false;
    }

    /**
     * Checks whether the given item is effectively contained in this whitelist,
     * taking blacklist mode into account.
     *
     * @param item the item to check for containment
     * @return {@code true} if the item is allowed by this whitelist/blacklist configuration
     */
    public boolean contains(T item) {
        return whitelist.contains(item) != blacklist;
    }

    /**
     * Adds an item to the whitelist set.
     *
     * @param item the item to add
     */
    public void add(T item) {
        whitelist.add(item);
    }

    /**
     * Removes an item from the whitelist set.
     *
     * @param item the item to remove
     */
    public void remove(T item) {
        whitelist.remove(item);
    }

    /**
     * Removes all items from the whitelist set that match the given predicate.
     *
     * @param predicate the condition used to determine which items to remove
     */
    public void removeIf(Predicate<T> predicate) {
        whitelist.removeIf(predicate);
    }

    /**
     * Removes all items from the whitelist set.
     */
    public void clear() {
        whitelist.clear();
    }

    /**
     * Checks if any item in the whitelist matches the given predicate,
     * and the list is not in blacklist mode.
     *
     * @param predicate the condition to test against whitelist items
     * @return {@code true} if any item matches and blacklist mode is off
     */
    public boolean check(Predicate<T> predicate) {
        return whitelist.stream().anyMatch(predicate) && ! blacklist;
    }

    /**
     * Checks if all items in the whitelist match the given predicate,
     * and the list is not in blacklist mode.
     *
     * @param predicate the condition to test against all whitelist items
     * @return {@code true} if all items match and blacklist mode is off
     */
    public boolean checkAll(Predicate<T> predicate) {
        return whitelist.stream().allMatch(predicate) && ! blacklist;
    }

    /**
     * Checks if no items in the whitelist match the given predicate,
     * and the list is not in blacklist mode.
     *
     * @param predicate the condition to test against whitelist items
     * @return {@code true} if no items match and blacklist mode is off
     */
    public boolean checkNone(Predicate<T> predicate) {
        return whitelist.stream().noneMatch(predicate) && ! blacklist;
    }

    /**
     * Checks whether the given item is allowed by this whitelist/blacklist.
     * In whitelist mode, the item must be present. In blacklist mode, the item must be absent.
     *
     * @param item the item to check
     * @return {@code true} if the item passes the whitelist/blacklist check
     */
    public boolean check(T item) {
        return ( whitelist.contains(item) && ! blacklist ) || ( ! whitelist.contains(item) && blacklist );
    }
}
