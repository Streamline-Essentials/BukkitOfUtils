package host.plas.bou.gui.screens.events;

import host.plas.bou.gui.screens.blocks.ScreenBlock;
import org.bukkit.entity.Player;

public class BlockCloseEvent extends ScreenBlockEvent {
    private Player player;

    public BlockCloseEvent(Player player, ScreenBlock screenBlock) {
        super(screenBlock);
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }
}
