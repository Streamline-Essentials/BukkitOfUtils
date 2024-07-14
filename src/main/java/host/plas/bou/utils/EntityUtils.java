package host.plas.bou.utils;

import host.plas.bou.MessageUtils;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.scheduling.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Predicate;

public class EntityUtils {
    public static ConcurrentSkipListMap<String, Entity> pollEntities() {
        ConcurrentSkipListMap<String, Entity> entities = new ConcurrentSkipListMap<>();

        try {
            TaskManager.getScheduler().runTask(() -> {
                Bukkit.getWorlds().forEach(world -> {
                    Arrays.stream(world.getLoadedChunks()).forEach(chunk -> {
                        TaskManager.getScheduler().runTask(world, chunk.getX(), chunk.getZ(), () -> {
                            Arrays.stream(chunk.getEntities()).forEach(entity -> {
                                TaskManager.getScheduler().runTask(entity, () -> {
                                    entities.put(entity.getUniqueId().toString(), entity);
                                });
                            });
                        });
                    });
                });
            });
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling entities.", e);
        }

        return entities;
    }

    public static ConcurrentSkipListMap<String, Entity> pollEntities(Predicate<Entity> predicate) {
        ConcurrentSkipListMap<String, Entity> entities = new ConcurrentSkipListMap<>();

        try {
            pollEntities().values().stream().filter(predicate).forEach(entity -> {
                entities.put(entity.getUniqueId().toString(), entity);
            });
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling entities.", e);
        }

        return entities;
    }

    public static ConcurrentSkipListMap<String, LivingEntity> pollLivingEntities() {
        ConcurrentSkipListMap<String, LivingEntity> entities = new ConcurrentSkipListMap<>();

        try {
            pollEntities().values().stream().filter(entity -> entity instanceof LivingEntity).forEach(entity -> {
                entities.put(entity.getUniqueId().toString(), (LivingEntity) entity);
            });
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling living entities.", e);
        }

        return entities;
    }

    public static ConcurrentSkipListMap<String, LivingEntity> pollLivingEntities(Predicate<LivingEntity> predicate) {
        ConcurrentSkipListMap<String, LivingEntity> entities = new ConcurrentSkipListMap<>();

        try {
            pollLivingEntities().values().stream().filter(predicate).forEach(entity -> {
                entities.put(entity.getUniqueId().toString(), entity);
            });
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling living entities.", e);
        }

        return entities;
    }
}
