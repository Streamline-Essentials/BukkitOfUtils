package host.plas.bou.compat.floodgate;

import host.plas.bou.profiles.UuidType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * Optional bridge to Floodgate (and, without Floodgate, to Geyser's own API) for Bedrock players.
 * <p>
 * Everything is reached through reflection against the plugins' own class loaders, so BOU has no
 * compile-time dependency on them and every method degrades to "unknown" when neither is installed.
 * Floodgate frequently runs only on the proxy; callers should fall back to
 * {@link UuidType#of(UUID)}, which recognises Floodgate UUIDs without any plugin present.
 */
public final class FloodgateCompat {
    private FloodgateCompat() {}

    /** Floodgate's default username prefix for Bedrock players. */
    public static final String DEFAULT_PREFIX = ".";

    private static volatile boolean resolved;
    private static volatile Object floodgateApi;
    private static volatile Object geyserApi;

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;

        floodgateApi = load("floodgate", "org.geysermc.floodgate.api.FloodgateApi", "getInstance");
        if (floodgateApi == null) {
            geyserApi = load("Geyser-Spigot", "org.geysermc.geyser.api.GeyserApi", "api");
        }
    }

    private static Object load(String pluginName, String className, String accessor) {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin == null) return null;

            Class<?> clazz = Class.forName(className, true, plugin.getClass().getClassLoader());
            return clazz.getMethod(accessor).invoke(null);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Forgets the cached API handles so they are looked up again on next use
     * (for example after Floodgate is reloaded).
     */
    public static synchronized void reset() {
        resolved = false;
        floodgateApi = null;
        geyserApi = null;
    }

    /**
     * Whether the Floodgate API is available on this server.
     *
     * @return true if Floodgate is installed and its API resolved
     */
    public static boolean isFloodgatePresent() {
        resolve();
        return floodgateApi != null;
    }

    /**
     * Whether Floodgate or Geyser's API is available on this server.
     *
     * @return true if either API resolved
     */
    public static boolean isAvailable() {
        resolve();
        return floodgateApi != null || geyserApi != null;
    }

    private static Object call(Object target, String method, Class<?>[] types, Object... args) {
        if (target == null) return null;
        try {
            Method m = target.getClass().getMethod(method, types);
            m.setAccessible(true);
            return m.invoke(target, args);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object call(Object target, String method) {
        return call(target, method, new Class<?>[0]);
    }

    private static Object floodgatePlayer(UUID uuid) {
        resolve();
        return call(floodgateApi, "getPlayer", new Class<?>[]{UUID.class}, uuid);
    }

    private static Object geyserConnection(UUID uuid) {
        resolve();
        return call(geyserApi, "connectionByUuid", new Class<?>[]{UUID.class}, uuid);
    }

    /**
     * Returns the prefix Floodgate puts in front of Bedrock usernames.
     *
     * @return the live prefix from Floodgate, or empty when Floodgate is not on this server
     */
    public static Optional<String> getPlayerPrefix() {
        resolve();
        Object prefix = call(floodgateApi, "getPlayerPrefix");
        return prefix instanceof String ? Optional.of((String) prefix) : Optional.empty();
    }

    /**
     * Whether an online player is connected through Geyser/Floodgate, according to the API.
     * Linked players (Bedrock accounts linked to a Java account) count as Bedrock here even
     * though their UUID is a Java UUID.
     *
     * @param uuid the player's UUID
     * @return true if the API reports a Bedrock player; false if not or unknown
     */
    public static boolean isBedrockPlayer(UUID uuid) {
        if (uuid == null) return false;
        resolve();
        if (floodgateApi != null) {
            return Boolean.TRUE.equals(call(floodgateApi, "isFloodgatePlayer", new Class<?>[]{UUID.class}, uuid));
        }
        if (geyserApi != null) {
            return Boolean.TRUE.equals(call(geyserApi, "isBedrockPlayer", new Class<?>[]{UUID.class}, uuid));
        }
        return false;
    }

    /**
     * Whether an online Bedrock player is linked to a Java account. Linked players use the Java
     * account's UUID, name, and skin, so Java profile services should be used for them.
     *
     * @param uuid the player's UUID
     * @return true if Floodgate reports the player as linked
     */
    public static boolean isLinked(UUID uuid) {
        return Boolean.TRUE.equals(call(floodgatePlayer(uuid), "isLinked"));
    }

    /**
     * Returns the online Bedrock player's XUID.
     *
     * @param uuid the player's UUID
     * @return the XUID, or empty if unknown
     */
    public static Optional<Long> getXuid(UUID uuid) {
        Object xuid = call(floodgatePlayer(uuid), "getXuid");
        if (xuid == null) xuid = call(geyserConnection(uuid), "xuid");
        if (xuid == null) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(xuid.toString()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the online Bedrock player's gamertag (without the Floodgate prefix).
     *
     * @param uuid the player's UUID
     * @return the gamertag, or empty if unknown
     */
    public static Optional<String> getGamertag(UUID uuid) {
        Object name = call(floodgatePlayer(uuid), "getUsername");
        if (name == null) name = call(geyserConnection(uuid), "bedrockUsername");
        return name instanceof String ? Optional.of((String) name) : Optional.empty();
    }

    /**
     * Returns the name the server uses for an online Bedrock player (normally prefixed).
     *
     * @param uuid the player's UUID
     * @return the Java-side username, or empty if unknown
     */
    public static Optional<String> getJavaUsername(UUID uuid) {
        Object name = call(floodgatePlayer(uuid), "getJavaUsername");
        if (name == null) name = call(geyserConnection(uuid), "javaUsername");
        return name instanceof String ? Optional.of((String) name) : Optional.empty();
    }
}
