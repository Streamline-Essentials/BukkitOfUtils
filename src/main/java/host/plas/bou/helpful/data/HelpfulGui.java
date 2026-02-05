package host.plas.bou.helpful.data;

import host.plas.bou.gui.GuiHelper;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.screens.ScreenInstance;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter @Setter
public class HelpfulGui implements HelpfulIdentified {
    private Helpful helpful;
    private HelpfulGuiType type;

    public HelpfulInfo getInfo() {
        return helpful.getInfo();
    }

    public void setInfo(HelpfulInfo info) {
        helpful.setInfo(info);
    }

    public HelpfulGui(Helpful helpful, HelpfulGuiType type) {
        this.helpful = helpful;
        this.type = type;
    }

    public HelpfulGui(Helpful helpful) {
        this(helpful, HelpfulGuiType.of(helpful));
    }

    public ScreenInstance makeScreenInstance(Player player) {
        return new ScreenInstance(player, type, makeInventorySheet());
    }

    public InventorySheet makeInventorySheet() {
        return InventorySheet.of(this, GuiHelper.getMiddleSlot(2), Material.BOOK);
    }

    public void open(Player player) {
        makeScreenInstance(player).open();
    }

    public static HelpfulGui of(Helpful helpful, HelpfulGuiType type) {
        return new HelpfulGui(helpful, type);
    }

    public static HelpfulGui of(Helpful helpful) {
        return new HelpfulGui(helpful);
    }
}
