package host.plas.bou.gui.screens.events;

import host.plas.bou.events.self.BouEvent;
import host.plas.bou.gui.GuiType;
import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Base event class for screen block-related events. Provides access to the
 * associated screen block and convenience methods for its properties.
 */
@Getter @Setter
public class ScreenBlockEvent extends BouEvent {
    /**
     * The screen block associated with this event.
     *
     * @param screenBlock the screen block to set
     * @return the screen block for this event
     */
    private ScreenBlock screenBlock;

    /**
     * Constructs a new ScreenBlockEvent for the given screen block.
     *
     * @param screenBlock the screen block associated with this event
     */
    public ScreenBlockEvent(ScreenBlock screenBlock) {
        super();

        this.screenBlock = screenBlock;
    }

    /**
     * Returns the Bukkit block at the screen block's location.
     *
     * @return the {@link Block} at the screen block's location
     */
    public Block getBlock() {
        return screenBlock.getBlock();
    }

    /**
     * Returns the world location of the screen block.
     *
     * @return the {@link Location} of the screen block
     */
    public Location getLocation() {
        return screenBlock.getLocation();
    }

    /**
     * Returns the GUI type of the screen block.
     *
     * @return the {@link GuiType} of the screen block
     */
    public GuiType getType() {
        return screenBlock.getType();
    }
}
