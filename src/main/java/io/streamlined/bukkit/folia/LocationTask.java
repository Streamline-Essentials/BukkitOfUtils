package io.streamlined.bukkit.folia;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Getter @Setter
public class LocationTask<R> implements Comparable<LocationTask> {
    public static long CURRENT_INDEX = 0;

    private long index;
    private Supplier<R> supplier;
    private Location location;
    private boolean runSync;

    public LocationTask(Supplier<R> supplier, Location location, boolean runSync) {
        this.index = CURRENT_INDEX ++;

        this.supplier = supplier;
        this.location = location;

        this.runSync = runSync;
    }

    public R execute() {
        try {
            return FoliaChecker.execute(this, runSync);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int compareTo(@NotNull LocationTask o) {
        return Long.compare(index, o.index);
    }
}
