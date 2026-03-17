package host.plas.bou.gui.type;

import host.plas.bou.gui.GuiType;
import lombok.Getter;

/**
 * Enum defining default GUI types with fallback titles.
 */
@Getter
public enum DefaultTypes implements GuiType {
    /** The default GUI type with a fallback title. */
    DEFAULT("Default GUI Title"),
    ;

    private final String title;

    /**
     * Constructs a new DefaultTypes entry with the given display title.
     *
     * @param title the display title for this GUI type
     */
    DefaultTypes(String title) {
        this.title = title;
    }
}
