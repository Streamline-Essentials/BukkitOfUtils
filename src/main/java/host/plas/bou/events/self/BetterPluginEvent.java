package host.plas.bou.events.self;

import host.plas.bou.BetterPlugin;
import lombok.Getter;
import lombok.Setter;

/**
 * Base event class for events associated with a specific {@link BetterPlugin} instance.
 */
@Getter @Setter
public class BetterPluginEvent extends BouEvent {
    /**
     * The BetterPlugin instance associated with this event.
     * @param plugin the plugin to set
     * @return the associated plugin
     */
    private BetterPlugin plugin;

    /**
     * Constructs a new BetterPluginEvent for the given plugin.
     *
     * @param plugin the BetterPlugin instance associated with this event
     */
    public BetterPluginEvent(BetterPlugin plugin) {
        super();

        this.plugin = plugin;
    }
}
