package io.streamlined.bukkit.folia;

import io.streamlined.bukkit.instances.FoliaManager;

import java.util.function.Supplier;

public class FoliaChecker {
    public static boolean isFolia() {
        try {
            return FoliaManager.isFolia();
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
    }

    public static <R> R validate(Supplier<R> whenTrue, Supplier<R> whenFalse) {
        if (isFolia()) {
            return whenTrue.get();
        } else {
            return whenFalse.get();
        }
    }
}
