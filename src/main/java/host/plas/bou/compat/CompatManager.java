package host.plas.bou.compat;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.luckperms.LPHeld;
import host.plas.bou.compat.papi.PAPIHeld;
import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.handling.IEventable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter @Setter
public class CompatManager {
    public static final String PAPI_IDENTIFIER = "PlaceholderAPI";
    public static final String LP_IDENTIFIER = "LuckPerms";

    @Getter @Setter
    private static ConcurrentSkipListMap<String, HeldHolder> holders = new ConcurrentSkipListMap<>();

    @Getter @Setter
    private static ConcurrentSkipListMap<Integer, CompatibilityManager> managers = new ConcurrentSkipListMap<>();

    public static void init() {
        registerHolder(PAPI_IDENTIFIER, PAPIHeld::new);
        registerHolder(LP_IDENTIFIER, LPHeld::new);
    }

    public static int registerManager(CompatibilityManager manager) {
        int id = 0;
        try {
            id = managers.lastKey() + 1;
        } catch (Throwable e) {
            // ignored
        }
        managers.put(id, manager);

        return id;
    }

    public static void unregisterManager(int id) {
        managers.remove(id);
    }

    public static CompatibilityManager getManager(int id) {
        return managers.get(id);
    }

    public static void putHolderRaw(String identifier, HeldHolder holder) {
        getHolders().put(identifier, holder);
    }

    public static void registerHolder(String identifier, Function<String, HeldHolder> creationFunction) {
        HeldHolder holder = new EmptyHolder(identifier);
        try {
            holder = creationFunction.apply(identifier);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logInfo(identifier + " not found, skipping...");
        }
        holders.put(identifier, holder);
    }

    public static void registerHolder(String identifier, Supplier<HeldHolder> creationFunction) {
        HeldHolder holder = new EmptyHolder(identifier);
        try {
            holder = creationFunction.get();
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logInfo(identifier + " not found, skipping...");
        }
        holders.put(identifier, holder);
    }

    public static void unregisterHolder(String identifier) {
        getHolders().remove(identifier);
    }

    public static HeldHolder getHolder(String identifier) {
        return getHolders().get(identifier);
    }

    public static boolean isEnabled(String identifier) {
        return getHolder(identifier) != null && getHolder(identifier).isEnabled();
    }
}
