package host.plas.bou.firestring;

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
    }

    public static void unregister(String identifier) {
        getFireStringThings().removeIf(fireStringThing -> fireStringThing.getIdentifier().equals(identifier));
    }

    public static Optional<FireStringThing> get(String identifier) {
        return getFireStringThings().stream().filter(fireStringThing -> fireStringThing.getIdentifier().equals(identifier)).findFirst();
    }

    public static void fire(String string) {
        fireStringThings.forEach(fireStringThing -> {
            if (fireStringThing.checkAndFire(string)) {
                return; // do something later...
            }
        });
    }

    public static void init() {
        Arrays.stream(BuiltIn.values()).forEach(builtIn -> register(builtIn.getFireString()));
    }
}
