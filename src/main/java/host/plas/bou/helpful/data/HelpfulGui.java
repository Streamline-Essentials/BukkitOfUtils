package host.plas.bou.helpful.data;

import host.plas.bou.gui.GuiHelper;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.screens.ScreenInstance;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * GUI wrapper for a {@link Helpful} instance, providing inventory-based
 * display of help content to players.
 */
@Setter
@Getter
public class HelpfulGui implements HelpfulIdentified {
    /**
     * The helpful data instance backing this GUI.
     *
     * @param helpful the helpful data to set
     * @return the helpful data
     */
    private Helpful helpful;
    /**
     * The GUI type configuration controlling layout and behavior.
     *
     * @param type the GUI type to set
     * @return the GUI type
     */
    private HelpfulGuiType type;

    /**
     * Retrieves the helpful info from the underlying helpful instance.
     *
     * @return the helpful info metadata
     */
    public HelpfulInfo getInfo() {
        return this.helpful.getInfo();
    }

    /**
     * Sets the helpful info on the underlying helpful instance.
     *
     * @param info the helpful info metadata to set
     */
    public void setInfo(HelpfulInfo info) {
        this.helpful.setInfo(info);
    }

    /**
     * Constructs a new HelpfulGui with the specified helpful data and GUI type.
     *
     * @param helpful the helpful data to display
     * @param type    the GUI type configuration
     */
    public HelpfulGui(Helpful helpful, HelpfulGuiType type) {
        this.helpful = helpful;
        this.type = type;
    }

    /**
     * Constructs a new HelpfulGui with the specified helpful data, using a default GUI type.
     *
     * @param helpful the helpful data to display
     */
    public HelpfulGui(Helpful helpful) {
        this(helpful, HelpfulGuiType.of(helpful));
    }

    /**
     * Creates a new screen instance for displaying this GUI to a player.
     *
     * @param player the player who will view the screen
     * @return a new {@link ScreenInstance} configured for this GUI
     */
    public ScreenInstance makeScreenInstance(Player player) {
        return new ScreenInstance(player, this.type, this.makeInventorySheet());
    }

    /**
     * Creates an inventory sheet for this GUI using a book icon in the middle slot.
     *
     * @return a new {@link InventorySheet} representing this GUI's layout
     */
    public InventorySheet makeInventorySheet() {
        return InventorySheet.of(this, GuiHelper.getMiddleSlot(2), Material.BOOK);
    }

    /**
     * Opens this GUI for the specified player.
     *
     * @param player the player to open the GUI for
     */
    public void open(Player player) {
        this.makeScreenInstance(player).open();
    }

    /**
     * Creates a new HelpfulGui with the specified helpful data and GUI type.
     *
     * @param helpful the helpful data to display
     * @param type    the GUI type configuration
     * @return a new HelpfulGui instance
     */
    public static HelpfulGui of(Helpful helpful, HelpfulGuiType type) {
        return new HelpfulGui(helpful, type);
    }

    /**
     * Creates a new HelpfulGui with the specified helpful data and a default GUI type.
     *
     * @param helpful the helpful data to display
     * @return a new HelpfulGui instance
     */
    public static HelpfulGui of(Helpful helpful) {
        return new HelpfulGui(helpful);
    }

}
