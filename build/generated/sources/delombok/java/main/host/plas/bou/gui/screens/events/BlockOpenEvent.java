package host.plas.bou.gui.screens.events;

import host.plas.bou.gui.screens.blocks.ScreenBlock;
import org.bukkit.entity.Player;

public class BlockOpenEvent extends ScreenBlockEvent {
    private Player player;
    private boolean override;

    public BlockOpenEvent(Player player, ScreenBlock screenBlock) {
        super(screenBlock);
        this.player = player;
        this.override = true;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isOverride() {
        return this.override;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    public void setOverride(final boolean override) {
        this.override = override;
    }
}
