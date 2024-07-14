package io.streamlined.bukkit.utils;

import io.streamlined.bukkit.scheduler.FoliaBridge;
import io.streamlined.bukkit.scheduler.MainScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Predicate;

public class EntityUtils {
    public static ConcurrentSkipListMap<String, Entity> pollEntities() {
        if (MainScheduler.isFoliaServer()) {
            return FoliaBridge.pollEntities();
        }
        ConcurrentSkipListMap<String, Entity> entities = new ConcurrentSkipListMap<>();

        Bukkit.getWorlds().forEach(world -> {
            world.getEntities().forEach(entity -> {
                entities.put(entity.getUniqueId().toString(), entity);
            });
        });

        return entities;
    }

    public static ConcurrentSkipListMap<String, Entity> pollEntities(Predicate<Entity> predicate) {
        ConcurrentSkipListMap<String, Entity> entities = new ConcurrentSkipListMap<>();

        pollEntities().values().stream().filter(predicate).forEach(entity -> {
            entities.put(entity.getUniqueId().toString(), entity);
        });

        return entities;
    }

    public static ConcurrentSkipListMap<String, LivingEntity> pollLivingEntities() {
        if (MainScheduler.isFoliaServer()) {
            return FoliaBridge.pollLivingEntities();
        }
        ConcurrentSkipListMap<String, LivingEntity> entities = new ConcurrentSkipListMap<>();

        pollEntities().values().stream().filter(entity -> entity instanceof LivingEntity).forEach(entity -> {
            entities.put(entity.getUniqueId().toString(), (LivingEntity) entity);
        });

        return entities;
    }

    public static ConcurrentSkipListMap<String, LivingEntity> pollLivingEntities(Predicate<LivingEntity> predicate) {
        ConcurrentSkipListMap<String, LivingEntity> entities = new ConcurrentSkipListMap<>();

        pollLivingEntities().values().stream().filter(predicate).forEach(entity -> {
            entities.put(entity.getUniqueId().toString(), entity);
        });

        return entities;
    }
}
