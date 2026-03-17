package host.plas.bou.gui.screens.events;

import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

/**
 * Event fired when a player closes a screen block's GUI.
 */
@Getter @Setter
public class BlockCloseEvent extends ScreenBlockEvent {
    /**
     * The player who closed the screen.
     * @param player the player to set
     * @return the player who closed the screen
     */
    private Player player;

    /**
     * Constructs a new BlockCloseEvent for the given player and screen block.
     *
     * @param player      the player who closed the screen
     * @param screenBlock the screen block whose screen was closed
     */
    public BlockCloseEvent(Player player, ScreenBlock screenBlock) {
        super(screenBlock);

        this.player = player;
    }
}
