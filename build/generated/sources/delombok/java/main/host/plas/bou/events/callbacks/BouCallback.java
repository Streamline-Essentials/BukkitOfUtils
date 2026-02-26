package host.plas.bou.events.callbacks;

import gg.drak.thebase.objects.Indexable;

import java.util.function.Consumer;

public interface BouCallback<T> extends Consumer<T>, Indexable {
    Consumer<T> getConsumer();

    void setConsumer(Consumer<T> consumer);

    void load();

    void unload();

    boolean isLoaded();

    boolean isOfType(Class<?> clazz);
}
