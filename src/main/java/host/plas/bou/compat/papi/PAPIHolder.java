package host.plas.bou.compat.papi;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.ApiHolder;
import host.plas.bou.compat.CompatManager;
import host.plas.bou.compat.papi.expansion.BetterExpansion;
import host.plas.bou.compat.papi.expansion.own.BouExpansion;
import host.plas.bou.utils.EntityUtils;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class PAPIHolder extends ApiHolder<PlaceholderAPIPlugin> {
    @Getter @Setter
    private static ConcurrentSkipListSet<BetterExpansion> loadedExpansions = new ConcurrentSkipListSet<>();

    @Getter @Setter
    private static BouExpansion ownExpansion;

    @Getter @Setter
    private int enableOverride;

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

        enableOverride = -1; // not set
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

    public ConcurrentSkipListSet<BetterExpansion> getOfPlugin(BetterPlugin plugin) {
        ConcurrentSkipListSet<BetterExpansion> expansions = new ConcurrentSkipListSet<>();

        getLoadedExpansions().forEach(expansion -> {
            if (expansion.getBetterPlugin().getIdentifier().equals(plugin.getIdentifier())) {
                expansions.add(expansion);
            }
        });

        return expansions;
    }

    public void flush(BetterPlugin plugin) {
        getOfPlugin(plugin).forEach(expansion -> {
            try {
                unregister(expansion);
                if (! expansion.unregister()) {
//                    BukkitOfUtils.getInstance().logWarning("Failed to unregister expansion " + expansion.getIdentifier() + " from PlaceholderAPI.");
                }
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logWarning("Failed to unregister expansion " + expansion.getIdentifier() + " from PlaceholderAPI.", e);
            }
        });
    }

    public void flushAll() {
        getLoadedExpansions().forEach(expansion -> {
            try {
                unregister(expansion);
                if (! expansion.unregister()) {
//                    BukkitOfUtils.getInstance().logWarning("Failed to unregister expansion " + expansion.getIdentifier() + " from PlaceholderAPI.");
                }
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logWarning("Failed to unregister expansion " + expansion.getIdentifier() + " from PlaceholderAPI.", e);
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && (getEnableOverride() == -1 || getEnableOverride() == 1);
    }

    public void shutdown() {
        if (! isEnabledRaw()) return;

        flushAll();

        ownExpansion.unregister();

        loadedExpansions.clear();

        enableOverride = 0; // set to false

        BukkitOfUtils.getInstance().logInfo("&cPlaceholderAPI &fholder has been shut down.");
    }

    public boolean isEnabledRaw() {
        return super.isEnabled();
    }

    public String replace(OfflinePlayer player, String from) {
        if (! isEnabled()) return from;
        return PlaceholderAPI.setPlaceholders(player, from);
    }

    public String replace(String from) {
        return replace(EntityUtils.getDummyOfflinePlayer(), from);
    }
}
