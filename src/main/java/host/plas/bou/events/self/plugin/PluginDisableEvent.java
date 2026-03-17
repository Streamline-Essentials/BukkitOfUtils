package host.plas.bou.events.self.plugin;

import host.plas.bou.BetterPlugin;
import host.plas.bou.events.self.BetterPluginEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Event fired when a {@link BetterPlugin} is being disabled.
 * Records the timestamp at which the disable occurred.
 */
@Getter @Setter
public class PluginDisableEvent extends BetterPluginEvent {
    /**
     * The timestamp at which the plugin was disabled.
     *
     * @param disabledAt the disable timestamp to set
     * @return the date when the plugin was disabled
     */
    private Date disabledAt;

    /**
     * Constructs a new PluginDisableEvent for the given plugin.
     * Automatically records the current time as the disable timestamp.
     *
     * @param plugin the BetterPlugin that is being disabled
     */
    public PluginDisableEvent(BetterPlugin plugin) {
        super(plugin);

        this.disabledAt = new Date();
    }
}
