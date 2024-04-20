package io.streamlined.bukkit.folia;

import io.streamlined.bukkit.instances.BaseManager;
import io.streamlined.bukkit.instances.FoliaManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Getter @Setter
public class LocationalTask<T> implements Comparable<LocationalTask<T>> {
    public static long CURRENT_INDEX = 0;

    private long index;
    private Runnable runnable;
    private Function<T, Location> locationGetter;
    private T object;
    private boolean runSync;

    public LocationalTask(Runnable runnable, Function<T, Location> locationGetter, T object, boolean runSync) {
        this.index = CURRENT_INDEX ++;

        this.runnable = runnable;
        this.locationGetter = locationGetter;
        this.object = object;

        this.runSync = runSync;
    }

    public void execute() {
        try {
            FoliaChecker.validate(this::executeWhenFolia, () -> {
                if (runSync) {
                    Bukkit.getScheduler().runTask(BaseManager.getBaseInstance(), runnable);
                } else {
                    runnable.run();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeWhenFolia() {
        FoliaManager.runTaskSync(locationGetter.apply(object), runnable);
    }

    @Override
    public int compareTo(@NotNull LocationalTask<T> o) {
        return Long.compare(index, o.index);
    }
}
