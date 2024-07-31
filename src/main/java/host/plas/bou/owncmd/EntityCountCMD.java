package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.utils.EntityUtils;

public class EntityCountCMD extends SimplifiedCommand {
    public EntityCountCMD() {
        super("entity-count", BukkitOfUtils.getInstance());
    }

    @Override
    public boolean command(CommandContext ctx) {
        EntityUtils.collectEntitiesThenDoSet(entities -> {
            int count = entities.size();
            ctx.sendMessage("&eThere are &a" + count + " &centities &ein the entire server.");
        });
        return true;
    }
}
