package host.plas.bou.gui.screens.events;

import host.plas.bou.events.self.BouEvent;
import host.plas.bou.gui.GuiType;
import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Getter @Setter
public class ScreenBlockEvent extends BouEvent {
    private ScreenBlock screenBlock;

    public ScreenBlockEvent(ScreenBlock screenBlock) {
        super();

        this.screenBlock = screenBlock;
    }

    public Block getBlock() {
        return screenBlock.getBlock();
    }

    public Location getLocation() {
        return screenBlock.getLocation();
    }

    public GuiType getType() {
        return screenBlock.getType();
    }
}
