package host.plas.bou.firestring;

import host.plas.bou.BukkitOfUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class FireStringManager {
    @Getter @Setter
    private static ConcurrentSkipListSet<FireStringThing> fireStringThings = new ConcurrentSkipListSet<>();

    public static void register(FireStringThing fireStringThing) {
        unregister(fireStringThing.getIdentifier());

        getFireStringThings().add(fireStringThing);

        BukkitOfUtils.getInstance().logInfo("Registered &cFireStringThing &fwith identifier &d" + fireStringThing.getIdentifier());
    }

    public static void unregister(String identifier) {
        getFireStringThings().removeIf(fireStringThing -> fireStringThing.getIdentifier().equals(identifier));
    }

    public static Optional<FireStringThing> get(String identifier) {
        return getFireStringThings().stream().filter(fireStringThing -> fireStringThing.getIdentifier().equals(identifier)).findFirst();
    }

    public static void fire(String string) {
        fireStringThings.forEach(fireStringThing -> {
            try {
                if (fireStringThing.checkAndFire(string)) {
                    BukkitOfUtils.getInstance().logInfo("Fired string for " + fireStringThing.getIdentifier() + " with string " + string);
                }
            } catch (Exception e) {
                BukkitOfUtils.getInstance().logWarning("Failed to fire string for " + fireStringThing.getIdentifier() + " with string " + string);
                BukkitOfUtils.getInstance().logWarning(e);
            }
        });
    }

    public static void init() {
        Arrays.stream(BuiltIn.values()).forEach(builtIn -> {
            FireStringThing fireString = new FireStringThing(builtIn.getIdentifier(), builtIn.getConsumer(), true);

            // do something with fireString
        });

        BukkitOfUtils.getInstance().logInfo("Initialized &cFireStringManager &fwith &a" + fireStringThings.size() + " &fFireStrings");
    }
}
