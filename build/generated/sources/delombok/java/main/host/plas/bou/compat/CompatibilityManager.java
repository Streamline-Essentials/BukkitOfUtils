package host.plas.bou.compat;

import gg.drak.thebase.objects.handling.IEventable;

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
        return !isEnabled(identifier);
    }

    public IEventable getEventable() {
        return this.eventable;
    }

    public int getId() {
        return this.id;
    }

    public void setEventable(final IEventable eventable) {
        this.eventable = eventable;
    }

    public void setId(final int id) {
        this.id = id;
    }
}
