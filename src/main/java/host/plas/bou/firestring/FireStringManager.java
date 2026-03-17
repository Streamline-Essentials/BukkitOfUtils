package host.plas.bou.firestring;

import host.plas.bou.BukkitOfUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Manages the registration, retrieval, and firing of {@link FireString} instances.
 * Provides static methods for managing fire strings and initializes built-in fire strings.
 */
public class FireStringManager {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FireStringManager() {
        // utility class
    }

    /**
     * The set of registered fire strings managed by this manager.
     *
     * @param fireStrings the set of fire strings to use
     * @return the current set of registered fire strings
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<FireString> fireStrings = new ConcurrentSkipListSet<>();

    /**
     * Registers a fire string, replacing any existing one with the same identifier.
     *
     * @param fireString the fire string to register
     */
    public static void register(FireString fireString) {
        unregister(fireString.getIdentifier());

        getFireStrings().add(fireString);

        BukkitOfUtils.getInstance().logInfo("&7> &6(&b" + fireString.getIdentifier() + "&6) &cFireString &fRegistered&7!");
    }

    /**
     * Unregisters a fire string by its identifier.
     *
     * @param identifier the identifier of the fire string to remove
     */
    public static void unregister(String identifier) {
        getFireStrings().removeIf(fireString -> fireString.getIdentifier().equals(identifier));
    }

    /**
     * Retrieves a fire string by its identifier.
     *
     * @param identifier the identifier to search for
     * @return an {@link Optional} containing the fire string if found, or empty otherwise
     */
    public static Optional<FireString> get(String identifier) {
        return getFireStrings().stream().filter(fireString -> fireString.getIdentifier().equals(identifier)).findFirst();
    }

    /**
     * Attempts to fire all registered fire strings with the given input string.
     * Each fire string will check if its identifier matches the parsed input.
     *
     * @param string the formatted string to fire (expected format: "(identifier) value")
     */
    public static void fire(String string) {
        fireStrings.forEach(fireString -> {
            try {
                if (fireString.checkAndFire(string)) {
                    // do nothing
                }
            } catch (Exception e) {
                BukkitOfUtils.getInstance().logWarning("Failed to fire string for " + fireString.getIdentifier() + " with string " + string);
                BukkitOfUtils.getInstance().logWarning(e);
            }
        });
    }

    /**
     * Initializes the fire string manager by registering all built-in fire strings
     * defined in {@link BuiltIn}.
     */
    public static void init() {
        Arrays.stream(BuiltIn.values()).forEach(builtIn -> {
            FireString fireString = new FireString(builtIn.getIdentifier(), builtIn.getConsumer(), false);

            register(fireString);
        });

        BukkitOfUtils.getInstance().logInfo("Initialized &cFireStringManager &fwith &a" + fireStrings.size() + " &fFireStrings");
    }
}
