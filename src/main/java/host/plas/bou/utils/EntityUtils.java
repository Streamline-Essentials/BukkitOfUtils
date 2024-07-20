package host.plas.bou.utils;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
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

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Predicate;

public class EntityUtils {
    @Getter @Setter
    private static ConcurrentSkipListMap<String, Entity> cachedEntities = new ConcurrentSkipListMap<>();

    public static void init() {
        new EntityLookupTimer();
    }

    public static void cacheEntity(Entity entity) {
        TaskManager.getScheduler().runTask(entity, () -> {
            cachedEntities.put(entity.getUniqueId().toString(), entity);
        });
    }

    public static void tickCache() {
        getCachedEntities().forEach((s, entity) -> {
            TaskManager.getScheduler().runTask(entity, () -> {
                if (entity.isDead()) {
                    cachedEntities.remove(entity.getUniqueId().toString());
                } else if (! entity.isValid()) {
                    cachedEntities.remove(entity.getUniqueId().toString());
                }
            });
        });

        pollEntities();
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

    public static void pollEntities() {
        try {
            if (ClassHelper.isFolia()) {
                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        if (!chunk.isEntitiesLoaded()) continue;
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

    public static ConcurrentSkipListMap<String, Entity> getEntities(Predicate<Entity> predicate) {
        ConcurrentSkipListMap<String, Entity> entities = new ConcurrentSkipListMap<>();

        try {
            if (! ClassHelper.isFolia()) {
                getEntitiesBukkit().forEach((s, entity) -> {
                    if (predicate.test(entity)) {
                        entities.put(entity.getUniqueId().toString(), entity);
                    }
                });

                return entities;
            }

            return CompletableFuture.supplyAsync(() -> {
                MyScheduledTask task = TaskManager.getScheduler().runTask(() -> {
                    getCachedEntities().forEach((s, entity) -> {
                        CompletableFuture.supplyAsync(() -> {
                            MyScheduledTask task2 = TaskManager.getScheduler().runTask(entity, () -> {
                                if (predicate.test(entity)) {
                                    entities.put(entity.getUniqueId().toString(), entity);
                                }
                            });

                            while (task2.isCurrentlyRunning()) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            return null;
                        }).join();
                    });
                });

                while (task.isCurrentlyRunning()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return entities;
            }).join();
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling entities.", e);
        }

        return entities;
    }

    public static ConcurrentSkipListMap<String, LivingEntity> getLivingEntities() {
        ConcurrentSkipListMap<String, LivingEntity> entities = new ConcurrentSkipListMap<>();

        try {
            if (! ClassHelper.isFolia()) {
                getEntitiesBukkit().forEach((s, entity) -> {
                    if (entity instanceof LivingEntity) {
                        entities.put(entity.getUniqueId().toString(), (LivingEntity) entity);
                    }
                });

                return entities;
            }

            return CompletableFuture.supplyAsync(() -> {
                MyScheduledTask task = TaskManager.getScheduler().runTask(() -> {
                    getCachedEntities().forEach((s, entity) -> {
                        CompletableFuture.supplyAsync(() -> {
                            MyScheduledTask task2 = TaskManager.getScheduler().runTask(entity, () -> {
                                if (entity instanceof LivingEntity) {
                                    entities.put(entity.getUniqueId().toString(), (LivingEntity) entity);
                                }
                            });

                            while (task2.isCurrentlyRunning()) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            return null;
                        }).join();
                    });
                });

                while (task.isCurrentlyRunning()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return entities;
            }).join();
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling living entities.", e);
        }

        return entities;
    }

    public static ConcurrentSkipListMap<String, LivingEntity> getLivingEntities(Predicate<LivingEntity> predicate) {
        ConcurrentSkipListMap<String, LivingEntity> entities = new ConcurrentSkipListMap<>();

        try {
            if (! ClassHelper.isFolia()) {
                getLivingEntities().forEach((s, entity) -> {
                    if (predicate.test(entity)) {
                        entities.put(entity.getUniqueId().toString(), entity);
                    }
                });

                return entities;
            }

            return CompletableFuture.supplyAsync(() -> {
                MyScheduledTask task = TaskManager.getScheduler().runTask(() -> {
                    getLivingEntities().forEach((s, entity) -> {
                        CompletableFuture.supplyAsync(() -> {
                            MyScheduledTask task2 = TaskManager.getScheduler().runTask(entity, () -> {
                                if (predicate.test(entity)) {
                                    entities.put(entity.getUniqueId().toString(), entity);
                                }
                            });

                            while (task2.isCurrentlyRunning()) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            return null;
                        }).join();
                    });
                });

                while (task.isCurrentlyRunning()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return entities;
            }).join();
        } catch (Exception e) {
            MessageUtils.logWarning("An error occurred while polling living entities.", e);
        }

        return entities;
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
