package host.plas.bou.gui.type;

import host.plas.bou.gui.GuiType;
import lombok.Getter;

/**
 * Enum defining built-in GUI types provided by BukkitOfUtils.
 */
@Getter
public enum BouGuiTypes implements GuiType {
    /** The task menu GUI type. */
    TASK_MENU("Task Menu"),
    ;

    private final String title;

    /**
     * Constructs a new BouGuiTypes entry with the given display title.
     *
     * @param title the display title for this GUI type
     */
    BouGuiTypes(String title) {
        this.title = title;
    }
}
