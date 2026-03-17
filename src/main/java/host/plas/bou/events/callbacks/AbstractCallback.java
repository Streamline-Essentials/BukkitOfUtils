package host.plas.bou.events.callbacks;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * Abstract base implementation of {@link BouCallback} that provides index management,
 * consumer delegation, and automatic registration with the {@link CallbackManager}.
 *
 * @param <T> the type of event or object this callback handles
 */
@Getter @Setter
public abstract class AbstractCallback<T> implements BouCallback<T> {
    private int index;
    private Consumer<T> consumer;
    /**
     * The class type that this callback handles.
     * @param clazz the class type to set
     * @return the class type this callback handles
     */
    private Class<T> clazz;

    /**
     * Constructs an AbstractCallback with a specific index, consumer, and type class.
     * Automatically loads (registers) the callback with the CallbackManager.
     *
     * @param index    the unique index for this callback
     * @param consumer the consumer function to execute when this callback is triggered
     * @param clazz    the class type this callback handles
     */
    public AbstractCallback(int index, Consumer<T> consumer, Class<T> clazz) {
        this.index = index;
        this.consumer = consumer;
        this.clazz = clazz;

        load();
    }

    /**
     * Constructs an AbstractCallback with an auto-generated index from the CallbackManager.
     *
     * @param consumer the consumer function to execute when this callback is triggered
     * @param clazz    the class type this callback handles
     */
    public AbstractCallback(Consumer<T> consumer, Class<T> clazz) {
        this(CallbackManager.getNextIndex(), consumer, clazz);
    }

    /**
     * Loads (registers) this callback with the {@link CallbackManager}.
     */
    public void load() {
        CallbackManager.loadCallback(this);
    }

    /**
     * Unloads (unregisters) this callback from the {@link CallbackManager}.
     */
    public void unload() {
        CallbackManager.unloadCallback(index);
    }

    /**
     * Checks whether this callback is currently loaded in the {@link CallbackManager}.
     *
     * @return {@code true} if this callback is registered, {@code false} otherwise
     */
    public boolean isLoaded() {
        return CallbackManager.hasCallback(index);
    }

    /**
     * Accepts and processes the given value by delegating to the internal consumer.
     *
     * @param t the value to process
     */
    @Override
    public void accept(T t) {
        getConsumer().accept(t);
    }

    /**
     * Checks whether this callback handles the specified class type.
     *
     * @param clazz the class to check against
     * @return {@code true} if this callback's type matches the given class
     */
    @Override
    public boolean isOfType(Class<?> clazz) {
        return this.clazz.equals(clazz);
    }
}
