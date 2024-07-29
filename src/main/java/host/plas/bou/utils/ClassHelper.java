package host.plas.bou.utils;

import lombok.Getter;

import java.util.concurrent.ConcurrentSkipListMap;

public class ClassHelper {
    public static final ConcurrentSkipListMap<String, Boolean> PROPERTY_CACHE = new ConcurrentSkipListMap<>();

    public static boolean hasClassNoCache(String fullClass) {
        try {
            Class.forName(fullClass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasClass(String fullClass) {
        Boolean cached = PROPERTY_CACHE.get(fullClass);
        if (cached != null) return cached;

        boolean hasClass = hasClassNoCache(fullClass);
        PROPERTY_CACHE.put(fullClass, hasClass);
        return hasClass;
    }

    public static boolean isFolia() {
        return hasClass("io.papermc.paper.threadedregions.RegionizedServer");
    }

    public static boolean isPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig");
    }

    public static boolean isExpandedSchedulingAvailable() {
        return hasClass("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
    }
}
