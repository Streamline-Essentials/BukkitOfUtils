package io.streamlined.bukkit.instances;

import io.streamlined.bukkit.PluginBase;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class BaseManager {
    @Getter @Setter
    private static PluginBase baseInstance;

    @Getter @Setter
    private static ConcurrentSkipListSet<BaseRunnable> loadedRunnables = new ConcurrentSkipListSet<>();
    @Setter
    private static int nextRunnableIndex = 0;
    @Getter @Setter
    private static Timer ticker;

    public static void init(PluginBase baseInstance) {
        setBaseInstance(baseInstance);
        new InventoryAPI(baseInstance).init();

        ticker = new Timer();

        ticker.scheduleAtFixedRate(BaseManager.getMainTimerTask(), 0L, 50L);
    }

    public static World getMainWorld() {
        World world = Bukkit.getWorlds().get(0);
        if (world == null) {
            throw new NullPointerException("Main world is null.");
        } else {
            return world;
        }
    }

    public static CommandSender getConsole() {
        return getBaseInstance().getServer().getConsoleSender();
    }

    public static List<Player> getOnlinePlayers() {
        return new ArrayList<>(getBaseInstance().getServer().getOnlinePlayers());
    }

    public static ConcurrentSkipListMap<String, Player> getOnlinePlayersByUUID() {
        ConcurrentSkipListMap<String, Player> players = new ConcurrentSkipListMap<>();
        for (Player player : getOnlinePlayers()) {
            players.put(player.getUniqueId().toString(), player);
        }

        return players;
    }

    public static ConcurrentSkipListMap<String, Player> getOnlinePlayersByName() {
        ConcurrentSkipListMap<String, Player> players = new ConcurrentSkipListMap<>();
        for (Player player : getOnlinePlayers()) {
            players.put(player.getName(), player);
        }

        return players;
    }

    public static Player getPlayerByUUID(String uuid) {
        return getOnlinePlayersByUUID().get(uuid);
    }

    public static Player getPlayerByName(String name) {
        return getOnlinePlayersByName().get(name);
    }

    public static <T extends BaseRunnable> void loadRunnable(T runnable) {
        loadedRunnables.add(runnable);
    }

    public static <T extends BaseRunnable> void unloadRunnable(T runnable) {
        try {
            loadedRunnables.remove(runnable);
        } catch (Exception e) {
            T r = getRunnable(runnable.getIndex());
            if (r == null) r = getRunnable(runnable.getStartTime());

            if (r != null) loadedRunnables.remove(r);
        }
    }

    public static <T extends BaseRunnable> T getRunnable(Date startDate) {
        AtomicReference<T> runnable = new AtomicReference<>();

        loadedRunnables.forEach(r -> {
            if (r.getStartTime().equals(startDate)) {
                runnable.set((T) r);
            }
        });

        return runnable.get();
    }

    public static <T extends BaseRunnable> T getRunnable(int index) {
        AtomicReference<T> runnable = new AtomicReference<>();

        loadedRunnables.forEach(r -> {
            if (r.getIndex() == index) {
                runnable.set((T) r);
            }
        });

        return runnable.get();
    }

    public static int getNextRunnableIndex() {
        int index = nextRunnableIndex;

        nextRunnableIndex ++;

        return index;
    }

    public static void tickAllRunnablesAsync() {
        getLoadedRunnables().forEach(BaseRunnable::runAsync);
    }

    public static void tickAllRunnablesSync() {
        getLoadedRunnables().forEach(BaseRunnable::run);
    }

    public static void tickAllRunnablesOnlySync() {
        for (BaseRunnable runnable : getLoadedRunnables()) {
            if (runnable.isAsyncable()) continue;

            runnable.runOnlySync();
        }
    }

    public static void tickAllRunnables() {
        tickAllRunnablesAsync();
        tickAllRunnablesOnlySync();
    }

    public static TimerTask getMainTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                tickAllRunnables();
            }
        };
    }

    public void schedule(Runnable runnable, int delay, int period, boolean isAsyncable) {
        new RBaseRunnable(runnable, delay, period, isAsyncable);
    }

    public void schedule(Runnable runnable, int delay, int period) {
        new RBaseRunnable(runnable, delay, period, true);
    }

    public void schedule(Runnable runnable, int delay, boolean isAsyncable) {
        new RDelayedRunnable(runnable, delay, isAsyncable);
    }

    public void schedule(Runnable runnable, int delay) {
        new RDelayedRunnable(runnable, delay, true);
    }
}
