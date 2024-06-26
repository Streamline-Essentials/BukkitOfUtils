package io.streamlined.bukkit.configs;

import io.streamlined.bukkit.instances.BaseManager;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;

public class BaseConfig extends SimpleConfiguration {
    public BaseConfig() {
        super("base-config.yml", BaseManager.getBaseInstance(), false);
    }

    @Override
    public void init() {
        getIsInfoLoggingEnabled();
        getIsInfoLoggingPrefix();
        getIsWarnLoggingEnabled();
        getIsWarnLoggingPrefix();
        getIsSevereLoggingEnabled();
        getIsSevereLoggingPrefix();
        getIsDebugLoggingEnabled();
        getIsDebugLoggingPrefix();
        getConsoleUUID();
        getConsoleName();
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

        return getOrSetDefault("logging.warn", true);
    }

    public String getIsWarnLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.warn.prefix", "&a[&3WARN&a] &e");
    }

    public boolean getIsSevereLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.severe", true);
    }

    public String getIsSevereLoggingPrefix() {
        reloadResource();

        return getOrSetDefault("logging.severe.prefix", "&a[&3SEVERE&a] &c");
    }

    public boolean getIsDebugLoggingEnabled() {
        reloadResource();

        return getOrSetDefault("logging.debug", false);
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
}
