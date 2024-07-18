package host.plas.bou.utils;

public class ClassHelper {
    public static boolean hasClass(String fullClass) {
        try {
            Class.forName(fullClass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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
