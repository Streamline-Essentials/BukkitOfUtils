package host.plas.bou.compat.papi.expansion;

import host.plas.bou.BetterPlugin;
import host.plas.bou.compat.papi.PAPIHolder;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tv.quaint.objects.Identified;

@Getter @Setter
public abstract class BetterExpansion extends PlaceholderExpansion implements Identified {
    private BetterPlugin betterPlugin;
    private boolean persistent;

    private String identifier;
    private String author;
    private String version;

    public BetterExpansion(BetterPlugin betterPlugin, String identifier, String author, String version, boolean persistent, boolean register) {
        this.betterPlugin = betterPlugin;
        this.identifier = identifier;
        this.author = author;
        this.version = version;
        this.persistent = persistent;

        if (register) register();
    }

    public BetterExpansion(BetterPlugin betterPlugin, String identifier, String author, String version, boolean persistentAndRegister) {
        this(betterPlugin, identifier, author, version, persistentAndRegister, persistentAndRegister);
    }

    public BetterExpansion(BetterPlugin betterPlugin, String identifier, String author, String version) {
        this(betterPlugin, identifier, author, version, true);
    }

    @Override
    public boolean register() {
        load();
        return super.register();
    }

    public void load() {
        PAPIHolder.loadExpansion(this);
    }

    public void unload() {
        PAPIHolder.unloadExpansion(this);
    }

    @Override
    public boolean persist() {
        return this.persistent;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        PlaceholderContext context = new PlaceholderContext(this, player, params);

        return replace(context);
    }

    public abstract @Nullable String replace(PlaceholderContext context);

    @Override
    public int compareTo(@NotNull Identified o) {
        return Identified.super.compareTo(o);
    }
}
