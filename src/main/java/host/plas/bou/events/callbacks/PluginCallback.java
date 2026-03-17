package host.plas.bou.events.callbacks;

import host.plas.bou.BetterPlugin;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * A callback specialized for handling {@link BetterPlugin} instances.
 */
@Getter @Setter
public class PluginCallback extends AbstractCallback<BetterPlugin> {
    /**
     * Constructs a PluginCallback with a specific index and consumer.
     *
     * @param index    the unique index for this callback
     * @param consumer the consumer to invoke when a BetterPlugin event occurs
     */
    public PluginCallback(int index, Consumer<BetterPlugin> consumer) {
        super(index, consumer, BetterPlugin.class);
    }

    /**
     * Constructs a PluginCallback with an auto-generated index.
     *
     * @param consumer the consumer to invoke when a BetterPlugin event occurs
     */
    public PluginCallback(Consumer<BetterPlugin> consumer) {
        super(consumer, BetterPlugin.class);
    }
}
