package host.plas.bou;

import host.plas.bou.bstats.BStats;
import host.plas.bou.commands.CommandBuilder;
import host.plas.bou.commands.CommandResult;
import host.plas.bou.compat.CompatManager;
import host.plas.bou.firestring.FireStringManager;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.owncmd.*;
import host.plas.bou.utils.ClassHelper;
import host.plas.bou.utils.obj.Versioning;
import lombok.Getter;
import lombok.Setter;

public class BukkitOfUtils extends BetterPlugin {
    @Getter @Setter
    private static BukkitOfUtils instance;

    public BukkitOfUtils() {
        super();
    }

    @Override
    public void onLoad() {
        setInstance(this);

        BaseManager.init(this);
    }

    @Override
    public void onBaseEnabled() {
        BaseManager.initOnEnabled();

        // Plugin startup logic
//        instance = this; // Set earlier.
        new DebugCMD();
        new EntityCountCMD();
        new FireStringCMD();
        new MessageCMD();
        new TitleCMD();

        new CommandBuilder("bouversion", this)
                .addAliases("bouv")
                .setExecutionHandler(ctx -> {
                    Versioning versioning = Versioning.getServerVersion();

                    ctx.sendMessage("&7You are running &eversion &a" + getDescription().getVersion() + " &7of &c&lBukkitOfUtils&7.");
                    ctx.sendMessage("&7Server is &eflagged &7as &a" + versioning.toString() + " &e(&fExact&7: &a" + Versioning.getBukkitVersion() + "&e)" + "&7.");
                    ctx.sendMessage("   &7-> &bIs &c&lEmpty &bVersioning&7? " + (versioning.isEmpty() ? "&aYes" : "&cNo"));
                    ctx.sendMessage("   &7-> &bIs &c&lModern &bVersioning&7? " + (versioning.isModern() ? "&aYes" : "&cNo"));

                    return CommandResult.SUCCESS;
                })
                .build();

        ClassHelper.init();
        ScreenManager.init();
        FireStringManager.init();

        CompatManager.init();

        BStats.onEnable();
    }

    @Override
    public void onBaseDisable() {
        BStats.onDisable();

        BaseManager.stop();
    }
}
