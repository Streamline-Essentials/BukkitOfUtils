package host.plas.bou.utils;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import host.plas.bou.MessageUtils;
import host.plas.bou.scheduling.BaseRunnable;
import host.plas.bou.scheduling.TaskManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityUtils {
    @Getter @Setter
    private static ConcurrentSkipListMap<String, Entity> cachedEntities = new ConcurrentSkipListMap<>();

    @Getter @Setter
    private static EntityLookupTimer lookupTimer;

    public static void init() {
        if (ClassHelper.isFolia()) lookupTimer = new EntityLookupTimer();
    }

    public static void cacheEntityAlreadyInSync(Entity entity) {
        if (! entity.isValid()) return;
        if (cachedEntities.containsKey(entity.getUniqueId().toString())) return;

        cachedEntities.put(entity.getUniqueId().toString(), entity);
    }

    public static void cacheEntity(Entity entity) {
        cacheEntity(entity, true);
    }

    public static void cacheEntity(Entity entity, boolean isInSync) {
        if (isInSync) {
            cacheEntityAlreadyInSync(entity);
        } else {
            TaskManager.getScheduler().runTask(entity, () -> cacheEntityAlreadyInSync(entity));
        }
    }

    public static void tickCache() {
        clearCache();
        collectEntities();
    }

    public static int totalEntities(World world) {
        return world.getEntityCount();
    }

    public static int totalEntities() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += totalEntities(world);
        }
        return total;
    }

    public static void clearCache() {
        cachedEntities.clear();
    }

    public static void collectEntities() {
        try {
            if (ClassHelper.isFolia()) {
                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        if (! chunk.isEntitiesLoaded()) continue;
                        TaskManager.getScheduler().runTask(world, chunk.getX(), chunk.getZ(), () -> {
                            Arrays.stream(chunk.getEntities()).forEach(EntityUtils::cacheEntity);
                        });
                    }
                }
            } else {
                Bukkit.getWorlds().forEach(world -> {
                    world.getEntities().forEach(EntityUtils::cacheEntity);
                });
            }
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling entities.", e);
        }
    }

    public static ConcurrentSkipListMap<String, Entity> getEntitiesBukkit() {
        ConcurrentSkipListMap<String, Entity> entities = new ConcurrentSkipListMap<>();

        try {
            Bukkit.getWorlds().forEach(world -> {
                world.getEntities().forEach(entity -> {
                    entities.put(entity.getUniqueId().toString(), entity);
                });
            });
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling entities.", e);
        }

        return entities;
    }

    public static ConcurrentSkipListMap<String, Entity> getEntitiesHard() {
        if (ClassHelper.isFolia()) {
            return getCachedEntities();
        } else {
            return getEntitiesBukkit();
        }
    }

    public static CompletableFuture<ConcurrentSkipListMap<String, Entity>> getEntitiesAsync() {
        return getCachedEntitiesCache().getIfPresent(0L);
    }

    public static ConcurrentSkipListMap<String, Entity> getEntities() {
        return getEntitiesAsync().join();
    }

    @Getter @Setter
    private static AsyncCache<Long, ConcurrentSkipListMap<String, Entity>> cachedEntitiesCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMillis(100))
                    .buildAsync(loader -> getEntitiesHard());

    @Getter @Setter
    private static AsyncCache<String, ConcurrentSkipListMap<String, Entity>> cachedPredicates =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMillis(100))
                    .buildAsync();

    public static final String LIVING_ENTITIES = "main-living";

    public static void collectEntities(String key, Predicate<Entity> predicate) {
        ConcurrentSkipListMap<String, Entity> toTest = getEntities();

        try {
            toTest.forEach((s, entity) -> {
                TaskManager.getScheduler().runTask(entity, () -> {
                    ConcurrentSkipListMap<String, Entity> entities = new ConcurrentSkipListMap<>();
                    if (predicate.test(entity)) {
                        entities.put(entity.getUniqueId().toString(), entity);
                    }

                    cachedPredicates.put(key, CompletableFuture.completedFuture(entities));
                });
            });
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling entities.", e);
        }
    }

    public static void collectLivingEntities() {
        collectEntities(LIVING_ENTITIES, entity -> entity instanceof LivingEntity);
    }

    public static void collectLivingEntities(String key, Predicate<LivingEntity> predicate) {
        collectEntities(key, entity -> entity instanceof LivingEntity && predicate.test((LivingEntity) entity));
    }

    public static CompletableFuture<ConcurrentSkipListMap<String, Entity>> getEntitiesAsync(String key) {
        return cachedPredicates.get(key, k -> new ConcurrentSkipListMap<>());
    }

    public static ConcurrentSkipListMap<String, Entity> getEntities(String key) {
        return getEntitiesAsync(key).join();
    }

    public static CompletableFuture<ConcurrentSkipListMap<String, Entity>> getLivingEntitiesAsync() {
        return getEntitiesAsync(LIVING_ENTITIES);
    }

    public static ConcurrentSkipListMap<String, Entity> getLivingEntities() {
        return getLivingEntitiesAsync().join();
    }

    public static void collectEntitiesThenDo(Consumer<Entity> consumer) {
        getEntities().forEach((s, entity) -> {
            TaskManager.getScheduler().runTask(entity, () -> consumer.accept(entity));
        });
    }

    public static void collectLivingEntitiesThenDo(Consumer<LivingEntity> consumer) {
        getEntities().forEach((s, entity) -> {
            TaskManager.getScheduler().runTask(entity, () -> {
                if (entity instanceof LivingEntity) {
                    consumer.accept((LivingEntity) entity);
                }
            });
        });
    }

    public static class EntityLookupTimer extends BaseRunnable {
        public EntityLookupTimer() {
            super(0, 1);
        }

        @Override
        public void run() {
            if (ClassHelper.isFolia()) tickCache();
            else pause();
        }
    }
}
