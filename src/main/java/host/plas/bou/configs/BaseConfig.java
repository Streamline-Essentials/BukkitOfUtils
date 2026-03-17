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
}
