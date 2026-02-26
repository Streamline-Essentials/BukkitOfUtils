package host.plas.bou.compat.papi.expansion;

import host.plas.bou.BetterPlugin;
import host.plas.bou.compat.papi.PAPIHolder;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import gg.drak.thebase.objects.Identified;
import java.util.function.Supplier;

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
    @NotNull
    public String getIdentifier() {
        return identifierGetter.get();
    }

    @Override
    @NotNull
    public String getVersion() {
        return versionGetter.get();
    }

    @Override
    @NotNull
    public String getAuthor() {
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
    @Nullable
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    @Override
    @Nullable
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        PlaceholderContext context = new PlaceholderContext(this, player, params);
        return replace(context);
    }

    @Nullable
    public abstract String replace(PlaceholderContext context);

    @Override
    public int compareTo(@NotNull Identified o) {
        return Identified.super.compareTo(o);
    }

    public BetterPlugin getBetterPlugin() {
        return this.betterPlugin;
    }

    public boolean isPersistent() {
        return this.persistent;
    }

    public Supplier<String> getIdentifierGetter() {
        return this.identifierGetter;
    }

    public Supplier<String> getAuthorGetter() {
        return this.authorGetter;
    }

    public Supplier<String> getVersionGetter() {
        return this.versionGetter;
    }

    public void setBetterPlugin(final BetterPlugin betterPlugin) {
        this.betterPlugin = betterPlugin;
    }

    public void setPersistent(final boolean persistent) {
        this.persistent = persistent;
    }

    public void setIdentifierGetter(final Supplier<String> identifierGetter) {
        this.identifierGetter = identifierGetter;
    }

    public void setAuthorGetter(final Supplier<String> authorGetter) {
        this.authorGetter = authorGetter;
    }

    public void setVersionGetter(final Supplier<String> versionGetter) {
        this.versionGetter = versionGetter;
    }
}
