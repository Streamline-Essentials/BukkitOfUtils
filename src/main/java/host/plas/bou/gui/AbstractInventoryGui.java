package host.plas.bou.gui;

import host.plas.bou.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight inventory GUI base: black border, colored corner panes, air interior,
 * and shared slot-key binding. Companion API alongside Oblivate {@link host.plas.bou.gui.screens.ScreenInstance}.
 */
public abstract class AbstractInventoryGui implements InventoryHolder {
    protected final Player player;
    protected final GuiConfig config;
    protected final CornerColor cornerColor;
    protected Inventory inventory;
    protected final Map<Integer, String> slotKeys = new HashMap<>();

    protected AbstractInventoryGui(Player player, CornerColor cornerColor) {
        this(GuiConfig.builder(player).cornerColor(cornerColor).build());
    }

    protected AbstractInventoryGui(GuiConfig config) {
        this.player = config.player();
        this.config = config;
        this.cornerColor = config.cornerColor() == null ? CornerColor.YELLOW : config.cornerColor();
    }

    protected ItemStack[] beginShell(int size, String title) {
        this.slotKeys.clear();
        this.inventory = Bukkit.createInventory(this, size, ColorUtils.colorizeHard(title));
        return GuiLayout.createShell(size, this.cornerColor);
    }

    protected ItemStack[] beginShell(int size, String title, CornerColor cornerColor) {
        this.slotKeys.clear();
        this.inventory = Bukkit.createInventory(this, size, ColorUtils.colorizeHard(title));
        return GuiLayout.createShell(size, cornerColor == null ? this.cornerColor : cornerColor);
    }

    protected void finishAndOpen(ItemStack[] contents) {
        this.inventory.setContents(contents);
        this.player.openInventory(this.inventory);
    }

    protected int resolveBackSlot(int size) {
        return this.config.resolveBackSlot(size);
    }

    protected void placeBalanceHeader(ItemStack[] contents, String formattedBalance) {
        String title = this.config.balanceText();
        if (title == null || title.isBlank()) {
            contents[GuiLayout.defaultBalanceSlot(contents.length)] = GuiItems.balanceButton(formattedBalance);
        } else {
            contents[GuiLayout.defaultBalanceSlot(contents.length)] = GuiItems.balanceButton(title, formattedBalance);
        }
    }

    protected void bindSlot(int slot, String key) {
        this.slotKeys.put(slot, key);
    }

    public String getKeyAtSlot(int slot) {
        return this.slotKeys.get(slot);
    }

    public int getBackSlot() {
        return this.inventory == null
                ? GuiLayout.defaultBackSlot(GuiLayout.SIZE_LARGE)
                : this.resolveBackSlot(this.inventory.getSize());
    }

    public Player getPlayer() {
        return this.player;
    }

    public GuiConfig getConfig() {
        return this.config;
    }

    public CornerColor getCornerColor() {
        return this.cornerColor;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
