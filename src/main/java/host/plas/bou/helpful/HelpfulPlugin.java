package host.plas.bou.helpful;

import gg.drak.thebase.objects.Identified;
import host.plas.bou.helpful.data.Helpful;
import host.plas.bou.helpful.data.HelpfulGui;
import host.plas.bou.helpful.data.HelpfulInfo;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface HelpfulPlugin extends Identified {
    HelpfulInfo getHelpfulInfo();

    Helpful getHelpful();

    default HelpfulGui getHelpfulGui() {
        return getHelpful().asGui();
    }

    default void sendHelpful(CommandSender sender) {
        getHelpful().send(sender);
    }

    default void sendHelpfulGui(Player player) {
        getHelpful().sendGui(player);
    }
}
