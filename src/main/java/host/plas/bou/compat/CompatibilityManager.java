package host.plas.bou.compat;

import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.handling.IEventable;

@Getter @Setter
public abstract class CompatibilityManager {
    private IEventable eventable;
    private int id;

    public void register(IEventable eventable) {
        this.eventable = eventable;
        id = CompatManager.registerManager(this);

        init();
    }

    public abstract void init();

    public static void putHolder(String identifier, HeldHolder holder) {
        host.plas.bou.compat.CompatManager.getHolders().put(identifier, holder);
    }

    public static HeldHolder getHolder(String identifier) {
        return host.plas.bou.compat.CompatManager.getHolders().get(identifier);
    }

    public static boolean isEnabled(String identifier) {
        return getHolder(identifier) != null && getHolder(identifier).isEnabled();
    }

    public static boolean isDisabled(String identifier) {
        return ! isEnabled(identifier);
    }
}
