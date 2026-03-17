package host.plas.bou.utils;

import java.util.function.Consumer;

/**
 * Generic utility class for applying a consumer to a given object.
 */
public class ThingyUtils {
    /** Private constructor to prevent instantiation of this utility class. */
    private ThingyUtils() {}
    /**
     * Applies the given consumer to the specified object.
     *
     * @param <C>      the type of the object
     * @param consumer the consumer to apply
     * @param thing    the object to pass to the consumer
     */
    public static <C> void withThing(Consumer<C> consumer, C thing) {
        consumer.accept(thing);
    }
}
