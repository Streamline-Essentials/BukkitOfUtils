package host.plas.bou.events.self.plugin;

import host.plas.bou.BetterPlugin;
import host.plas.bou.events.self.BetterPluginEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class PluginDisableEvent extends BetterPluginEvent {
    private Date disabledAt;

    public PluginDisableEvent(BetterPlugin plugin) {
        super(plugin);

        this.disabledAt = new Date();
    }
}
