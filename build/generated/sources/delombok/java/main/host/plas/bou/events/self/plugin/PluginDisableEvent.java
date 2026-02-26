package host.plas.bou.events.self.plugin;

import host.plas.bou.BetterPlugin;
import host.plas.bou.events.self.BetterPluginEvent;
import java.util.Date;

public class PluginDisableEvent extends BetterPluginEvent {
    private Date disabledAt;

    public PluginDisableEvent(BetterPlugin plugin) {
        super(plugin);
        this.disabledAt = new Date();
    }

    public Date getDisabledAt() {
        return this.disabledAt;
    }

    public void setDisabledAt(final Date disabledAt) {
        this.disabledAt = disabledAt;
    }
}
