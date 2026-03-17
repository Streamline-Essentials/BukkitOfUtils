package host.plas.bou.events.callbacks;

import host.plas.bou.events.self.plugin.PluginDisableEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * A callback specialized for handling {@link PluginDisableEvent} instances.
 */
@Getter @Setter
public class DisableCallback extends AbstractCallback<PluginDisableEvent> {
    /**
     * Constructs a DisableCallback with a specific index and consumer.
     *
     * @param index    the unique index for this callback
     * @param consumer the consumer to invoke when a plugin disable event occurs
     */
    public DisableCallback(int index, Consumer<PluginDisableEvent> consumer) {
        super(index, consumer, PluginDisableEvent.class);
    }

    /**
     * Constructs a DisableCallback with an auto-generated index.
     *
     * @param consumer the consumer to invoke when a plugin disable event occurs
     */
    public DisableCallback(Consumer<PluginDisableEvent> consumer) {
        super(consumer, PluginDisableEvent.class);
    }
}
