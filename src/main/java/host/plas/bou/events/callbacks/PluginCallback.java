package host.plas.bou.events.callbacks;

import host.plas.bou.BetterPlugin;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter @Setter
public class PluginCallback extends AbstractCallback<BetterPlugin> {
    public PluginCallback(int index, Consumer<BetterPlugin> consumer) {
        super(index, consumer, BetterPlugin.class);
    }

    public PluginCallback(Consumer<BetterPlugin> consumer) {
        super(consumer, BetterPlugin.class);
    }
}
