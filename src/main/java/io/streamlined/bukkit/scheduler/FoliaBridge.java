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
}
