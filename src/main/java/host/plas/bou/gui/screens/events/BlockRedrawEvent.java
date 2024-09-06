package host.plas.bou.gui.screens.events;

import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BlockRedrawEvent extends ScreenBlockEvent {
    public BlockRedrawEvent(ScreenBlock screenBlock) {
        super(screenBlock);
    }
}
