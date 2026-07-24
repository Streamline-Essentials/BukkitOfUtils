package host.plas.bou.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Editor-style GUI with standard navigation slots and list pagination helpers.
 */
public abstract class EditorInventoryGui extends AbstractInventoryGui {
    protected EditorInventoryGui(Player player, CornerColor cornerColor) {
        super(player, cornerColor);
    }

    protected EditorInventoryGui(GuiConfig config) {
        super(config);
    }

    protected static int perPage(int size) {
        return GuiLayout.listContentSlots(size).length;
    }

    protected void placeReturnButton(ItemStack[] contents) {
        int slot = this.resolveBackSlot(contents.length);
        contents[slot] = GuiItems.returnButton();
    }

    protected void placeReturnButton(ItemStack[] contents, String slotKey) {
        int slot = this.resolveBackSlot(contents.length);
        contents[slot] = GuiItems.returnButton();
        this.bindSlot(slot, slotKey);
    }

    protected void placeHelpButton(ItemStack[] contents, String... lines) {
        contents[GuiLayout.HELP_SLOT] = GuiItems.helpButton(lines);
    }

    protected void placeCreateButton(ItemStack[] contents, String title, String slotKey, String... lines) {
        contents[GuiLayout.CREATE_SLOT] = GuiItems.createButton(title, lines);
        this.bindSlot(GuiLayout.CREATE_SLOT, slotKey);
    }

    protected void placePagination(ItemStack[] contents, int page, int totalEntries) {
        this.placePagination(contents, page, totalEntries, "__page");
    }

    protected void placePagination(ItemStack[] contents, int page, int totalEntries, String prefix) {
        int perPage = GuiLayout.listContentSlots(contents.length).length;
        int totalPages = Math.max(1, (int) Math.ceil(totalEntries / (double) perPage));
        int backSlot = this.resolveBackSlot(contents.length);
        int prevSlot = GuiLayout.pagePrevSlot(backSlot);
        int nextSlot = GuiLayout.pageNextSlot(backSlot);
        if (page > 0) {
            contents[prevSlot] = GuiItems.pagePreviousButton(page);
            this.bindSlot(prevSlot, prefix + "prev");
        }
        if (page + 1 < totalPages) {
            contents[nextSlot] = GuiItems.pageNextButton(page + 2);
            this.bindSlot(nextSlot, prefix + "next");
        }
    }
}
