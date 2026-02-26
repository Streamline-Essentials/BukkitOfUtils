package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.utils.WorldUtils;
import java.util.concurrent.ConcurrentSkipListSet;

public class EntityCountCMD extends SimplifiedCommand {
    public EntityCountCMD() {
        super("entity-count", BukkitOfUtils.getInstance());
    }

    public boolean command(CommandContext ctx) {
        if (!ctx.isArgUsable(0)) {
            EntityUtils.collectEntitiesThenDoSet((entities) -> {
                int count = entities.size();
                ctx.sendMessage("&eThere are &a" + count + " &centities &ein the entire server.");
            });
            return true;
        } else {
            String worldName = ctx.getStringArg(0);
            EntityUtils.collectEntitiesInWorldThenDoSet(worldName, (entities) -> {
                int count = entities.size();
                ctx.sendMessage("&eThere are &a" + count + " &centities &ein the world &a" + worldName + "&e.");
            });
            return true;
        }
    }

    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        ConcurrentSkipListSet<String> completions = new ConcurrentSkipListSet();
        if (ctx.getArgCount() <= 1) {
            completions.addAll(WorldUtils.getWorldNames());
        }

        return completions;
    }
}
