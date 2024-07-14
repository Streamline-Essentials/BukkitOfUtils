package io.streamlined.bukkit.scheduler;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.streamlined.bukkit.MessageUtils;
import io.streamlined.bukkit.instances.BaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Predicate;

public class FoliaBridge {
    public static void sync(SchedulableTask task) {
        RegionScheduler scheduler = Bukkit.getRegionScheduler();

        switch (task.getType()) {
            case LOCATION:
                if (task.getRunAt() == null) {
                    try {
                        throw new Exception("Location-based task is missing a location.");
                    } catch (Exception e) {
                        MessageUtils.logWarning("An error occurred while executing a scheduled task.", e);
                    }
                    return;
                }

                sync(task.getRunAt(), task.getTask());
                break;
            case ENTITY:
                if (task.getEntity() == null) {
                    try {
                        throw new Exception("Sender-based task is missing a sender.");
                    } catch (Exception e) {
                        MessageUtils.logWarning("An error occurred while executing a scheduled task.", e);
                    }
                    return;
                }

                sync(task.getEntity(), task.getTask());
            case GLOBAL:
                sync(task.getTask());
                break;
        }
    }

    public static void sync(Entity ctx, Runnable task) {
        ctx.getScheduler().execute(BaseManager.getBaseInstance(), task, null, 0);
    }

    public static void sync(CommandSender ctx, Runnable task) {
        try {
            if (ctx instanceof Entity) {
                sync((Entity) ctx, task);
            } else if (ctx instanceof BlockCommandSender) {
                Bukkit.getRegionScheduler().execute(BaseManager.getBaseInstance(),
                        ((BlockCommandSender) ctx).getBlock().getLocation(), task);
            } else if (ctx instanceof ProxiedCommandSender) {
                sync(((ProxiedCommandSender) ctx).getCallee(), task);
            }
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while executing a scheduled task.", e);
        }
    }

    public static void sync(Location location, Runnable task) {
        try {
            Bukkit.getRegionScheduler().execute(BaseManager.getBaseInstance(), location, task);
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while executing a scheduled task.", e);
        }
    }

    public static void sync(Runnable task) {
        try {
            Bukkit.getGlobalRegionScheduler().execute(BaseManager.getBaseInstance(), task);
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while executing a scheduled task.", e);
        }
    }

    public static void alertError(Throwable e) {
        MessageUtils.logWarning("An error occurred while executing a scheduled task.", e);
    }

    public static ConcurrentSkipListMap<String, Entity> pollEntities() {
        ConcurrentSkipListMap<String, Entity> entities = new ConcurrentSkipListMap<>();

        try {
            Bukkit.getWorlds().forEach(world -> {
                RegionScheduler scheduler = Bukkit.getRegionScheduler();

                Arrays.asList(world.getLoadedChunks()).forEach(chunk -> {
                    scheduler.execute(BaseManager.getBaseInstance(), world, chunk.getX(), chunk.getZ(), () -> {
                        Arrays.asList(chunk.getEntities()).forEach(entity -> {
                            entities.put(entity.getUniqueId().toString(), entity);
                        });
                    });
                });
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
