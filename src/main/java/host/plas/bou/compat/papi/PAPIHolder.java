package host.plas.bou.compat.papi;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.ApiHolder;
import host.plas.bou.compat.CompatManager;
import host.plas.bou.compat.papi.expansion.BetterExpansion;
import host.plas.bou.compat.papi.expansion.own.BouExpansion;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class PAPIHolder extends ApiHolder<PlaceholderAPIPlugin> {
    @Getter @Setter
    private static ConcurrentSkipListSet<BetterExpansion> loadedExpansions = new ConcurrentSkipListSet<>();

    @Getter @Setter
    private static BouExpansion ownExpansion;

    public static void loadExpansion(BetterExpansion expansion) {
        unloadExpansion(expansion);

        loadedExpansions.add(expansion);
    }

    public static void unloadExpansion(String identifier) {
        loadedExpansions.removeIf(expansion -> expansion.getIdentifier().equalsIgnoreCase(identifier));
    }

    public static void unloadExpansion(BetterExpansion expansion) {
        unloadExpansion(expansion.getIdentifier());
    }

    public static Optional<BetterExpansion> getExpansion(String identifier) {
        return loadedExpansions.stream().filter(expansion -> expansion.getIdentifier().equalsIgnoreCase(identifier)).findFirst();
    }

    public static boolean isExpansionLoaded(String identifier) {
        return getExpansion(identifier).isPresent();
    }

    public PAPIHolder() {
        super(CompatManager.PAPI_IDENTIFIER, (v) -> PlaceholderAPIPlugin.getInstance());

        try {
            init();
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Failed to initialize PlaceholderAPI holder.");
        }
    }

    public void register(PlaceholderExpansion expansion) {
        expansion.register();
    }

    public void register(BetterExpansion expansion) {
        expansion.register();
    }

    public void unregister(PlaceholderExpansion expansion) {
        expansion.unregister();
    }

    public void unregister(BetterExpansion expansion) {
        expansion.unregister();
    }

    public void unregisterAll() {
        loadedExpansions.forEach(this::unregister);
    }

    public void registerAll() {
        loadedExpansions.forEach(this::register);
    }

    public void init() {
        if (isEnabled()) {
            ownExpansion = new BouExpansion();
        }
    }
}
