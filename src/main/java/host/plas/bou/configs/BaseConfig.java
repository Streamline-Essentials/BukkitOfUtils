package host.plas.bou.configs;

import host.plas.bou.BetterPlugin;
import gg.drak.thebase.storage.resources.flat.simple.SimpleConfiguration;

/**
 * Base configuration class for BetterPlugin instances.
 * Manages core configuration settings including console identity, logging preferences, and timer frequencies.
 */
public class BaseConfig extends SimpleConfiguration {
    /**
     * Constructs a new BaseConfig for the given plugin.
     *
     * @param betterPlugin the plugin instance this configuration belongs to
     */
    public BaseConfig(BetterPlugin betterPlugin) {
        super("base-config.yml", betterPlugin, false);
    }

    /**
     * Initializes all configuration values by loading their defaults.
     * This triggers the creation of default entries for console, logging, and timer settings.
     */
    @Override
    public void init() {
        // Console.
        getConsoleUUID();
        getConsoleName();

        // Logging.
        getIsInfoLoggingEnabled();
        getIsInfoLoggingPrefix();
        getIsWarnLoggingEnabled();
        getIsWarnLoggingPrefix();
        getIsSevereLoggingEnabled();
        getIsSevereLoggingPrefix();
        getIsDebugLoggingEnabled();
        getIsDebugLoggingPrefix();

        // Timers.
        getTickingFrequency();
        getEntityCollectionFrequency();

        // Version checker.
        isVersionCheckerEnabled();
        getModrinthProjectSlug();

        // OneMenu.
        isOneMenuEnabled();
        getOneMenuCommandAlias();

        // Player lookups.
        isPlayerLookupWebEnabled();
        getPlayerLookupTimeoutMillis();
        getPlayerLookupFoundCacheMinutes();
        getPlayerLookupMissingCacheMinutes();
        getPlayerLookupFailedCacheMinutes();
        getPlayerLookupMaxBackoffMinutes();
        getBedrockPrefixFallback();
    }

    /**
     * Whether the built-in Modrinth version checker should run on enable.
     *
     * @return {@code true} if version checking is enabled
     */
    public boolean isVersionCheckerEnabled() {
        reloadResource();
        return getOrSetDefault("version-checker.enabled", true);
    }

    /**
     * Modrinth project ID or slug used by the built-in version checker.
     *
     * @return Modrinth project id/slug (default {@code 6owv5fWs})
     */
    public String getModrinthProjectSlug() {
        reloadResource();
        return getOrSetDefault("version-checker.modrinth-project", "6owv5fWs");
    }

    /**
     * Whether the OneMenu command/GUI feature is enabled.
     *
     * @return {@code true} if OneMenu is enabled
     */
    public boolean isOneMenuEnabled() {
        reloadResource();
        return getOrSetDefault("one-menu.enabled", false);
    }

    /**
     * Optional alias for {@code /onemenu}. Empty string disables the alias.
     * Default is {@code menu}.
     *
     * @return command alias, or blank for none
     */
    public String getOneMenuCommandAlias() {
        reloadResource();
        return getOrSetDefault("one-menu.command-alias", "menu");
    }

    /**
     * Retrieves whether info-level logging is enabled.
     *
     * @return {@code true} if info logging is enabled, {@code false} otherwise
     */
    public boolean getIsInfoLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.info.enabled", true);
    }

    /**
     * Retrieves the prefix string used for info-level log messages.
     *
     * @return the info logging prefix with color codes
     */
    public String getIsInfoLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.info.prefix", "&a[&3INFO&a] &r");
    }

    /**
     * Retrieves whether warn-level logging is enabled.
     *
     * @return {@code true} if warn logging is enabled, {@code false} otherwise
     */
    public boolean getIsWarnLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.warn.enabled", true);
    }

    /**
     * Retrieves the prefix string used for warn-level log messages.
     *
     * @return the warn logging prefix with color codes
     */
    public String getIsWarnLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.warn.prefix", "&a[&3WARN&a] &e");
    }

    /**
     * Retrieves whether severe-level logging is enabled.
     *
     * @return {@code true} if severe logging is enabled, {@code false} otherwise
     */
    public boolean getIsSevereLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.severe.enabled", true);
    }

    /**
     * Retrieves the prefix string used for severe-level log messages.
     *
     * @return the severe logging prefix with color codes
     */
    public String getIsSevereLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.severe.prefix", "&a[&3SEVERE&a] &c");
    }

    /**
     * Retrieves whether debug-level logging is enabled.
     *
     * @return {@code true} if debug logging is enabled, {@code false} otherwise
     */
    public boolean getIsDebugLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.debug.enabled", false);
    }

    /**
     * Retrieves the prefix string used for debug-level log messages.
     *
     * @return the debug logging prefix with color codes
     */
    public String getIsDebugLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.debug.prefix", "&a[&3DEBUG&a] &d");
    }

    /**
     * Retrieves the UUID string used to identify the console.
     *
     * @return the console UUID string
     */
    public String getConsoleUUID() {
        reloadResource();

        return getOrSetDefault("console.uuid", "%");
    }

    /**
     * Retrieves the display name used for the console.
     *
     * @return the console display name
     */
    public String getConsoleName() {
        reloadResource();

        return getOrSetDefault("console.name", "Console");
    }

    /**
     * Retrieves the ticking frequency in milliseconds for scheduled tasks.
     *
     * @return the ticking frequency value
     */
    public int getTickingFrequency() {
        reloadResource();

        return getOrSetDefault("timers.ticking-frequency", 50);
    }

    /**
     * Retrieves the frequency at which entities are collected, in ticks.
     *
     * @return the entity collection frequency value
     */
    public long getEntityCollectionFrequency() {
        reloadResource();

        return getOrSetDefault("timers.entity-collection.frequency", 10L);
    }

    /**
     * Whether player lookups may contact web APIs (Geyser global API, PlayerDB, MineTools).
     * When disabled, lookups only use data already on the server. Mojang is never contacted.
     *
     * @return true if web lookups are enabled
     */
    public boolean isPlayerLookupWebEnabled() {
        reloadResource();

        return getOrSetDefault("player-lookups.web.enabled", true);
    }

    /**
     * Connect and read timeout for each web lookup request, in milliseconds.
     *
     * @return the timeout in milliseconds
     */
    public int getPlayerLookupTimeoutMillis() {
        reloadResource();

        return getOrSetDefault("player-lookups.web.timeout-millis", 4000);
    }

    /**
     * How long a successful lookup is cached, in minutes.
     *
     * @return the cache duration in minutes
     */
    public long getPlayerLookupFoundCacheMinutes() {
        reloadResource();

        return getOrSetDefault("player-lookups.cache.found-minutes", 360L);
    }

    /**
     * How long a "no such player" answer is cached, in minutes.
     *
     * @return the cache duration in minutes
     */
    public long getPlayerLookupMissingCacheMinutes() {
        reloadResource();

        return getOrSetDefault("player-lookups.cache.missing-minutes", 30L);
    }

    /**
     * How long an incomplete or failed lookup is cached before it is retried, in minutes.
     *
     * @return the cache duration in minutes
     */
    public long getPlayerLookupFailedCacheMinutes() {
        reloadResource();

        return getOrSetDefault("player-lookups.cache.failed-minutes", 5L);
    }

    /**
     * The longest a web provider is skipped after repeated failures (timeouts, rate limits,
     * outages), in minutes. Backoff starts at 30 seconds and doubles per consecutive failure.
     *
     * @return the maximum backoff in minutes
     */
    public long getPlayerLookupMaxBackoffMinutes() {
        reloadResource();

        return getOrSetDefault("player-lookups.backoff.max-minutes", 30L);
    }

    /**
     * The Bedrock username prefix to assume when Floodgate is not installed on this server
     * (for example when it only runs on the proxy). Floodgate's own value wins when present.
     *
     * @return the prefix
     */
    public String getBedrockPrefixFallback() {
        reloadResource();

        return getOrSetDefault("player-lookups.bedrock.prefix", ".");
    }
}
