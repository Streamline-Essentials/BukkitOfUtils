package host.plas.bou.events.callbacks;

import host.plas.bou.BukkitOfUtils;
import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.events.BaseEventHandler;
import gg.drak.thebase.events.BaseEventListener;
import gg.drak.thebase.events.components.BaseEvent;
import gg.drak.thebase.events.processing.BaseProcessor;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Manages the registration, lookup, and dispatching of {@link BouCallback} instances.
 * Maintains a thread-safe set of loaded callbacks and provides static utility methods
 * for callback lifecycle management.
 */
public class CallbackManager {
    /**
     * The thread-safe set of all currently loaded callbacks.
     * @param loadedCallbacks the set of loaded callbacks to set
     * @return the set of loaded callbacks
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<BouCallback<?>> loadedCallbacks = new ConcurrentSkipListSet<>();
    /**
     * The atomic counter used to assign unique indices to callbacks.
     * @param index the atomic index counter to set
     * @return the atomic index counter
     */
    @Getter @Setter
    private static AtomicInteger index = new AtomicInteger(0);
    /**
     * The listener that bridges base events to the callback dispatching system.
     * @param callbackListener the callback listener to set
     * @return the callback listener
     */
    @Getter @Setter
    private static CallbackListener callbackListener;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CallbackManager() {
        // utility class
    }

    /**
     * Loads a callback into the manager. If a callback with the same index already exists,
     * it is unloaded first before the new one is added.
     *
     * @param callback the callback to load
     */
    public static void loadCallback(BouCallback<?> callback) {
        if (hasCallback(callback.getIndex())) {
            unloadCallback(callback.getIndex());
        }

        loadedCallbacks.add(callback);
    }

    /**
     * Unloads (removes) a callback from the manager by its index.
     *
     * @param index the index of the callback to unload
     */
    public static void unloadCallback(int index) {
        loadedCallbacks.removeIf(c -> c.getIndex() == index);
    }

    /**
     * Checks whether a callback with the given index is currently loaded.
     *
     * @param index the index to check
     * @return {@code true} if a callback with that index exists
     */
    public static boolean hasCallback(int index) {
        return getCallback(index) != null;
    }

    /**
     * Retrieves a callback by its index.
     *
     * @param index the index of the callback to retrieve
     * @return the callback with the given index, or {@code null} if not found
     */
    public static BouCallback<?> getCallback(int index) {
        return loadedCallbacks.stream().filter(c -> c.getIndex() == index).findFirst().orElse(null);
    }

    /**
     * Retrieves and casts a callback to a specific typed callback by its index.
     *
     * @param index the index of the callback to retrieve
     * @param <T>   the event/object type the callback handles
     * @param <C>   the callback type
     * @return the typed callback, or {@code null} if not found
     */
    public static <T, C extends BouCallback<T>> C getTypedCallback(int index) {
        return (C) getCallback(index);
    }

    /**
     * Gets and increments the next available callback index.
     *
     * @return the next available index value
     */
    public static int getNextIndex() {
        return index.getAndIncrement();
    }

    /**
     * Creates and registers an anonymous callback that subscribes to events of the given type.
     *
     * @param callback the consumer to invoke when an event of the specified type occurs
     * @param clazz    the class of events to subscribe to
     * @param <T>      the event type
     */
    public static <T> void subscribe(Consumer<T> callback, Class<T> clazz) {
        new AbstractCallback<>(callback, clazz) {
            @Override
            public void accept(T t) {
                callback.accept(t);
            }
        };
    }

    /**
     * Initializes the callback system by creating and registering the internal callback listener.
     */
    public static void init() {
        callbackListener = new CallbackListener();
    }

    /**
     * Fires an event to all loaded callbacks that match the event's type.
     *
     * @param event the event to dispatch to matching callbacks
     * @param <E>   the event type, must extend {@link BaseEvent}
     */
    public static <E extends BaseEvent> void fireEvent(E event) {
        getLoadedCallbacks().forEach(c -> {
            if (c.isOfType(event.getClass())) {
                AbstractCallback<E> ac = (AbstractCallback<E>) c;
                ac.accept(event);
            }
        });
    }

    /**
     * Internal listener that bridges the base event system to the callback manager,
     * forwarding all received events to {@link CallbackManager#fireEvent(BaseEvent)}.
     */
    public static class CallbackListener implements BaseEventListener {
        /**
         * Constructs a new CallbackListener and registers it with the event handler system.
         */
        public CallbackListener() {
            BaseEventHandler.bake(this, BukkitOfUtils.getInstance());

            BukkitOfUtils.getInstance().logInfo("&cCallback Listener &fhas been &aregistered&f!");
        }

        /**
         * Receives any base event and forwards it to the callback manager for dispatching.
         *
         * @param event the event to forward
         * @param <E>   the event type
         */
        @BaseProcessor
        public <E extends BaseEvent> void onEvent(E event) {
            fireEvent(event);
        }
    }
}
