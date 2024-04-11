package io.streamlined.bukkit;

import io.streamlined.bukkit.configs.BaseConfig;
import io.streamlined.bukkit.instances.BaseManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import tv.quaint.objects.handling.derived.IModifierEventable;

@Getter @Setter
public class PluginBase extends JavaPlugin implements IModifierEventable {
    @Getter
    private final ModifierType modifierType;

    @Getter @Setter
    private static BaseConfig baseConfig;

    @Getter @Setter
    private static PluginBase baseInstance;

    @Override
    public String getIdentifier() {
        return getName();
    }

    public PluginBase() {
        modifierType = ModifierType.PLUGIN;

        onBaseConstruct();
    }

    public void onBaseConstruct() {}
    public void onBaseLoad() {}
    public void onBaseEnabling() {}
    public void onBaseEnabled() {}
    public void onBaseDisable() {}

    @Override
    public void onLoad() {
        onBaseLoad();
    }

    @Override
    public void onEnable() {
        onBaseEnabling();

        baseInstance = this;

        BaseManager.init(this);

        baseConfig = new BaseConfig();

        onBaseEnabled();
    }

    @Override
    public void onDisable() {
        onBaseDisable();
        BaseManager.stop();
    }

    @Override
    public boolean isPlugin() {
        return true;
    }

    @Override
    public boolean isMod() {
        return false;
    }

    @Override
    public boolean isStreamline() {
        return false;
    }

    @Override
    public void initializeDataFolder() {
        if (! getDataFolder().mkdirs()) {
            MessageUtils.logWarning("Could not create data folder for " + getIdentifier() + "!");
        }
    }
}
