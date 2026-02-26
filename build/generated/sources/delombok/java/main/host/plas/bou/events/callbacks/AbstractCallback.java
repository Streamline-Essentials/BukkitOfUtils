package host.plas.bou.events.callbacks;

import java.util.function.Consumer;

public abstract class AbstractCallback<T> implements BouCallback<T> {
    private int index;
    private Consumer<T> consumer;
    private Class<T> clazz;

    public AbstractCallback(int index, Consumer<T> consumer, Class<T> clazz) {
        this.index = index;
        this.consumer = consumer;
        this.clazz = clazz;
        load();
    }

    public AbstractCallback(Consumer<T> consumer, Class<T> clazz) {
        this(CallbackManager.getNextIndex(), consumer, clazz);
    }

    public void load() {
        CallbackManager.loadCallback(this);
    }

    public void unload() {
        CallbackManager.unloadCallback(index);
    }

    public boolean isLoaded() {
        return CallbackManager.hasCallback(index);
    }

    @Override
    public void accept(T t) {
        getConsumer().accept(t);
    }

    @Override
    public boolean isOfType(Class<?> clazz) {
        return this.clazz.equals(clazz);
    }

    public int getIndex() {
        return this.index;
    }

    public Consumer<T> getConsumer() {
        return this.consumer;
    }

    public Class<T> getClazz() {
        return this.clazz;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void setConsumer(final Consumer<T> consumer) {
        this.consumer = consumer;
    }

    public void setClazz(final Class<T> clazz) {
        this.clazz = clazz;
    }
}
