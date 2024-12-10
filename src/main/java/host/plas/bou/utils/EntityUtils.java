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

public class EntityUtils {
    @Getter @Setter
    private static Cache<String, WeakReference<Entity>> cachedEntities = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(1))
            .build()
            ;

    @Getter @Setter
    private static EntityLookupTimer lookupTimer;

    public static void init() {
        lookupTimer = new EntityLookupTimer();
    }

    public static boolean containsValue(Entity entity) {
        return cachedEntities.asMap().containsValue(entity);
    }

    public static boolean containsKey(String uniqueId) {
        return cachedEntities.asMap().containsKey(uniqueId);
    }

    public static void cacheEntityAlreadyInSync(WeakReference<Entity> entity) {
        if (entity == null || entity.get() == null) return;
        if (! entity.get().isValid()) return;
        if (containsKey(entity.get().getUniqueId().toString())) return;

        cachedEntities.put(entity.get().getUniqueId().toString(), entity);
    }

    public static void cacheEntity(WeakReference<Entity> entity, boolean isInSync) {
        if (isInSync || ! ClassHelper.isFolia()) {
            cacheEntityAlreadyInSync(entity);
        } else {
            TaskManager.getScheduler().runTask(entity.get(), () -> cacheEntityAlreadyInSync(entity));
        }
    }

    public static void cacheEntity(WeakReference<Entity> entity) {
        cacheEntity(entity, false);
    }

    public static void tickCache() {
        clearCache();
        collectEntities();
    }

    public static int totalEntities(World world) {
        return world.getEntities().size();
    }

    public static int totalEntities() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += totalEntities(world);
        }
        return total;
    }

    public static void clearCache() {
        cachedEntities.invalidateAll();
    }

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
                    getEntitiesBukkit().values().forEach(EntityUtils::cacheEntity);
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

    public static ConcurrentSkipListMap<String, WeakReference<Entity>> getEntities() {
        return getEntities(false);
    }

    public static void collectEntitiesThenDo(Consumer<Entity> consumer) {
        TaskManager.runTask(() -> {
            getEntities(true).forEach((s, entity) -> {
                TaskManager.runTask(entity.get(), () -> consumer.accept(entity.get()));
            });
        });
    }

    public static void collectEntitiesThenDoSet(Consumer<Collection<Entity>> consumer) {
        TaskManager.runTask(() -> {
            getEntities(true).forEach((s, entity) -> {
                TaskManager.runTask(entity.get(), () -> {
                    if (entity.get() == null) return;
                    ThingyUtils.withThing(consumer, List.of(entity.get()));
                });
            });
        });
    }

    public static void collectLivingEntitiesThenDo(Consumer<LivingEntity> consumer) {
        TaskManager.runTask(() -> {
            getEntities(true).forEach((s, entity) -> {
                TaskManager.runTask(entity.get(), () -> {
                    if (entity instanceof LivingEntity) {
                        consumer.accept((LivingEntity) entity);
                    }
                });
            });
        });
    }

    public static ConcurrentSkipListSet<String> getOnlinePlayerNames() {
        ConcurrentSkipListSet<String> names = new ConcurrentSkipListSet<>();
        Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));

        return names;
    }

    public static ConcurrentSkipListSet<String> getOnlinePlayerUuids() {
        ConcurrentSkipListSet<String> uuids = new ConcurrentSkipListSet<>();
        Bukkit.getOnlinePlayers().forEach(player -> uuids.add(player.getUniqueId().toString()));

        return uuids;
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



    public static Optional<Player> getLastDamager(Entity entity) {
        try {
            EntityDamageByEntityEvent lastDamageCause = (EntityDamageByEntityEvent) entity.getLastDamageCause();
            if (lastDamageCause == null) return Optional.empty();
            return abstractDamager(lastDamageCause.getDamager());
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

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

    public static OfflinePlayer getDummyOfflinePlayer() {
        return Bukkit.getOfflinePlayer("Drakified");
    }
}
