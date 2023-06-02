package io.streamlined.bukkit;

import io.streamlined.bukkit.configs.BaseConfig;
import io.streamlined.bukkit.instances.InstanceManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import tv.quaint.objects.handling.derived.IModifierEventable;

public class BukkitBase extends JavaPlugin implements IModifierEventable {
    @Getter
    private final ModifierType modifierType;
    @Getter @Setter
    private String identifier;

    @Getter @Setter
    private static BaseConfig baseConfig;

    public BukkitBase(String identifier) {
        modifierType = ModifierType.PLUGIN;
        this.identifier = identifier;

        InstanceManager.init(this);

        baseConfig = new BaseConfig();
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
