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
import gg.drak.thebase.objects.Identified;

import java.util.function.Supplier;

@Getter @Setter
public abstract class BetterExpansion extends PlaceholderExpansion implements Identified {
    private BetterPlugin betterPlugin;
    private boolean persistent;

    private Supplier<String> identifierGetter;
    private Supplier<String> authorGetter;
    private Supplier<String> versionGetter;

    public BetterExpansion(BetterPlugin betterPlugin, Supplier<String> identifierGetter, Supplier<String> authorGetter, Supplier<String> versionGetter, boolean persistent, boolean register) {
        this.betterPlugin = betterPlugin;
        this.identifierGetter = identifierGetter;
        this.authorGetter = authorGetter;
        this.versionGetter = versionGetter;
        this.persistent = persistent;

        if (register) register();
    }

    public BetterExpansion(BetterPlugin betterPlugin, Supplier<String> identifierGetter, Supplier<String> authorGetter, Supplier<String> versionGetter, boolean persistentAndRegister) {
        this(betterPlugin, identifierGetter, authorGetter, versionGetter, persistentAndRegister, persistentAndRegister);
    }

    public BetterExpansion(BetterPlugin betterPlugin, Supplier<String> identifierGetter, Supplier<String> authorGetter, Supplier<String> versionGetter) {
        this(betterPlugin, identifierGetter, authorGetter, versionGetter, true);
    }

    public BetterExpansion(BetterPlugin betterPlugin, String identifier, Supplier<String> authorGetter, Supplier<String> versionGetter, boolean persistent, boolean register) {
        this(betterPlugin, () -> identifier, authorGetter, versionGetter, persistent, register);
    }

    public BetterExpansion(BetterPlugin betterPlugin, String identifier, Supplier<String> authorGetter, Supplier<String> versionGetter, boolean persistentAndRegister) {
        this(betterPlugin, identifier, authorGetter, versionGetter, persistentAndRegister, persistentAndRegister);
    }

    public BetterExpansion(BetterPlugin betterPlugin, String identifier, Supplier<String> authorGetter, Supplier<String> versionGetter) {
        this(betterPlugin, identifier, authorGetter, versionGetter, true);
    }

    public BetterExpansion(BetterPlugin betterPlugin, String identifier, String author, String version, boolean persistent, boolean register) {
        this(betterPlugin, () -> identifier, () -> author, () -> version, persistent, register);
    }

    public BetterExpansion(BetterPlugin betterPlugin, String identifier, String author, String version, boolean persistentAndRegister) {
        this(betterPlugin, identifier, author, version, persistentAndRegister, persistentAndRegister);
    }

    public BetterExpansion(BetterPlugin betterPlugin, String identifier, String author, String version) {
        this(betterPlugin, identifier, author, version, true);
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifierGetter.get();
    }

    @Override
    public @NotNull String getVersion() {
        return versionGetter.get();
    }

    @Override
    public @NotNull String getAuthor() {
        return authorGetter.get();
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
