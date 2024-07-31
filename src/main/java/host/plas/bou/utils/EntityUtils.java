package host.plas.bou.utils;

import host.plas.bou.MessageUtils;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.scheduling.BaseRunnable;
import host.plas.bou.scheduling.TaskManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

public class EntityUtils {
    @Getter @Setter
    private static ConcurrentSkipListMap<String, Entity> cachedEntities = new ConcurrentSkipListMap<>();

    @Getter @Setter
    private static EntityLookupTimer lookupTimer;

    public static void init() {
        lookupTimer = new EntityLookupTimer();
    }

    public static void cacheEntityAlreadyInSync(Entity entity) {
        if (! entity.isValid()) return;
        if (cachedEntities.containsKey(entity.getUniqueId().toString())) return;

        cachedEntities.put(entity.getUniqueId().toString(), entity);
    }

    public static void cacheEntity(Entity entity, boolean isInSync) {
        if (isInSync || ! ClassHelper.isFolia()) {
            cacheEntityAlreadyInSync(entity);
        } else {
            TaskManager.getScheduler().runTask(entity, () -> cacheEntityAlreadyInSync(entity));
        }
    }

    public static void cacheEntity(Entity entity) {
        cacheEntity(entity, false);
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

                        TaskManager.runTask(chunk, () -> {
                            Arrays.stream(chunk.getEntities()).forEach(EntityUtils::cacheEntity);
                        });
                    }
                }
            } else {
                TaskManager.runTask(() -> {
                    getEntitiesBukkit().values().forEach(EntityUtils::cacheEntity);
                });
            }
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling entities.", e);
        }
    }

    /**
     * Gets a map of entities from the server.
     * Must be called from the main thread.
     * @return A map of entities.
     */
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

    public static ConcurrentSkipListMap<String, Entity> getEntities(boolean isInSync) {
        if (ClassHelper.isFolia()) {
            return getCachedEntities();
        } else {
            if (isInSync) {
                return getEntitiesBukkit();
            } else {
                return getCachedEntities();
            }
        }
    }

    public static ConcurrentSkipListMap<String, Entity> getEntities() {
        return getEntities(false);
    }

    public static void collectEntitiesThenDo(Consumer<Entity> consumer) {
        TaskManager.runTask(() -> {
            getEntities(true).forEach((s, entity) -> {
                TaskManager.runTask(entity, () -> consumer.accept(entity));
            });
        });
    }

    public static void collectEntitiesThenDoSet(Consumer<Collection<Entity>> consumer) {
        TaskManager.runTask(() -> consumer.accept(getEntities(true).values()));
    }

    public static void collectLivingEntitiesThenDo(Consumer<LivingEntity> consumer) {
        TaskManager.runTask(() -> {
            getEntities(true).forEach((s, entity) -> {
                TaskManager.runTask(entity, () -> {
                    if (entity instanceof LivingEntity) {
                        consumer.accept((LivingEntity) entity);
                    }
                });
            });
        });
    }

    public static class EntityLookupTimer extends BaseRunnable {
        public EntityLookupTimer() {
            super(0, BaseManager.getBaseConfig().getEntityCollectionFrequency());
        }

        @Override
        public void run() {
            if (isCancelled()) return;

            tickCache();
            if (getPeriod() != BaseManager.getBaseConfig().getEntityCollectionFrequency()) setPeriod(BaseManager.getBaseConfig().getEntityCollectionFrequency());
        }
    }
}
