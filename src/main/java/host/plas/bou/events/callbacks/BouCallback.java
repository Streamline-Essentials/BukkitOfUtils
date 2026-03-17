package host.plas.bou.events.callbacks;

import gg.drak.thebase.objects.Indexable;

import java.util.function.Consumer;

/**
 * Interface for event callbacks in BukkitOfUtils.
 * Combines {@link Consumer} functionality with {@link Indexable} for indexed registration
 * in the {@link CallbackManager}.
 *
 * @param <T> the type of event or object this callback consumes
 */
public interface BouCallback<T> extends Consumer<T>, Indexable {
    /**
     * Gets the consumer function associated with this callback.
     *
     * @return the consumer that handles the callback logic
     */
    Consumer<T> getConsumer();

    /**
     * Sets the consumer function for this callback.
     *
     * @param consumer the consumer to set
     */
    void setConsumer(Consumer<T> consumer);

    /**
     * Loads (registers) this callback with the callback management system.
     */
    void load();

    /**
     * Unloads (unregisters) this callback from the callback management system.
     */
    void unload();

    /**
     * Checks whether this callback is currently loaded (registered).
     *
     * @return {@code true} if the callback is loaded, {@code false} otherwise
     */
    boolean isLoaded();

    /**
     * Checks whether this callback handles the specified class type.
     *
     * @param clazz the class to check against
     * @return {@code true} if this callback is compatible with the given type
     */
    boolean isOfType(Class<?> clazz);
}
