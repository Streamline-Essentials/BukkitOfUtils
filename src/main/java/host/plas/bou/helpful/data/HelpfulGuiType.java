package host.plas.bou.helpful.data;

import host.plas.bou.gui.GuiType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class HelpfulGuiType implements GuiType {
    private HelpfulInfo helpfulInfo;

    @Override
    public String name() {
        return "BOU_HELPFUL_" + getHelpfulInfo().getIdentifier().toUpperCase();
    }

    @Override
    public String getTitle() {
        return getHelpfulInfo().getStylizedName();
    }

    public static HelpfulGuiType of(HelpfulInfo helpfulInfo) {
        return new HelpfulGuiType(helpfulInfo);
    }

    public static HelpfulGuiType of(Helpful helpful) {
        return new HelpfulGuiType(helpful.getInfo());
    }
}
