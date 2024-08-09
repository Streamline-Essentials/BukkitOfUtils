package host.plas.bou.compat;

import host.plas.bou.BukkitOfUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

@Getter @Setter
public class CompatManager {
    @Getter @Setter
    private static ConcurrentSkipListMap<String, HeldHolder> holders = new ConcurrentSkipListMap<>();

    public void registerHolder(String identifier, Function<String, HeldHolder> creationFunction) {
        HeldHolder holder = new EmptyHolder(identifier);
        try {
            holder = creationFunction.apply(identifier);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logInfo(identifier + " not found, skipping...");
        }
        holders.put(identifier, holder);
    }

    public void unregisterHolder(String identifier) {
        holders.remove(identifier);
    }

    public static HeldHolder getHolder(String identifier) {
        return holders.get(identifier);
    }

    public static boolean isEnabled(String identifier) {
        return getHolder(identifier) != null && getHolder(identifier).isEnabled();
    }
}
