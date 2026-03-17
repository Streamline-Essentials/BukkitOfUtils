package host.plas.bou.events;

import org.bukkit.event.Listener;
import gg.drak.thebase.events.BaseEventListener;

/**
 * A combined listener interface that merges Bukkit's {@link Listener}
 * and the base event system's {@link BaseEventListener} into a single type.
 */
public interface ListenerConglomerate extends Listener, BaseEventListener {
}
