package host.plas.bou.compat;

import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.handling.IEventable;

/**
 * Abstract base class for managing compatibility with external plugins or APIs.
 * Subclasses implement the {@link #init()} method to perform specific initialization logic.
 */
@Getter @Setter
public abstract class CompatibilityManager {
    /**
     * Protected no-arg constructor for subclass instantiation.
     */
    protected CompatibilityManager() {
        // Default constructor for subclasses
    }

    /**
     * The eventable context associated with this compatibility manager.
     *
     * @param eventable the eventable to set
     * @return the eventable
     */
    private IEventable eventable;
    /**
     * The unique ID assigned to this compatibility manager.
     *
     * @param id the id to set
     * @return the id
     */
    private int id;

    /**
     * Registers this compatibility manager with the given eventable context,
     * assigns it an ID via {@link CompatManager}, and calls {@link #init()}.
     *
     * @param eventable the eventable context to associate with this manager
     */
    public void register(IEventable eventable) {
        this.eventable = eventable;
        id = CompatManager.registerManager(this);

        init();
    }

    /**
     * Initializes the compatibility manager. Subclasses should implement this
     * to perform any setup required for their specific compatibility integration.
     */
    public abstract void init();

    /**
     * Stores a held holder in the global compatibility manager's holders map.
     *
     * @param identifier the identifier key for the holder
     * @param holder the held holder to store
     */
    public static void putHolder(String identifier, HeldHolder holder) {
        host.plas.bou.compat.CompatManager.getHolders().put(identifier, holder);
    }

    /**
     * Retrieves a held holder from the global compatibility manager's holders map.
     *
     * @param identifier the identifier of the holder to retrieve
     * @return the held holder, or null if not found
     */
    public static HeldHolder getHolder(String identifier) {
        return host.plas.bou.compat.CompatManager.getHolders().get(identifier);
    }

    /**
     * Checks whether a holder with the given identifier is registered and enabled.
     *
     * @param identifier the identifier to check
     * @return true if the holder exists and is enabled, false otherwise
     */
    public static boolean isEnabled(String identifier) {
        return getHolder(identifier) != null && getHolder(identifier).isEnabled();
    }

    /**
     * Checks whether a holder with the given identifier is not enabled or not registered.
     *
     * @param identifier the identifier to check
     * @return true if the holder is disabled or not found, false otherwise
     */
    public static boolean isDisabled(String identifier) {
        return ! isEnabled(identifier);
    }
}
