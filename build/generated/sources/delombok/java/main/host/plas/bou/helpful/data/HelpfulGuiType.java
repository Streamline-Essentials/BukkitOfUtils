package host.plas.bou.helpful.data;

import host.plas.bou.gui.GuiType;

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

    public void setHelpfulInfo(final HelpfulInfo helpfulInfo) {
        this.helpfulInfo = helpfulInfo;
    }

    public HelpfulInfo getHelpfulInfo() {
        return this.helpfulInfo;
    }
}
