package host.plas.bou.firestring;

import host.plas.bou.BukkitOfUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class FireStringManager {
    private static ConcurrentSkipListSet<FireString> fireStrings = new ConcurrentSkipListSet<>();

    public static void register(FireString fireString) {
        unregister(fireString.getIdentifier());
        getFireStrings().add(fireString);
        BukkitOfUtils.getInstance().logInfo("&7> &6(&b" + fireString.getIdentifier() + "&6) &cFireString &fRegistered&7!");
    }

    public static void unregister(String identifier) {
        getFireStrings().removeIf(fireString -> fireString.getIdentifier().equals(identifier));
    }

    public static Optional<FireString> get(String identifier) {
        return getFireStrings().stream().filter(fireString -> fireString.getIdentifier().equals(identifier)).findFirst();
    }

    public static void fire(String string) {
        fireStrings.forEach(fireString -> {
            try {
                if (fireString.checkAndFire(string)) {
                }
            } catch (
            // do nothing
            Exception e) {
                BukkitOfUtils.getInstance().logWarning("Failed to fire string for " + fireString.getIdentifier() + " with string " + string);
                BukkitOfUtils.getInstance().logWarning(e);
            }
        });
    }

    public static void init() {
        Arrays.stream(BuiltIn.values()).forEach(builtIn -> {
            FireString fireString = new FireString(builtIn.getIdentifier(), builtIn.getConsumer(), false);
            register(fireString);
        });
        BukkitOfUtils.getInstance().logInfo("Initialized &cFireStringManager &fwith &a" + fireStrings.size() + " &fFireStrings");
    }

    public static ConcurrentSkipListSet<FireString> getFireStrings() {
        return FireStringManager.fireStrings;
    }

    public static void setFireStrings(final ConcurrentSkipListSet<FireString> fireStrings) {
        FireStringManager.fireStrings = fireStrings;
    }
}
