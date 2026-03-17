package host.plas.bou.owncmd;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandContext;
import host.plas.bou.commands.SimplifiedCommand;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.utils.WorldUtils;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Command that reports the total number of entities on the server or in a specific world.
 * When no argument is provided, counts all entities server-wide.
 * When a world name is provided, counts entities only in that world.
 */
public class EntityCountCMD extends SimplifiedCommand {
    /**
     * Constructs the /entity-count command and registers it with the BukkitOfUtils plugin.
     */
    public EntityCountCMD() {
        super("entity-count", BukkitOfUtils.getInstance());
    }

    /**
     * Executes the entity-count command. Counts entities either server-wide or for
     * a specific world if a world name argument is provided.
     *
     * @param ctx the command context containing the sender and arguments
     * @return true if the command executed successfully
     */
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

    /**
     * Provides tab-completion suggestions for the entity-count command.
     * Suggests available world names when completing the first argument.
     *
     * @param ctx the command context containing the current arguments
     * @return a sorted set of tab-completion suggestions
     */
    public ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        ConcurrentSkipListSet<String> completions = new ConcurrentSkipListSet();
        if (ctx.getArgCount() <= 1) {
            completions.addAll(WorldUtils.getWorldNames());
        }

        return completions;
    }
}
