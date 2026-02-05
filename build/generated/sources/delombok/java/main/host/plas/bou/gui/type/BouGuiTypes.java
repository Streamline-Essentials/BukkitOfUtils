package host.plas.bou.gui.type;

import host.plas.bou.gui.GuiType;

public enum BouGuiTypes implements GuiType {
    TASK_MENU("Task Menu");
    private final String title;

    BouGuiTypes(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
