package host.plas.bou.helpful.data;

import host.plas.bou.gui.GuiHelper;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.screens.ScreenInstance;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HelpfulGui implements HelpfulIdentified {
    private Helpful helpful;
    private HelpfulGuiType type;

    public HelpfulInfo getInfo() {
        return this.helpful.getInfo();
    }

    public void setInfo(HelpfulInfo info) {
        this.helpful.setInfo(info);
    }

    public HelpfulGui(Helpful helpful, HelpfulGuiType type) {
        this.helpful = helpful;
        this.type = type;
    }

    public HelpfulGui(Helpful helpful) {
        this(helpful, HelpfulGuiType.of(helpful));
    }

    public ScreenInstance makeScreenInstance(Player player) {
        return new ScreenInstance(player, this.type, this.makeInventorySheet());
    }

    public InventorySheet makeInventorySheet() {
        return InventorySheet.of(this, GuiHelper.getMiddleSlot(2), Material.BOOK);
    }

    public void open(Player player) {
        this.makeScreenInstance(player).open();
    }

    public static HelpfulGui of(Helpful helpful, HelpfulGuiType type) {
        return new HelpfulGui(helpful, type);
    }

    public static HelpfulGui of(Helpful helpful) {
        return new HelpfulGui(helpful);
    }

    public void setHelpful(final Helpful helpful) {
        this.helpful = helpful;
    }

    public void setType(final HelpfulGuiType type) {
        this.type = type;
    }

    public Helpful getHelpful() {
        return this.helpful;
    }

    public HelpfulGuiType getType() {
        return this.type;
    }
}
