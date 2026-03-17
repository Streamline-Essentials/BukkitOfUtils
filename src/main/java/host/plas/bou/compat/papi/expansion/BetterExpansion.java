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

/**
 * Abstract base class for PlaceholderAPI expansions within the BukkitOfUtils framework.
 * Provides lifecycle management (load/unload), multiple constructor variants,
 * and delegates placeholder resolution to the {@link #replace(PlaceholderContext)} method.
 */
@Getter @Setter
public abstract class BetterExpansion extends PlaceholderExpansion implements Identified {
    /**
     * The owning plugin for this expansion.
     * @param betterPlugin the owning plugin
     * @return the owning plugin
     */
    private BetterPlugin betterPlugin;
    /**
     * Whether this expansion should persist across PlaceholderAPI reloads.
     * @param persistent whether the expansion is persistent
     * @return whether the expansion is persistent
     */
    private boolean persistent;

    /**
     * Supplier for the expansion identifier string.
     * @param identifierGetter supplier for the expansion identifier
     * @return supplier for the expansion identifier
     */
    private Supplier<String> identifierGetter;
    /**
     * Supplier for the expansion author string.
     * @param authorGetter supplier for the expansion author
     * @return supplier for the expansion author
     */
    private Supplier<String> authorGetter;
    /**
     * Supplier for the expansion version string.
     * @param versionGetter supplier for the expansion version
     * @return supplier for the expansion version
     */
    private Supplier<String> versionGetter;

    /**
     * Constructs a BetterExpansion with full control over all parameters.
     *
     * @param betterPlugin the owning plugin
     * @param identifierGetter supplier for the expansion identifier
     * @param authorGetter supplier for the expansion author
     * @param versionGetter supplier for the expansion version
     * @param persistent whether this expansion should persist across reloads
     * @param register whether to register the expansion immediately
     */
    public BetterExpansion(BetterPlugin betterPlugin, Supplier<String> identifierGetter, Supplier<String> authorGetter, Supplier<String> versionGetter, boolean persistent, boolean register) {
        this.betterPlugin = betterPlugin;
        this.identifierGetter = identifierGetter;
        this.authorGetter = authorGetter;
        this.versionGetter = versionGetter;
        this.persistent = persistent;

        if (register) register();
    }

    /**
     * Constructs a BetterExpansion where persistent and register share the same value.
     *
     * @param betterPlugin the owning plugin
     * @param identifierGetter supplier for the expansion identifier
     * @param authorGetter supplier for the expansion author
     * @param versionGetter supplier for the expansion version
     * @param persistentAndRegister whether this expansion should be persistent and registered immediately
     */
    public BetterExpansion(BetterPlugin betterPlugin, Supplier<String> identifierGetter, Supplier<String> authorGetter, Supplier<String> versionGetter, boolean persistentAndRegister) {
        this(betterPlugin, identifierGetter, authorGetter, versionGetter, persistentAndRegister, persistentAndRegister);
    }

    /**
     * Constructs a BetterExpansion with supplier-based metadata, defaulting to persistent and auto-registered.
     *
     * @param betterPlugin the owning plugin
     * @param identifierGetter supplier for the expansion identifier
     * @param authorGetter supplier for the expansion author
     * @param versionGetter supplier for the expansion version
     */
    public BetterExpansion(BetterPlugin betterPlugin, Supplier<String> identifierGetter, Supplier<String> authorGetter, Supplier<String> versionGetter) {
        this(betterPlugin, identifierGetter, authorGetter, versionGetter, true);
    }

    /**
     * Constructs a BetterExpansion with a string identifier and supplier-based author/version.
     *
     * @param betterPlugin the owning plugin
     * @param identifier the expansion identifier
     * @param authorGetter supplier for the expansion author
     * @param versionGetter supplier for the expansion version
     * @param persistent whether this expansion should persist across reloads
     * @param register whether to register the expansion immediately
     */
    public BetterExpansion(BetterPlugin betterPlugin, String identifier, Supplier<String> authorGetter, Supplier<String> versionGetter, boolean persistent, boolean register) {
        this(betterPlugin, () -> identifier, authorGetter, versionGetter, persistent, register);
    }

    /**
     * Constructs a BetterExpansion with a string identifier where persistent and register share the same value.
     *
     * @param betterPlugin the owning plugin
     * @param identifier the expansion identifier
     * @param authorGetter supplier for the expansion author
     * @param versionGetter supplier for the expansion version
     * @param persistentAndRegister whether this expansion should be persistent and registered immediately
     */
    public BetterExpansion(BetterPlugin betterPlugin, String identifier, Supplier<String> authorGetter, Supplier<String> versionGetter, boolean persistentAndRegister) {
        this(betterPlugin, identifier, authorGetter, versionGetter, persistentAndRegister, persistentAndRegister);
    }

    /**
     * Constructs a BetterExpansion with a string identifier, defaulting to persistent and auto-registered.
     *
     * @param betterPlugin the owning plugin
     * @param identifier the expansion identifier
     * @param authorGetter supplier for the expansion author
     * @param versionGetter supplier for the expansion version
     */
    public BetterExpansion(BetterPlugin betterPlugin, String identifier, Supplier<String> authorGetter, Supplier<String> versionGetter) {
        this(betterPlugin, identifier, authorGetter, versionGetter, true);
    }

    /**
     * Constructs a BetterExpansion with all string-based metadata.
     *
     * @param betterPlugin the owning plugin
     * @param identifier the expansion identifier
     * @param author the expansion author
     * @param version the expansion version
     * @param persistent whether this expansion should persist across reloads
     * @param register whether to register the expansion immediately
     */
    public BetterExpansion(BetterPlugin betterPlugin, String identifier, String author, String version, boolean persistent, boolean register) {
        this(betterPlugin, () -> identifier, () -> author, () -> version, persistent, register);
    }

    /**
     * Constructs a BetterExpansion with all string-based metadata where persistent and register share the same value.
     *
     * @param betterPlugin the owning plugin
     * @param identifier the expansion identifier
     * @param author the expansion author
     * @param version the expansion version
     * @param persistentAndRegister whether this expansion should be persistent and registered immediately
     */
    public BetterExpansion(BetterPlugin betterPlugin, String identifier, String author, String version, boolean persistentAndRegister) {
        this(betterPlugin, identifier, author, version, persistentAndRegister, persistentAndRegister);
    }

    /**
     * Constructs a BetterExpansion with all string-based metadata, defaulting to persistent and auto-registered.
     *
     * @param betterPlugin the owning plugin
     * @param identifier the expansion identifier
     * @param author the expansion author
     * @param version the expansion version
     */
    public BetterExpansion(BetterPlugin betterPlugin, String identifier, String author, String version) {
        this(betterPlugin, identifier, author, version, true);
    }

    /**
     * Returns the expansion identifier from the identifier supplier.
     *
     * @return the expansion identifier
     */
    @Override
    public @NotNull String getIdentifier() {
        return identifierGetter.get();
    }

    /**
     * Returns the expansion version from the version supplier.
     *
     * @return the expansion version
     */
    @Override
    public @NotNull String getVersion() {
        return versionGetter.get();
    }

    /**
     * Returns the expansion author from the author supplier.
     *
     * @return the expansion author
     */
    @Override
    public @NotNull String getAuthor() {
        return authorGetter.get();
    }

    /**
     * Registers this expansion with PlaceholderAPI, loading it into the
     * tracked expansions set first.
     *
     * @return true if the registration was successful
     */
    @Override
    public boolean register() {
        load();
        return super.register();
    }

    /**
     * Loads this expansion into the {@link PAPIHolder} tracked expansions set.
     */
    public void load() {
        PAPIHolder.loadExpansion(this);
    }

    /**
     * Unloads this expansion from the {@link PAPIHolder} tracked expansions set.
     */
    public void unload() {
        PAPIHolder.unloadExpansion(this);
    }

    /**
     * Returns whether this expansion should persist across PlaceholderAPI reloads.
     *
     * @return true if the expansion is persistent
     */
    @Override
    public boolean persist() {
        return this.persistent;
    }

    /**
     * Handles a placeholder request from an online player by delegating to
     * {@link #onRequest(OfflinePlayer, String)}.
     *
     * @param player the online player requesting the placeholder
     * @param params the placeholder parameters
     * @return the replacement string, or null if not handled
     */
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    /**
     * Handles a placeholder request by creating a {@link PlaceholderContext}
     * and delegating to {@link #replace(PlaceholderContext)}.
     *
     * @param player the offline player requesting the placeholder
     * @param params the placeholder parameters
     * @return the replacement string, or null if not handled
     */
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        PlaceholderContext context = new PlaceholderContext(this, player, params);

        return replace(context);
    }

    /**
     * Resolves a placeholder given the provided context.
     * Subclasses must implement this to provide their placeholder logic.
     *
     * @param context the placeholder context containing the player and parameters
     * @return the replacement string, or null if the placeholder is not recognized
     */
    public abstract @Nullable String replace(PlaceholderContext context);

    /**
     * Compares this expansion to another {@link Identified} instance
     * using the default comparison.
     *
     * @param o the other Identified instance to compare to
     * @return the comparison result
     */
    @Override
    public int compareTo(@NotNull Identified o) {
        return Identified.super.compareTo(o);
    }
}
