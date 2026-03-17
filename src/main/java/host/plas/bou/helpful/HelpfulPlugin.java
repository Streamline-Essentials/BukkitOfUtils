package host.plas.bou.helpful;

import gg.drak.thebase.objects.Identified;
import host.plas.bou.helpful.data.Helpful;
import host.plas.bou.helpful.data.HelpfulGui;
import host.plas.bou.helpful.data.HelpfulInfo;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Interface for plugins that provide helpful information to players.
 * Extends {@link Identified} to provide identification capabilities.
 */
public interface HelpfulPlugin extends Identified {
    /**
     * Retrieves the helpful information metadata for this plugin.
     *
     * @return the helpful info containing identifier, pretty name, and stylized name
     */
    HelpfulInfo getHelpfulInfo();

    /**
     * Retrieves the helpful data object containing the text document and info.
     *
     * @return the helpful data object
     */
    Helpful getHelpful();

    /**
     * Retrieves the GUI representation of the helpful data.
     *
     * @return a {@link HelpfulGui} created from this plugin's helpful data
     */
    default HelpfulGui getHelpfulGui() {
        return this.getHelpful().asGui();
    }

    /**
     * Sends the helpful text document to a command sender.
     *
     * @param sender the command sender to receive the helpful information
     */
    default void sendHelpful(CommandSender sender) {
        this.getHelpful().send(sender);
    }

    /**
     * Opens the helpful GUI for a player.
     *
     * @param player the player to open the GUI for
     */
    default void sendHelpfulGui(Player player) {
        this.getHelpful().sendGui(player);
    }
}
