package host.plas.bou.helpful.data;

import host.plas.bou.gui.GuiType;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines the GUI type for a helpful screen, implementing {@link GuiType}
 * to provide a name and title derived from the associated {@link HelpfulInfo}.
 */
@Setter
@Getter
public class HelpfulGuiType implements GuiType {
    /**
     * The helpful info used to derive the GUI type name and title.
     *
     * @param helpfulInfo the helpful info to set
     * @return the current helpful info
     */
    private HelpfulInfo helpfulInfo;

    /**
     * Returns the unique name for this GUI type, prefixed with "BOU_HELPFUL_"
     * followed by the uppercase identifier.
     *
     * @return the GUI type name
     */
    public String name() {
        return "BOU_HELPFUL_" + this.getHelpfulInfo().getIdentifier().toUpperCase();
    }

    /**
     * Returns the display title for this GUI type, using the stylized name
     * from the helpful info.
     *
     * @return the GUI title
     */
    public String getTitle() {
        return this.getHelpfulInfo().getStylizedName();
    }

    /**
     * Creates a new HelpfulGuiType from the given helpful info.
     *
     * @param helpfulInfo the helpful info to derive the GUI type from
     * @return a new HelpfulGuiType instance
     */
    public static HelpfulGuiType of(HelpfulInfo helpfulInfo) {
        return new HelpfulGuiType(helpfulInfo);
    }

    /**
     * Creates a new HelpfulGuiType from the given helpful instance's info.
     *
     * @param helpful the helpful instance whose info will be used
     * @return a new HelpfulGuiType instance
     */
    public static HelpfulGuiType of(Helpful helpful) {
        return new HelpfulGuiType(helpful.getInfo());
    }

    /**
     * Constructs a new HelpfulGuiType with the specified helpful info.
     *
     * @param helpfulInfo the helpful info containing identifier and display names
     */
    public HelpfulGuiType(HelpfulInfo helpfulInfo) {
        this.helpfulInfo = helpfulInfo;
    }
}
