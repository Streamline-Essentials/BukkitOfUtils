package host.plas.bou.profiles;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.floodgate.FloodgateCompat;
import host.plas.bou.configs.BaseConfig;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.VersionTool;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Player name, UUID, and skin lookups that never touch Mojang's API.
 * <p>
 * Resolution order:
 * <ol>
 *     <li>Data already on the server: the online player's live profile, then the server's player cache.</li>
 *     <li>Floodgate/Geyser on this server, for Bedrock players.</li>
 *     <li>The Geyser global API for Bedrock (Floodgate) UUIDs and prefixed names.</li>
 *     <li>PlayerDB, then MineTools, for Java accounts only.</li>
 * </ol>
 * Floodgate and offline-mode UUIDs are never sent to Java profile services; nothing knows them
 * there, and asking only burns rate limit. Each web provider has its own exponential backoff, and
 * {@code player-lookups.web.enabled: false} in BOU's base config keeps every lookup on-server,
 * so offline-mode and no-internet servers get fast local answers instead of hanging requests.
 * <p>
 * Lookups run on a small dedicated thread pool and are cached (found, missing, and failed answers
 * each with their own TTL). Concurrent lookups for the same key share one request.
 */
public final class PlayerLookup {
    private PlayerLookup() {}

    /** Characters a Java Edition account name can contain. */
    private static final Pattern JAVA_NAME = Pattern.compile("^[A-Za-z0-9_]{1,16}$");
    private static final int MAX_CACHE_ENTRIES = 20_000;

    private static final Map<UUID, CacheEntry> BY_UUID = new ConcurrentHashMap<>();
    private static final Map<String, CacheEntry> BY_NAME = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<LookupResult>> IN_FLIGHT = new ConcurrentHashMap<>();

    private static final Object EXECUTOR_LOCK = new Object();
    private static ExecutorService executor;

    private static volatile Boolean proxied;

    private static final class CacheEntry {
        final LookupResult result;
        final long expiresAt;

        CacheEntry(LookupResult result, long expiresAt) {
            this.result = result;
            this.expiresAt = expiresAt;
        }

        boolean isValid() {
            return System.currentTimeMillis() < expiresAt;
        }
    }

    /** A point-in-time copy of the lookup settings, so one lookup reads the config once. */
    private static final class Settings {
        boolean web = true;
        int timeout = 4000;
        long foundMillis = TimeUnit.HOURS.toMillis(6);
        long missingMillis = TimeUnit.MINUTES.toMillis(30);
        long failedMillis = TimeUnit.MINUTES.toMillis(5);
        long maxBackoffMillis = TimeUnit.MINUTES.toMillis(30);
        String userAgent = "BukkitOfUtils";

        static Settings read() {
            Settings s = new Settings();
            try {
                BaseConfig config = BaseManager.getBaseConfig();
                s.web = config.isPlayerLookupWebEnabled();
                s.timeout = Math.max(500, config.getPlayerLookupTimeoutMillis());
                s.foundMillis = TimeUnit.MINUTES.toMillis(Math.max(0, config.getPlayerLookupFoundCacheMinutes()));
                s.missingMillis = TimeUnit.MINUTES.toMillis(Math.max(0, config.getPlayerLookupMissingCacheMinutes()));
                s.failedMillis = TimeUnit.MINUTES.toMillis(Math.max(0, config.getPlayerLookupFailedCacheMinutes()));
                s.maxBackoffMillis = TimeUnit.MINUTES.toMillis(Math.max(1, config.getPlayerLookupMaxBackoffMinutes()));
            } catch (Throwable ignored) {
                // Config not loaded yet: the defaults above apply.
            }
            try {
                s.userAgent = "BukkitOfUtils/" + BukkitOfUtils.getInstance().getDescription().getVersion();
            } catch (Throwable ignored) {
                // Keep the bare user agent.
            }
            return s;
        }

        long ttlFor(LookupResult result) {
            switch (result.getStatus()) {
                case FOUND:
                    return result.isComplete() ? foundMillis : failedMillis;
                case NOT_FOUND:
                    return missingMillis;
                default:
                    return failedMillis;
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // Server state
    // ------------------------------------------------------------------------------------------

    /**
     * Whether the server authenticates players itself ({@code online-mode=true}).
     *
     * @return true in online mode
     */
    public static boolean isOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    /**
     * Whether the server sits behind a BungeeCord/Velocity proxy that forwards real UUIDs.
     * Such servers report {@code online-mode=false} but still see Mojang (and Floodgate) UUIDs.
     *
     * @return true if proxy forwarding is enabled
     */
    public static boolean isProxied() {
        Boolean cached = proxied;
        if (cached != null) return cached;

        boolean result = false;
        try {
            result = Bukkit.spigot().getConfig().getBoolean("settings.bungeecord", false);
        } catch (Throwable ignored) {
            // Not Spigot.
        }
        if (!result) {
            try {
                Object spigot = Bukkit.spigot();
                Object paperConfig = spigot.getClass().getMethod("getPaperConfig").invoke(spigot);
                if (paperConfig instanceof YamlConfiguration) {
                    YamlConfiguration yaml = (YamlConfiguration) paperConfig;
                    result = yaml.getBoolean("settings.velocity-support.enabled", false)
                            || yaml.getBoolean("proxies.velocity.enabled", false);
                }
            } catch (Throwable ignored) {
                // Not Paper, or a Paper without this accessor.
            }
        }
        if (!result) {
            try {
                File global = new File("config", "paper-global.yml");
                if (global.isFile()) {
                    result = YamlConfiguration.loadConfiguration(global).getBoolean("proxies.velocity.enabled", false);
                }
            } catch (Throwable ignored) {
                // Unreadable; assume not proxied.
            }
        }

        proxied = result;
        return result;
    }

    /**
     * Whether player UUIDs on this server are real account UUIDs (online mode or proxied).
     * When false, every Java player has an offline-mode UUID derived from their name.
     *
     * @return true if UUIDs come from authenticated accounts
     */
    public static boolean usesAccountUuids() {
        return isOnlineMode() || isProxied();
    }

    /**
     * Returns the Floodgate username prefix: Floodgate's live value when it is installed here,
     * otherwise the configured fallback ({@code player-lookups.bedrock.prefix}).
     *
     * @return the prefix, possibly empty
     */
    public static String getBedrockPrefix() {
        Optional<String> live = FloodgateCompat.getPlayerPrefix();
        if (live.isPresent()) return live.get();
        try {
            return BaseManager.getBaseConfig().getBedrockPrefixFallback();
        } catch (Throwable t) {
            return FloodgateCompat.DEFAULT_PREFIX;
        }
    }

    /**
     * Whether the UUID belongs to a Bedrock player: a Floodgate UUID, or a player the
     * Floodgate/Geyser API on this server reports as Bedrock (which includes linked players).
     *
     * @param uuid the UUID
     * @return true for Bedrock players
     */
    public static boolean isBedrock(UUID uuid) {
        return UuidType.of(uuid) == UuidType.BEDROCK || FloodgateCompat.isBedrockPlayer(uuid);
    }

    /**
     * Whether a name carries the Bedrock prefix (and therefore cannot be a Java account name,
     * when the prefix contains a character Java names cannot use).
     *
     * @param name the name
     * @return true if the name starts with the Bedrock prefix
     */
    public static boolean hasBedrockPrefix(String name) {
        String prefix = getBedrockPrefix();
        return name != null && !prefix.isEmpty() && name.length() > prefix.length() && name.startsWith(prefix);
    }

    /**
     * Whether Mojang's session server can know this UUID. Use this to guard any
     * {@code PlayerProfile#update()} / {@code complete()} call: for Floodgate and offline-mode
     * UUIDs Mojang always fails, and each failure is a logged stack trace and rate-limit hit.
     *
     * @param uuid the UUID
     * @return true only for real Java account UUIDs
     */
    public static boolean isMojangResolvable(UUID uuid) {
        return UuidType.of(uuid).isJavaResolvable();
    }

    // ------------------------------------------------------------------------------------------
    // Public lookups
    // ------------------------------------------------------------------------------------------

    /**
     * Resolves a player by UUID, asynchronously.
     *
     * @param uuid the player's UUID
     * @return a future with the lookup result; never completes exceptionally
     */
    public static CompletableFuture<LookupResult> resolve(UUID uuid) {
        if (uuid == null) return CompletableFuture.completedFuture(LookupResult.notFound());

        CacheEntry entry = BY_UUID.get(uuid);
        if (entry != null && entry.isValid()) return CompletableFuture.completedFuture(entry.result);

        return submit("u:" + uuid, () -> {
            Settings settings = Settings.read();
            LookupResult result = resolveUuidNow(uuid, settings);
            store(result, uuid, null, settings);
            return result;
        });
    }

    /**
     * Resolves a player by name, asynchronously. Bedrock names are recognised by the Floodgate prefix.
     * On offline-mode servers (not behind a proxy) the UUID is the offline-mode UUID for the name.
     *
     * @param name the player's name
     * @return a future with the lookup result; never completes exceptionally
     */
    public static CompletableFuture<LookupResult> resolve(String name) {
        if (name == null || name.isBlank()) return CompletableFuture.completedFuture(LookupResult.notFound());

        String key = name.toLowerCase(Locale.ROOT);
        CacheEntry entry = BY_NAME.get(key);
        if (entry != null && entry.isValid()) return CompletableFuture.completedFuture(entry.result);

        return submit("n:" + key, () -> {
            Settings settings = Settings.read();
            LookupResult result = resolveNameNow(name, settings);
            store(result, null, name, settings);
            return result;
        });
    }

    /**
     * Looks up a player by UUID, asynchronously.
     *
     * @param uuid the player's UUID
     * @return a future with the profile, empty if unknown or unreachable
     */
    public static CompletableFuture<Optional<ProfileData>> lookup(UUID uuid) {
        return resolve(uuid).thenApply(LookupResult::getProfile);
    }

    /**
     * Looks up a player by name, asynchronously.
     *
     * @param name the player's name
     * @return a future with the profile, empty if unknown or unreachable
     */
    public static CompletableFuture<Optional<ProfileData>> lookup(String name) {
        return resolve(name).thenApply(LookupResult::getProfile);
    }

    /**
     * Resolves a player by UUID, blocking for at most the given time. Avoid calling this on the
     * main thread; on timeout the lookup keeps running and fills the cache for later calls.
     *
     * @param uuid          the player's UUID
     * @param timeoutMillis the longest to wait
     * @return the result, or {@link LookupResult.Status#UNAVAILABLE} on timeout
     */
    public static LookupResult resolveBlocking(UUID uuid, long timeoutMillis) {
        return await(resolve(uuid), timeoutMillis);
    }

    /**
     * Resolves a player by name, blocking for at most the given time. Avoid calling this on the
     * main thread; on timeout the lookup keeps running and fills the cache for later calls.
     *
     * @param name          the player's name
     * @param timeoutMillis the longest to wait
     * @return the result, or {@link LookupResult.Status#UNAVAILABLE} on timeout
     */
    public static LookupResult resolveBlocking(String name, long timeoutMillis) {
        return await(resolve(name), timeoutMillis);
    }

    /**
     * Returns a cached profile without doing any work.
     *
     * @param uuid the player's UUID
     * @return the cached profile, if any
     */
    public static Optional<ProfileData> getCached(UUID uuid) {
        CacheEntry entry = uuid == null ? null : BY_UUID.get(uuid);
        return entry != null && entry.isValid() ? entry.result.getProfile() : Optional.empty();
    }

    /**
     * Returns a cached profile without doing any work.
     *
     * @param name the player's name
     * @return the cached profile, if any
     */
    public static Optional<ProfileData> getCached(String name) {
        CacheEntry entry = name == null ? null : BY_NAME.get(name.toLowerCase(Locale.ROOT));
        return entry != null && entry.isValid() ? entry.result.getProfile() : Optional.empty();
    }

    /**
     * Returns a player's UUID when it is known or derivable without a network request: cached,
     * online, in the server's user cache, or (on offline-mode servers) the offline-mode UUID.
     *
     * @param name the player's name
     * @return the UUID, or empty when only a web lookup could tell
     */
    public static Optional<UUID> getUuidWithoutLookup(String name) {
        if (name == null || name.isBlank()) return Optional.empty();

        Optional<UUID> cached = getCached(name).map(ProfileData::getUuid);
        if (cached.isPresent()) return cached;

        ProfileData local = localByName(name);
        if (local != null && local.getUuid() != null) return Optional.of(local.getUuid());

        if (!usesAccountUuids() && !hasBedrockPrefix(name)) return Optional.of(UuidType.offlineUuid(name));
        return Optional.empty();
    }

    /**
     * Returns a player's name when it is known without a network request: cached, online, or in
     * the server's player data.
     *
     * @param uuid the player's UUID
     * @return the name, or empty when only a web lookup could tell
     */
    public static Optional<String> getNameWithoutLookup(UUID uuid) {
        if (uuid == null) return Optional.empty();

        Optional<String> cached = getCached(uuid).map(ProfileData::getName);
        if (cached.isPresent()) return cached;

        ProfileData local = localByUuid(uuid);
        return local == null ? Optional.empty() : Optional.ofNullable(local.getName());
    }

    /**
     * Returns what the server itself knows about a UUID: the online player (with their live skin),
     * or the name from the server's player data. Never makes a network request.
     *
     * @param uuid the player's UUID
     * @return local profile data, if any
     */
    public static Optional<ProfileData> getLocal(UUID uuid) {
        return Optional.ofNullable(localByUuid(uuid));
    }

    /**
     * Returns what the server itself knows about a name: an online player with that exact name, or
     * (on Paper) a cached offline player. Never makes a network request.
     *
     * @param name the player's name
     * @return local profile data, if any
     */
    public static Optional<ProfileData> getLocal(String name) {
        return Optional.ofNullable(localByName(name));
    }

    /**
     * Drops cached data for a UUID.
     *
     * @param uuid the UUID
     */
    public static void invalidate(UUID uuid) {
        if (uuid == null) return;
        CacheEntry entry = BY_UUID.remove(uuid);
        if (entry != null) entry.result.getProfile().map(ProfileData::getName).ifPresent(PlayerLookup::invalidateName);
    }

    /**
     * Drops cached data for a name.
     *
     * @param name the name
     */
    public static void invalidate(String name) {
        if (name == null) return;
        CacheEntry entry = BY_NAME.remove(name.toLowerCase(Locale.ROOT));
        if (entry != null) entry.result.getProfile().map(ProfileData::getUuid).ifPresent(BY_UUID::remove);
    }

    private static void invalidateName(String name) {
        BY_NAME.remove(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Clears every cache, provider backoff, and the detected proxy state.
     */
    public static void clearCache() {
        BY_UUID.clear();
        BY_NAME.clear();
        LookupProviders.GEYSER.reset();
        LookupProviders.PLAYERDB.reset();
        LookupProviders.MINETOOLS.reset();
        proxied = null;
        FloodgateCompat.reset();
    }

    /**
     * Stops the lookup threads. Pending lookups complete as unavailable.
     */
    public static void shutdown() {
        synchronized (EXECUTOR_LOCK) {
            if (executor != null) {
                executor.shutdownNow();
                executor = null;
            }
        }
        IN_FLIGHT.values().forEach(f -> f.complete(LookupResult.unavailable()));
        IN_FLIGHT.clear();
    }

    // ------------------------------------------------------------------------------------------
    // Execution and caching
    // ------------------------------------------------------------------------------------------

    private interface Work {
        LookupResult run();
    }

    private static ExecutorService executor() {
        synchronized (EXECUTOR_LOCK) {
            if (executor == null || executor.isShutdown()) {
                AtomicInteger counter = new AtomicInteger();
                ThreadFactory factory = runnable -> {
                    Thread thread = new Thread(runnable, "BOU-PlayerLookup-" + counter.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                };
                executor = Executors.newFixedThreadPool(3, factory);
            }
            return executor;
        }
    }

    private static CompletableFuture<LookupResult> submit(String key, Work work) {
        CompletableFuture<LookupResult> created = new CompletableFuture<>();
        CompletableFuture<LookupResult> existing = IN_FLIGHT.putIfAbsent(key, created);
        if (existing != null) return existing;

        try {
            executor().execute(() -> {
                LookupResult result;
                try {
                    result = work.run();
                } catch (Throwable t) {
                    debug("Player lookup for " + key + " failed: " + t);
                    result = LookupResult.unavailable();
                }
                IN_FLIGHT.remove(key, created);
                created.complete(result);
            });
        } catch (Throwable rejected) {
            IN_FLIGHT.remove(key, created);
            created.complete(LookupResult.unavailable());
        }
        return created;
    }

    private static LookupResult await(CompletableFuture<LookupResult> future, long timeoutMillis) {
        try {
            return future.get(Math.max(0, timeoutMillis), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return LookupResult.unavailable();
        } catch (Throwable t) {
            return LookupResult.unavailable();
        }
    }

    private static void store(LookupResult result, UUID uuid, String name, Settings settings) {
        long ttl = settings.ttlFor(result);
        if (ttl <= 0) return;

        if (BY_UUID.size() + BY_NAME.size() > MAX_CACHE_ENTRIES) {
            BY_UUID.values().removeIf(e -> !e.isValid());
            BY_NAME.values().removeIf(e -> !e.isValid());
            if (BY_UUID.size() + BY_NAME.size() > MAX_CACHE_ENTRIES) {
                BY_UUID.clear();
                BY_NAME.clear();
            }
        }

        CacheEntry entry = new CacheEntry(result, System.currentTimeMillis() + ttl);
        if (uuid != null) BY_UUID.put(uuid, entry);
        if (name != null) BY_NAME.put(name.toLowerCase(Locale.ROOT), entry);

        result.getProfile().ifPresent(profile -> {
            if (profile.getUuid() != null) BY_UUID.put(profile.getUuid(), entry);
            if (profile.getName() != null) BY_NAME.put(profile.getName().toLowerCase(Locale.ROOT), entry);
        });
    }

    private static void debug(String message) {
        try {
            BukkitOfUtils.getInstance().logDebug(message);
        } catch (Throwable ignored) {
            // Plugin not initialised.
        }
    }

    // ------------------------------------------------------------------------------------------
    // Local data
    // ------------------------------------------------------------------------------------------

    private static ProfileData localByUuid(UUID uuid) {
        if (uuid == null) return null;
        try {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                String[] textures = readTextures(player);
                return new ProfileData(uuid, player.getName(), textures[0], textures[1], LookupSource.ONLINE_PLAYER);
            }
        } catch (Throwable ignored) {
            // Fall through to the player cache.
        }
        try {
            // getOfflinePlayer(UUID) only reads local player data; unlike getOfflinePlayer(String)
            // it never queries Mojang.
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            String name = offline.getName();
            if (name != null && !name.isBlank()) return new ProfileData(uuid, name, null, null, LookupSource.SERVER_CACHE);
        } catch (Throwable ignored) {
            // No local data.
        }
        return null;
    }

    private static Method getOfflinePlayerIfCached;
    private static boolean checkedOfflinePlayerIfCached;

    private static ProfileData localByName(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            Player player = Bukkit.getPlayerExact(name);
            if (player != null) {
                String[] textures = readTextures(player);
                return new ProfileData(player.getUniqueId(), player.getName(), textures[0], textures[1], LookupSource.ONLINE_PLAYER);
            }
        } catch (Throwable ignored) {
            // Fall through to the player cache.
        }
        try {
            // Paper's getOfflinePlayerIfCached reads the user cache without the Mojang lookup
            // that Bukkit.getOfflinePlayer(String) performs on online-mode servers.
            if (!checkedOfflinePlayerIfCached) {
                checkedOfflinePlayerIfCached = true;
                getOfflinePlayerIfCached = Bukkit.getServer().getClass().getMethod("getOfflinePlayerIfCached", String.class);
            }
            if (getOfflinePlayerIfCached != null) {
                Object result = getOfflinePlayerIfCached.invoke(Bukkit.getServer(), name);
                if (result instanceof OfflinePlayer) {
                    OfflinePlayer offline = (OfflinePlayer) result;
                    String cachedName = offline.getName();
                    return new ProfileData(offline.getUniqueId(), cachedName != null ? cachedName : name, null, null, LookupSource.SERVER_CACHE);
                }
            }
        } catch (Throwable ignored) {
            // Not Paper.
        }
        return null;
    }

    /**
     * Reads the {@code textures} property from an online player's live game profile. Tolerates
     * both the getter-style (authlib before 6) and record-style (authlib 6+) accessors.
     *
     * @return {value, signature}; both null when unavailable
     */
    private static String[] readTextures(Player player) {
        String[] out = new String[2];
        try {
            Object profile = VersionTool.getCraftPlayerGetGameProfileMethod().invoke(player);
            Object properties = invokeFirst(profile, "getProperties", "properties");
            if (properties == null) return out;

            Object values = properties.getClass().getMethod("get", Object.class).invoke(properties, "textures");
            if (!(values instanceof Collection)) return out;

            for (Object property : (Collection<?>) values) {
                Object value = invokeFirst(property, "getValue", "value");
                if (value == null) continue;
                Object signature = invokeFirst(property, "getSignature", "signature");
                out[0] = value.toString();
                out[1] = signature == null ? null : signature.toString();
                break;
            }
        } catch (Throwable ignored) {
            // No readable profile on this server version.
        }
        return out;
    }

    private static Object invokeFirst(Object target, String... names) {
        if (target == null) return null;
        for (String name : names) {
            try {
                Method method = target.getClass().getMethod(name);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (NoSuchMethodException ignored) {
                // Try the next accessor name.
            } catch (Throwable t) {
                return null;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------------------------
    // Resolution
    // ------------------------------------------------------------------------------------------

    private interface Call<T> {
        T call() throws IOException;
    }

    /** One provider call: {@code answered} is true when the provider gave a definite answer (value or "no such player"). */
    private static final class Attempt<T> {
        final T value;
        final boolean answered;

        Attempt(T value, boolean answered) {
            this.value = value;
            this.answered = answered;
        }
    }

    private static <T> Attempt<T> attempt(LookupProviders.Gate gate, Settings settings, Call<T> call) {
        if (!settings.web || !gate.isOpen()) return new Attempt<>(null, false);
        try {
            T value = call.call();
            gate.success();
            return new Attempt<>(value, true);
        } catch (Throwable t) {
            gate.failure(settings.maxBackoffMillis);
            debug("Player lookup via " + gate.host + " failed (backing off): " + t.getMessage());
            return new Attempt<>(null, false);
        }
    }

    /** Java profile (with textures) for an account UUID: PlayerDB, then MineTools. */
    private static Attempt<ProfileData> javaByUuid(UUID uuid, Settings settings) {
        Attempt<ProfileData> playerDb = attempt(LookupProviders.PLAYERDB, settings,
                () -> LookupProviders.playerDb(uuid.toString(), settings.timeout, settings.userAgent));
        if (playerDb.value != null && playerDb.value.hasTextures()) return playerDb;
        // MineTools proxies Mojang and reports unknown UUIDs as an upstream error, so asking it
        // after a definite "no such player" would only trip its backoff.
        if (playerDb.answered && playerDb.value == null) return playerDb;

        Attempt<ProfileData> mineTools = attempt(LookupProviders.MINETOOLS, settings,
                () -> LookupProviders.mineToolsProfile(uuid, settings.timeout, settings.userAgent));
        if (mineTools.value != null) return new Attempt<>(mineTools.value.mergeFrom(playerDb.value), true);
        if (playerDb.value != null) return playerDb;
        return new Attempt<>(null, playerDb.answered || mineTools.answered);
    }

    /** Java profile (with textures when reachable) for an account name: PlayerDB, then MineTools. */
    private static Attempt<ProfileData> javaByName(String name, Settings settings) {
        if (!JAVA_NAME.matcher(name).matches()) return new Attempt<>(null, true);

        Attempt<ProfileData> playerDb = attempt(LookupProviders.PLAYERDB, settings,
                () -> LookupProviders.playerDb(name, settings.timeout, settings.userAgent));
        if (playerDb.value != null && playerDb.value.hasTextures()) return playerDb;
        if (playerDb.answered && playerDb.value == null) return playerDb;

        Attempt<ProfileData> uuid = playerDb.value != null ? playerDb : attempt(LookupProviders.MINETOOLS, settings,
                () -> LookupProviders.mineToolsUuid(name, settings.timeout, settings.userAgent));
        if (uuid.value == null) return uuid;

        Attempt<ProfileData> profile = attempt(LookupProviders.MINETOOLS, settings,
                () -> LookupProviders.mineToolsProfile(uuid.value.getUuid(), settings.timeout, settings.userAgent));
        return new Attempt<>(profile.value != null ? profile.value.mergeFrom(uuid.value) : uuid.value, true);
    }

    private static LookupResult resolveUuidNow(UUID uuid, Settings settings) {
        ProfileData local = localByUuid(uuid);
        if (local != null && local.hasTextures()) return LookupResult.found(local, true);

        UuidType type = UuidType.of(uuid);
        ProfileData remote = null;
        boolean answered;

        if (type == UuidType.BEDROCK) {
            long xuid = UuidType.toXuid(uuid);

            Attempt<ProfileData> skin = attempt(LookupProviders.GEYSER, settings,
                    () -> LookupProviders.geyserSkin(xuid, settings.timeout, settings.userAgent));
            answered = skin.answered;
            remote = skin.value;

            String name = local != null ? local.getName() : null;
            if (name == null) {
                Optional<String> gamertag = FloodgateCompat.getGamertag(uuid);
                if (!gamertag.isPresent()) {
                    Attempt<String> lookedUp = attempt(LookupProviders.GEYSER, settings,
                            () -> LookupProviders.geyserGamertag(xuid, settings.timeout, settings.userAgent));
                    answered = answered && lookedUp.answered;
                    gamertag = Optional.ofNullable(lookedUp.value);
                }
                if (gamertag.isPresent()) name = getBedrockPrefix() + gamertag.get();
            }
            if (name != null) {
                ProfileData named = new ProfileData(uuid, name, null, null, remote != null ? remote.getSource() : LookupSource.GEYSER_API);
                remote = remote == null ? named : remote.mergeFrom(named);
            }
        } else if (type == UuidType.ONLINE) {
            Attempt<ProfileData> java = javaByUuid(uuid, settings);
            answered = java.answered;
            remote = java.value;
        } else if (type == UuidType.OFFLINE && local != null && local.getName() != null) {
            // No service knows an offline-mode UUID. Its name may still belong to a real account,
            // whose skin is the one most offline-mode skin setups show.
            Attempt<ProfileData> java = javaByName(local.getName(), settings);
            answered = java.answered;
            if (java.value != null && java.value.hasTextures()) {
                remote = new ProfileData(uuid, local.getName(), java.value.getTextureValue(), java.value.getTextureSignature(), java.value.getSource());
            }
        } else {
            answered = true;
        }

        ProfileData merged = local != null ? local.mergeFrom(remote) : remote;
        if (merged != null && merged.getUuid() == null) merged = new ProfileData(uuid, merged.getName(), merged.getTextureValue(), merged.getTextureSignature(), merged.getSource());

        if (merged != null && (merged.getName() != null || merged.hasTextures())) {
            return LookupResult.found(merged, answered || merged.hasTextures());
        }
        if (local != null) return LookupResult.found(local, answered);
        return answered ? LookupResult.notFound() : LookupResult.unavailable();
    }

    private static LookupResult resolveNameNow(String name, Settings settings) {
        ProfileData local = localByName(name);
        if (local != null) {
            if (local.hasTextures()) return LookupResult.found(local, true);
            LookupResult byUuid = resolveUuidNow(local.getUuid(), settings);
            if (byUuid.isFound()) {
                ProfileData profile = byUuid.getProfile().get();
                return LookupResult.found(local.mergeFrom(profile), byUuid.isComplete());
            }
            return LookupResult.found(local, false);
        }

        if (hasBedrockPrefix(name)) {
            String gamertag = name.substring(getBedrockPrefix().length());
            Attempt<Long> xuid = attempt(LookupProviders.GEYSER, settings,
                    () -> LookupProviders.geyserXuid(gamertag, settings.timeout, settings.userAgent));
            if (xuid.value != null) {
                UUID uuid = UuidType.fromXuid(xuid.value);
                LookupResult byUuid = resolveUuidNow(uuid, settings);
                ProfileData named = new ProfileData(uuid, name, null, null, LookupSource.GEYSER_API);
                return LookupResult.found(byUuid.getProfile().map(p -> named.mergeFrom(p)).orElse(named), byUuid.isComplete());
            }
            // A prefix that Java names cannot contain settles it; otherwise this may be a Java name.
            if (!JAVA_NAME.matcher(name).matches()) {
                return xuid.answered ? LookupResult.notFound() : LookupResult.unavailable();
            }
        }

        if (!usesAccountUuids()) {
            // Offline mode: the server will give this name its offline-mode UUID, whatever any
            // service says. Any account with that name only contributes a skin.
            UUID uuid = UuidType.offlineUuid(name);
            Attempt<ProfileData> java = javaByName(name, settings);
            ProfileData skin = java.value;
            return LookupResult.found(new ProfileData(uuid, name,
                    skin != null ? skin.getTextureValue() : null,
                    skin != null ? skin.getTextureSignature() : null,
                    skin != null && skin.hasTextures() ? skin.getSource() : LookupSource.DERIVED), java.answered);
        }

        Attempt<ProfileData> java = javaByName(name, settings);
        if (java.value != null) return LookupResult.found(java.value, java.answered);
        return java.answered ? LookupResult.notFound() : LookupResult.unavailable();
    }
}
