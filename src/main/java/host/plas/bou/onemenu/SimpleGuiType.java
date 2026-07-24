package host.plas.bou.onemenu;

import host.plas.bou.gui.GuiType;
import lombok.Getter;

/**
 * Lightweight {@link GuiType} with a custom id and title (for YAML GUIs).
 */
@Getter
public class SimpleGuiType implements GuiType {
    private final String name;
    private final String title;

    public SimpleGuiType(String name, String title) {
        this.name = name == null ? "ONE_MENU" : name;
        this.title = title == null ? "Menu" : title;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
