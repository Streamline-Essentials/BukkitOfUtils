package host.plas.bou.events.self;

import host.plas.bou.BetterPlugin;

public class BetterPluginEvent extends BouEvent {
    private BetterPlugin plugin;

    public BetterPluginEvent(BetterPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public BetterPlugin getPlugin() {
        return this.plugin;
    }

    public void setPlugin(final BetterPlugin plugin) {
        this.plugin = plugin;
    }
}
