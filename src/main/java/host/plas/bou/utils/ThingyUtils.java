package host.plas.bou.utils;

import java.util.function.Consumer;

public class ThingyUtils {
    public static <C> void withThing(Consumer<C> consumer, C thing) {
        consumer.accept(thing);
    }
}
