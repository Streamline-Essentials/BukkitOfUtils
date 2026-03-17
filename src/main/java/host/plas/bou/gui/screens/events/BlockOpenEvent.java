package host.plas.bou.gui.screens.events;

import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

/**
 * Event fired when a player opens a screen block's GUI.
 */
@Getter @Setter
public class BlockOpenEvent extends ScreenBlockEvent {
    /**
     * The player who is opening the screen.
     * @param player the player to set
     * @return the player who is opening the screen
     */
    private Player player;
    /**
     * Whether this open event should override an existing screen.
     * @param override whether to override
     * @return whether this event overrides an existing screen
     */
    private boolean override;

    /**
     * Constructs a new BlockOpenEvent for the given player and screen block.
     * The override flag is set to {@code true} by default, allowing existing screens to be replaced.
     *
     * @param player      the player who is opening the screen
     * @param screenBlock the screen block being opened
     */
    public BlockOpenEvent(Player player, ScreenBlock screenBlock) {
        super(screenBlock);

        this.player = player;
        this.override = true;
    }
}
