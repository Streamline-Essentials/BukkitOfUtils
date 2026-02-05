package host.plas.bou.events.self;

import host.plas.bou.BetterPlugin;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BetterPluginEvent extends BouEvent {
    private BetterPlugin plugin;

    public BetterPluginEvent(BetterPlugin plugin) {
        super();

        this.plugin = plugin;
    }
}
