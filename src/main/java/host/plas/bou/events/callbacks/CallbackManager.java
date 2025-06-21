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

public class CallbackManager {
    @Getter @Setter
    private static ConcurrentSkipListSet<BouCallback<?>> loadedCallbacks = new ConcurrentSkipListSet<>();
    @Getter @Setter
    private static AtomicInteger index = new AtomicInteger(0);
    @Getter @Setter
    private static CallbackListener callbackListener;

    public static void loadCallback(BouCallback<?> callback) {
        if (hasCallback(callback.getIndex())) {
            unloadCallback(callback.getIndex());
        }

        loadedCallbacks.add(callback);
    }

    public static void unloadCallback(int index) {
        loadedCallbacks.removeIf(c -> c.getIndex() == index);
    }

    public static boolean hasCallback(int index) {
        return getCallback(index) != null;
    }

    public static BouCallback<?> getCallback(int index) {
        return loadedCallbacks.stream().filter(c -> c.getIndex() == index).findFirst().orElse(null);
    }

    public static <T, C extends BouCallback<T>> C getTypedCallback(int index) {
        return (C) getCallback(index);
    }

    public static int getNextIndex() {
        return index.getAndIncrement();
    }

    public static <T> void subscribe(Consumer<T> callback, Class<T> clazz) {
        new AbstractCallback<>(callback, clazz) {
            @Override
            public void accept(T t) {
                callback.accept(t);
            }
        };
    }

    public static void init() {
        callbackListener = new CallbackListener();
    }

    public static <E extends BaseEvent> void fireEvent(E event) {
        getLoadedCallbacks().forEach(c -> {
            if (c.isOfType(event.getClass())) {
                AbstractCallback<E> ac = (AbstractCallback<E>) c;
                ac.accept(event);
            }
        });
    }

    public static class CallbackListener implements BaseEventListener {
        public CallbackListener() {
            BaseEventHandler.bake(this, BukkitOfUtils.getInstance());

            BukkitOfUtils.getInstance().logInfo("&cCallback Listener &fhas been &aregistered&f!");
        }

        @BaseProcessor
        public <E extends BaseEvent> void onEvent(E event) {
            fireEvent(event);
        }
    }
}
