package host.plas.bou.configs;

import host.plas.bou.BetterPlugin;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;

public class BaseConfig extends SimpleConfiguration {
    public BaseConfig(BetterPlugin betterPlugin) {
        super("base-config.yml", betterPlugin, false);
    }

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

    public boolean getIsInfoLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.info.enabled", true);
    }

    public String getIsInfoLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.info.prefix", "&a[&3INFO&a] &r");
    }

    public boolean getIsWarnLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.warn.enabled", true);
    }

    public String getIsWarnLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.warn.prefix", "&a[&3WARN&a] &e");
    }

    public boolean getIsSevereLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.severe.enabled", true);
    }

    public String getIsSevereLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.severe.prefix", "&a[&3SEVERE&a] &c");
    }

    public boolean getIsDebugLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.debug.enabled", false);
    }

    public String getIsDebugLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.debug.prefix", "&a[&3DEBUG&a] &d");
    }

    public String getConsoleUUID() {
        reloadResource();

        return getOrSetDefault("console.uuid", "%");
    }

    public String getConsoleName() {
        reloadResource();

        return getOrSetDefault("console.name", "Console");
    }

    public int getTickingFrequency() {
        reloadResource();

        return getOrSetDefault("timers.ticking-frequency", 50);
    }

    public long getEntityCollectionFrequency() {
        reloadResource();

        return getOrSetDefault("timers.entity-collection.frequency", 10L);
    }
}
