package host.plas.bou.gui.screens.events;

import host.plas.bou.gui.screens.blocks.ScreenBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter @Setter
public class BlockOpenEvent extends ScreenBlockEvent {
    private Player player;
    private boolean override;

    public BlockOpenEvent(Player player, ScreenBlock screenBlock) {
        super(screenBlock);

        this.player = player;
        this.override = true;
    }
}
