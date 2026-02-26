package host.plas.bou;

import host.plas.bou.bstats.BStats;
import host.plas.bou.commands.CommandBuilder;
import host.plas.bou.commands.CommandResult;
import host.plas.bou.compat.CompatManager;
import host.plas.bou.firestring.FireStringManager;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.helpful.HelpfulPlugin;
import host.plas.bou.helpful.data.Helpful;
import host.plas.bou.helpful.data.HelpfulInfo;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.owncmd.*;
import host.plas.bou.utils.ClassHelper;
import host.plas.bou.utils.obj.Versioning;

public class BukkitOfUtils extends BetterPlugin implements HelpfulPlugin {
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
        new BouHelpCMD();
        new DebugCMD();
        new EntityCountCMD();
        new FireStringCMD();
        new GetItemCMD();
        new MessageCMD();
        new TitleCMD();
        new CommandBuilder("bouversion", this).addAliases("bouv").setExecutionHandler(ctx -> {
            Versioning versioning = Versioning.getServerVersion();
            ctx.sendMessage("&7You are running &eversion &a" + getDescription().getVersion() + " &7of &c&lBukkitOfUtils&7.");
            ctx.sendMessage("&7Server is &eflagged &7as &a" + versioning.toString() + " &e(&fExact&7: &a" + Versioning.getBukkitVersion() + "&e)" + "&7.");
            ctx.sendMessage("   &7-> &bIs &c&lEmpty &bVersioning&7? " + (versioning.isEmpty() ? "&aYes" : "&cNo"));
            ctx.sendMessage("   &7-> &bIs &c&lModern &bVersioning&7? " + (versioning.isModern() ? "&aYes" : "&cNo"));
            return CommandResult.SUCCESS;
        }).build();
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

    @Override
    public HelpfulInfo getHelpfulInfo() {
        return HelpfulInfo.of("bukkitofutils", "BukkitOfUtils", "&6&lBukkitOfUtils");
    }

    @Override
    public Helpful getHelpful() {
        return new Helpful(getHelpfulInfo(), "&eA utility plugin for Bukkit-based servers.", "&eProvides various utilities, commands, and features to enhance server management and gameplay experience.", "&r", "&eCommands Included:", "&b- /debug: &7Toggle debug mode.", "&b- /entitycount: &7Check entity counts on the server.", "&b- /firestring: &7Execute Fire Strings for dynamic actions.", "&b- /message: &7Send custom messages to players.", "&b- /title: &7Display titles to players.&r", "&r", "&eFor more information, visit the wiki:", "&bhttps://wiki.drak.gg/bukkitofutils/");
    }

    public static BukkitOfUtils getInstance() {
        return BukkitOfUtils.instance;
    }

    public static void setInstance(final BukkitOfUtils instance) {
        BukkitOfUtils.instance = instance;
    }
}
