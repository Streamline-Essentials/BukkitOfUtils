package host.plas.bou.utils;

import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Supplier;

/**
 * Utility class for checking class availability at runtime,
 * detecting server types (Folia, Paper), and retrieving the server version.
 */
public class ClassHelper {
    /** Cache mapping fully qualified class names to their availability on the classpath. */
    public static final ConcurrentSkipListMap<String, Boolean> PROPERTY_CACHE = new ConcurrentSkipListMap<>();
    /** Cached server version, wrapped in a double Optional to distinguish uninitialized from absent. */
    public static Optional<Optional<String>> SERVER_VERSION = Optional.empty();

    /**
     * Private constructor to prevent instantiation.
     */
    private ClassHelper() {
    }

    /**
     * Checks whether a class exists on the classpath without using any cache.
     *
     * @param fullClass the fully qualified class name to check
     * @return true if the class is found, false otherwise
     */
    public static boolean hasClassNoCache(String fullClass) {
        try {
            Class.forName(fullClass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks whether a class exists on the classpath, using a cache for repeated lookups.
     *
     * @param fullClass the fully qualified class name to check
     * @return true if the class is found, false otherwise
     */
    public static boolean hasClass(String fullClass) {
        Boolean cached = PROPERTY_CACHE.get(fullClass);
        if (cached != null) return cached;

        boolean hasClass = hasClassNoCache(fullClass);
        PROPERTY_CACHE.put(fullClass, hasClass);
        return hasClass;
    }

    /**
     * Checks whether the server is running Folia by looking for the Folia-specific class.
     *
     * @return true if the server is running Folia
     */
    public static boolean isFolia() {
        return hasClass("io.papermc.paper.threadedregions.RegionizedServer");
    }

    /**
     * Checks whether the server is running Paper by looking for the Paper-specific class.
     *
     * @return true if the server is running Paper
     */
    public static boolean isPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig");
    }

    /**
     * Checks whether expanded scheduling APIs are available (Paper/Folia scheduler).
     *
     * @return true if the expanded scheduling API class is present
     */
    public static boolean isExpandedSchedulingAvailable() {
        return hasClass("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
    }

    /**
     * Retrieves the server version string from the CraftBukkit package name.
     * The result is cached after the first call.
     *
     * @return an Optional containing the server version string, or empty if it cannot be determined
     */
    public static Optional<String> getServerVersion() {
        if (SERVER_VERSION.isPresent()) return SERVER_VERSION.get();

        String v = null;
        try {
            v = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore
        }

        Optional<String> optional = Optional.ofNullable(v);
        SERVER_VERSION = Optional.of(optional);
        return optional;
    }

    /**
     * Initializes the ClassHelper by eagerly resolving and caching the server version.
     */
    public static void init() {
        getServerVersion();
    }

    /**
     * Executes the given runnable if the server is running Folia.
     *
     * @param ifTrue the runnable to execute if Folia is detected
     */
    public static void ifFolia(Runnable ifTrue) {
        if (isFolia()) {
            ifTrue.run();
        }
    }

    /**
     * Executes one of two runnables depending on whether the server is running Folia.
     *
     * @param ifTrue  the runnable to execute if Folia is detected
     * @param ifFalse the runnable to execute if Folia is not detected
     */
    public static void ifFoliaOrElse(Runnable ifTrue, Runnable ifFalse) {
        if (isFolia()) {
            ifTrue.run();
        } else {
            ifFalse.run();
        }
    }

    /**
     * Returns a value from a supplier if the server is running Folia, or a default value otherwise.
     *
     * @param <C>     the type of the value to return
     * @param ifTrue  the supplier to invoke if Folia is detected
     * @param ifFalse the default value to return if Folia is not detected
     * @return the result from the supplier if Folia, or the default value
     */
    public static <C> C ifFolia(Supplier<C> ifTrue, C ifFalse) {
        if (isFolia()) {
            return ifTrue.get();
        }

        return ifFalse;
    }

    /**
     * Returns a value from one of two suppliers depending on whether the server is running Folia.
     *
     * @param <C>     the type of the value to return
     * @param ifTrue  the supplier to invoke if Folia is detected
     * @param ifFalse the supplier to invoke if Folia is not detected
     * @return the result from the appropriate supplier
     */
    public static <C> C ifFoliaOrElse(Supplier<C> ifTrue, Supplier<C> ifFalse) {
        if (isFolia()) {
            return ifTrue.get();
        } else {
            return ifFalse.get();
        }
    }
}
