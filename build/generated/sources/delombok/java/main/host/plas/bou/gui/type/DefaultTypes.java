package host.plas.bou.gui.type;

import host.plas.bou.gui.GuiType;

public enum DefaultTypes implements GuiType {
    DEFAULT("Default GUI Title");
    private final String title;

    DefaultTypes(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
