package host.plas.bou.gui.screens.events;

import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter @Setter
public class BlockCloseEvent extends ScreenBlockEvent {
    private Player player;

    public BlockCloseEvent(Player player, ScreenBlock screenBlock) {
        super(screenBlock);

        this.player = player;
    }
}
