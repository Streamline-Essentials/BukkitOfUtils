package host.plas.bou.gui.screens.events;

import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;

/**
 * Event fired when a screen block's GUI needs to be redrawn for all viewers.
 */
@Getter @Setter
public class BlockRedrawEvent extends ScreenBlockEvent {
    /**
     * Constructs a new BlockRedrawEvent for the given screen block.
     *
     * @param screenBlock the screen block to be redrawn
     */
    public BlockRedrawEvent(ScreenBlock screenBlock) {
        super(screenBlock);
    }
}
