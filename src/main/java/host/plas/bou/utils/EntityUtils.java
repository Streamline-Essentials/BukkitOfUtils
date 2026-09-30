package host.plas.bou.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.scheduling.BaseRunnable;
import host.plas.bou.scheduling.TaskManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for entity management including caching, lookup, and damage tracking.
 * Supports both standard Bukkit and Folia server environments.
 */
public class EntityUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private EntityUtils() {
        // utility class
    }

    /**
     * The cache of entities indexed by their UUID string, with a 1-second expiration.
     *
     * @param cachedEntities the entity cache to set
     */
    @Setter
    private static Cache<String, WeakReference<Entity>> cachedEntities = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(1))
            .build()
            ;

    /**
     * How long the lookup timer keeps refreshing the cache after the cache was last read.
     * Once this passes with no reads, the timer cancels itself, so an idle server does no entity scanning.
     */
    public static final long CACHE_IDLE_MILLIS = 30_000L;

    /**
     * The last time (epoch millis) the entity cache was read.
     */
    private static volatile long lastCacheRead = 0L;

    /**
     * Whether the lookup timer may be started. False before {@link #init()} and after {@link #stop()},
     * so a late read during shutdown cannot restart the timer.
     */
    private static volatile boolean enabled = false;

    /**
     * Guards starting and stopping {@link #lookupTimer}.
     */
    private static final Object TIMER_LOCK = new Object();

    /**
     * The periodic timer responsible for refreshing the entity cache.
     * It is null while nothing is reading the cache; it is started by the first read
     * and cancels itself after {@link #CACHE_IDLE_MILLIS} without reads.
     *
     * @return the entity lookup timer, or null if it is not running
     */
    @Getter
    private static volatile EntityLookupTimer lookupTimer;

    /**
     * Allows the entity lookup timer to be started. The timer itself is started lazily,
     * the first time something reads the entity cache.
     */
    public static void init() {
        enabled = true;
    }

    /**
     * Stops the entity lookup timer and prevents it from being started again.
     */
    public static void stop() {
        synchronized (TIMER_LOCK) {
            enabled = false;
            if (lookupTimer != null) {
                lookupTimer.cancel();
                lookupTimer = null;
            }
        }
        clearCache();
    }

    /**
     * Gets the cache of entities indexed by their UUID string. Reading the cache marks it as in use
     * and starts the lookup timer if it is not running, so the cache is only kept filled while
     * something actually reads it.
     *
     * <p>A read that starts the timer sees whatever the cache holds at that moment (usually nothing);
     * the first refresh is scheduled immediately and lands shortly after.</p>
     *
     * @return the entity cache
     */
    public static Cache<String, WeakReference<Entity>> getCachedEntities() {
        lastCacheRead = System.currentTimeMillis();
        ensureLookupTimer();
        return cachedEntities;
    }

    /**
     * Whether the entity cache has been read within {@link #CACHE_IDLE_MILLIS}.
     *
     * @return true if something is using the cache
     */
    public static boolean isCacheInUse() {
        return System.currentTimeMillis() - lastCacheRead <= CACHE_IDLE_MILLIS;
    }

    /**
     * Starts the lookup timer if it is enabled and not already running.
     */
    private static void ensureLookupTimer() {
        EntityLookupTimer timer = lookupTimer;
        if (timer != null && ! timer.isCancelled()) return;

        synchronized (TIMER_LOCK) {
            if (! enabled) return;
            if (lookupTimer != null && ! lookupTimer.isCancelled()) return;

            lookupTimer = new EntityLookupTimer();
        }
    }

    /**
     * Cancels the given lookup timer because the cache went idle, and drops the cached entities.
     *
     * @param timer the timer that detected the idle cache
     */
    private static void stopIdleLookupTimer(EntityLookupTimer timer) {
        synchronized (TIMER_LOCK) {
            // A read may have landed between the idle check and acquiring the lock.
            if (isCacheInUse()) return;

            timer.cancel();
            if (lookupTimer == timer) lookupTimer = null;
        }
        clearCache();
    }

    /**
     * Checks whether the entity cache contains the specified entity as a value.
     *
     * @param entity the entity to check for
     * @return true if the entity is in the cache
     */
    public static boolean containsValue(Entity entity) {
        if (entity == null) return false;
        return getCachedEntities().asMap().values().stream().anyMatch(ref -> ref.get() == entity);
    }

    /**
     * Checks whether the entity cache contains an entry with the specified UUID string.
     *
     * @param uniqueId the UUID string to check for
     * @return true if an entry with the given UUID exists in the cache
     */
    public static boolean containsKey(String uniqueId) {
        return getCachedEntities().asMap().containsKey(uniqueId);
    }

    /**
     * Caches an entity directly, assuming the call is already synchronized with the entity's thread.
     * Skips null, invalid, or already-cached entities.
     *
     * @param entity a weak reference to the entity to cache
     */
    public static void cacheEntityAlreadyInSync(WeakReference<Entity> entity) {
        Entity e = entity == null ? null : entity.get();
        if (e == null) return;
        if (! e.isValid()) return;

        cachedEntities.asMap().putIfAbsent(e.getUniqueId().toString(), entity);
    }

    /**
     * Caches an entity, optionally dispatching to the entity's thread on Folia.
     *
     * @param entity   a weak reference to the entity to cache
     * @param isInSync whether the current call is already on the entity's owning thread
     */
    public static void cacheEntity(WeakReference<Entity> entity, boolean isInSync) {
        if (isInSync || ! ClassHelper.isFolia()) {
            cacheEntityAlreadyInSync(entity);
        } else {
            TaskManager.getScheduler().runTask(entity.get(), () -> cacheEntityAlreadyInSync(entity));
        }
    }

    /**
     * Caches an entity, dispatching to the entity's thread on Folia if needed.
     *
     * @param entity a weak reference to the entity to cache
     */
    public static void cacheEntity(WeakReference<Entity> entity) {
        cacheEntity(entity, false);
    }

    /**
     * Clears the entity cache and re-collects all entities from loaded worlds.
     */
    public static void tickCache() {
        // On non-Folia the cache is swapped in one main-thread task (see collectEntities),
        // so async readers never observe an empty cache between clear and refill.
        if (ClassHelper.isFolia()) clearCache();
        collectEntities();
    }

    /**
     * Returns the total number of entities in the specified world.
     *
     * @param world the world to count entities in
     * @return the total entity count
     */
    public static int totalEntities(World world) {
        return world.getEntities().size();
    }

    /**
     * Returns the total number of entities across all worlds.
     *
     * @return the total entity count across all worlds
     */
    public static int totalEntities() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += totalEntities(world);
        }
        return total;
    }

    /**
     * Invalidates all entries in the entity cache.
     */
    public static void clearCache() {
        cachedEntities.invalidateAll();
    }

    /**
     * Collects all entities from all loaded worlds and chunks and caches them.
     * Uses chunk-based collection on Folia and world-based collection on standard Bukkit.
     */
    public static void collectEntities() {
        try {
            if (ClassHelper.isFolia()) {
                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
//                        if (! chunk.isEntitiesLoaded()) continue;

                        TaskManager.runTask(chunk, () -> {
                            Arrays.stream(chunk.getEntities()).forEach(entity -> {
                                cacheEntity(new WeakReference<>(entity));
                            });
                        });
                    }
                }
            } else {
                TaskManager.runTask(() -> {
                    // Entities returned by World#getEntities are valid, so they go straight into
                    // a plain map without the isValid/containsKey checks of cacheEntity.
                    Map<String, WeakReference<Entity>> fresh = new HashMap<>();
                    for (World world : Bukkit.getWorlds()) {
                        for (Entity entity : world.getEntities()) {
                            fresh.put(entity.getUniqueId().toString(), new WeakReference<>(entity));
                        }
                    }
                    cachedEntities.invalidateAll();
                    cachedEntities.putAll(fresh);
                });
            }
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("An error occurred while polling entities.", e);
        }
    }

    /**
     * Gets a map of entities from the server.
     * Must be called from the main thread.
     * @return A map of entities.
     */
    public static ConcurrentSkipListMap<String, WeakReference<Entity>> getEntitiesBukkit() {
        ConcurrentSkipListMap<String, WeakReference<Entity>> entities = new ConcurrentSkipListMap<>();

        try {
            Bukkit.getWorlds().forEach(world -> {
                world.getEntities().forEach(entity -> {
                    entities.put(entity.getUniqueId().toString(), new WeakReference<>(entity));
                });
            });
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("An error occurred while polling entities.", e);
        }

        return entities;
    }

    /**
     * Retrieves a map of all known entities, using the cache on Folia or when not in sync,
     * or fetching directly from Bukkit when in sync on a standard server.
     *
     * @param isInSync whether the current call is on the main server thread
     * @return a map of entity UUID strings to weak entity references
     */
    public static ConcurrentSkipListMap<String, WeakReference<Entity>> getEntities(boolean isInSync) {
        ConcurrentSkipListMap<String, WeakReference<Entity>> entities = new ConcurrentSkipListMap<>();
        if (ClassHelper.isFolia()) {
            entities.putAll(getCachedEntities().asMap());
        } else {
            if (isInSync) {
                entities.putAll(getEntitiesBukkit());
            } else {
                entities.putAll(getCachedEntities().asMap());
            }
        }

        return entities;
    }

    /**
     * Retrieves a map of all known entities from the cache.
     *
     * @return a map of entity UUID strings to weak entity references
     */
    public static ConcurrentSkipListMap<String, WeakReference<Entity>> getEntities() {
        return getEntities(false);
    }

    /**
     * Collects all entities synchronously and then applies a consumer to each entity on its owning thread.
     *
     * @param consumer the consumer to apply to each entity
     */
    public static void collectEntitiesThenDo(Consumer<Entity> consumer) {
        TaskManager.runTask(() -> {
            getEntities(true).forEach((s, entity) -> {
                Entity e = entity.get();
                if (e == null) return;
                TaskManager.runTask(e, () -> consumer.accept(e));
            });
        });
    }

    /**
     * Collects all entities synchronously and then applies a consumer to the entire collection.
     *
     * @param consumer the consumer to apply to the collection of entities
     */
    public static void collectEntitiesThenDoSet(Consumer<Collection<Entity>> consumer) {
        TaskManager.runTask(() -> {
            consumer.accept(getEntities(true).values().stream()
                    .map(WeakReference::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        });
    }

    /**
     * Collects all living entities synchronously and then applies a consumer to each on its owning thread.
     *
     * @param consumer the consumer to apply to each living entity
     */
    public static void collectLivingEntitiesThenDo(Consumer<LivingEntity> consumer) {
        TaskManager.runTask(() -> {
            getEntities(true).forEach((s, entity) -> {
                Entity e = entity.get();
                if (! (e instanceof LivingEntity)) return;
                TaskManager.runTask(e, () -> consumer.accept((LivingEntity) e));
            });
        });
    }

    /**
     * Collects all entities in a specific world synchronously and applies a consumer to the collection.
     *
     * @param worldName the name of the world to filter entities by
     * @param consumer  the consumer to apply to the filtered collection of entities
     */
    public static void collectEntitiesInWorldThenDoSet(String worldName, Consumer<Collection<Entity>> consumer) {
        TaskManager.runTask(() -> {
            consumer.accept(getEntities(true).values().stream()
                    .map(WeakReference::get)
                    .filter(Objects::nonNull)
                    .filter(entity -> entity.getWorld().getName().equalsIgnoreCase(worldName))
                    .collect(Collectors.toList()));
        });
    }

    /**
     * Returns a set of all online player names.
     *
     * @return a sorted set of online player names
     */
    public static ConcurrentSkipListSet<String> getOnlinePlayerNames() {
        ConcurrentSkipListSet<String> names = new ConcurrentSkipListSet<>();
        Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));

        return names;
    }

    /**
     * Returns a set of all online player UUIDs as strings.
     *
     * @return a sorted set of online player UUID strings
     */
    public static ConcurrentSkipListSet<String> getOnlinePlayerUuids() {
        ConcurrentSkipListSet<String> uuids = new ConcurrentSkipListSet<>();
        Bukkit.getOnlinePlayers().forEach(player -> uuids.add(player.getUniqueId().toString()));

        return uuids;
    }

    /**
     * Returns a stream of all offline players known to the server.
     *
     * @return a stream of OfflinePlayer instances
     */
    public static Stream<OfflinePlayer> getOfflinePlayersStream() {
        return Arrays.stream(Bukkit.getOfflinePlayers());
    }

    /**
     * Returns a set of all offline player names.
     *
     * @return a sorted set of offline player names
     */
    public static ConcurrentSkipListSet<String> getOfflinePlayerNames() {
        ConcurrentSkipListSet<String> names = new ConcurrentSkipListSet<>();
        getOfflinePlayersStream().forEach(player -> names.add(player.getName()));

        return names;
    }

    /**
     * Returns a set of all offline player UUIDs as strings.
     *
     * @return a sorted set of offline player UUID strings
     */
    public static ConcurrentSkipListSet<String> getOfflinePlayerUuids() {
        ConcurrentSkipListSet<String> uuids = new ConcurrentSkipListSet<>();
        getOfflinePlayersStream().forEach(player -> uuids.add(player.getUniqueId().toString()));

        return uuids;
    }

    /**
     * A periodic timer that refreshes the entity cache at a configurable frequency while the cache
     * is being read, and cancels itself once the cache has gone unread for {@link #CACHE_IDLE_MILLIS}.
     */
    public static class EntityLookupTimer extends BaseRunnable {
        /**
         * Constructs a new EntityLookupTimer with the configured collection frequency.
         */
        public EntityLookupTimer() {
            super(0, BaseManager.getBaseConfig().getEntityCollectionFrequency());
        }

        @Override
        public void run() {
            try {
                if (isCancelled()) return;

                if (! isCacheInUse()) {
                    stopIdleLookupTimer(this);
                    return;
                }

                tickCache();
                if (getPeriod() != BaseManager.getBaseConfig().getEntityCollectionFrequency()) setPeriod(BaseManager.getBaseConfig().getEntityCollectionFrequency());
            } catch (Exception e) {
                BukkitOfUtils.getInstance().logWarning("An error occurred while ticking the entity cache.", e);
            }
        }
    }



    /**
     * Attempts to find the last player who damaged the given entity.
     *
     * @param entity the entity to check the last damage cause for
     * @return an Optional containing the attacking player, or empty if not found
     */
    public static Optional<Player> getLastDamager(Entity entity) {
        try {
            EntityDamageByEntityEvent lastDamageCause = (EntityDamageByEntityEvent) entity.getLastDamageCause();
            if (lastDamageCause == null) return Optional.empty();
            return abstractDamager(lastDamageCause.getDamager());
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    /**
     * Resolves the actual player from a damaging entity, handling projectiles shot by players.
     *
     * @param damager the entity that dealt damage
     * @return an Optional containing the player who dealt the damage, or empty if not a player
     */
    public static Optional<Player> abstractDamager(Entity damager) {
        Player attacker = null;
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        } else {
            if (! (damager instanceof Player)) return Optional.empty();
            attacker = (Player) damager;
        }

        return Optional.ofNullable(attacker);
    }

    /**
     * Returns a dummy offline player instance for testing or placeholder purposes.
     *
     * @return an OfflinePlayer for the name "Drakified"
     */
    public static OfflinePlayer getDummyOfflinePlayer() {
        return Bukkit.getOfflinePlayer("Drakified");
    }
}
