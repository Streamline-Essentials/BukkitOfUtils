package host.plas.bou.events.callbacks;

import host.plas.bou.events.self.plugin.PluginDisableEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter @Setter
public class DisableCallback extends AbstractCallback<PluginDisableEvent> {
    public DisableCallback(int index, Consumer<PluginDisableEvent> consumer) {
        super(index, consumer, PluginDisableEvent.class);
    }

    public DisableCallback(Consumer<PluginDisableEvent> consumer) {
        super(consumer, PluginDisableEvent.class);
    }
}
