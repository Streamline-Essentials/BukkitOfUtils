package host.plas.bou.helpful.data;

import host.plas.bou.gui.GuiType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HelpfulGuiType implements GuiType {
    private HelpfulInfo helpfulInfo;

    public String name() {
        return "BOU_HELPFUL_" + this.getHelpfulInfo().getIdentifier().toUpperCase();
    }

    public String getTitle() {
        return this.getHelpfulInfo().getStylizedName();
    }

    public static HelpfulGuiType of(HelpfulInfo helpfulInfo) {
        return new HelpfulGuiType(helpfulInfo);
    }

    public static HelpfulGuiType of(Helpful helpful) {
        return new HelpfulGuiType(helpful.getInfo());
    }

    public HelpfulGuiType(HelpfulInfo helpfulInfo) {
        this.helpfulInfo = helpfulInfo;
    }
}
