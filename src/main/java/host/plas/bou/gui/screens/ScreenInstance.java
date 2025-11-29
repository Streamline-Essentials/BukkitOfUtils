package host.plas.bou.gui.screens;

import host.plas.bou.gui.GuiType;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.screens.blocks.ScreenBlock;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.ColorUtils;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Gui;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import gg.drak.thebase.objects.Identified;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

@Getter @Setter
public class ScreenInstance extends Gui implements Identified {
    private final String identifier;
    private GuiType type;
    private Optional<ScreenBlock> screenBlock;
    private InventorySheet inventorySheet;
    private boolean noPlace;

    public ScreenInstance(@NotNull Player player, GuiType type, InventorySheet inventorySheet, boolean noPlace) {
        super(player, type.name(), ColorUtils.colorizeHard(getTitleByType(type)), inventorySheet.getRows());

        this.identifier = player.getUniqueId().toString();

        this.type = type;
        this.screenBlock = Optional.empty();
        this.inventorySheet = inventorySheet;
        this.noPlace = noPlace;
    }

    public ScreenInstance(@NotNull Player player, GuiType type, InventorySheet inventorySheet) {
        this(player, type, inventorySheet, true);
    }

    public void setBlock(ScreenBlock block) {
        this.screenBlock = Optional.of(block);
    }

    public static String getTitleByType(GuiType type) {
        return type.getTitle();
    }

    public void updateSize(int size) {
        super.setSize(size);
    }

    public void updateTitle(String title) {
        super.setTitle(title);
    }

    public void build(InventorySheet sheet) {
        sheet.getSlots().forEach(s -> {
            addItem(s.getIndex(), s.getIcon());
        });
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        super.onClose(event);

        getScreenBlock().ifPresent(block -> block.onClose((Player) event.getPlayer()));
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        build(inventorySheet);

        ScreenManager.setScreen(player, this);
    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (! (event.getWhoClicked() instanceof Player)) return false;
        Player p = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        Inventory clickedInventory = event.getClickedInventory();
        Inventory playerInventory = p.getInventory();
        if (clickedInventory == null || playerInventory == null) return false;

        boolean isPlace = false;
        InventoryAction action = event.getAction();

        if (clickedInventory.equals(playerInventory)) {
            if (
                    action == InventoryAction.MOVE_TO_OTHER_INVENTORY
            ) {
                isPlace = true;
            }
        } else {

            if (
                    action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ONE_CURSOR ||
                            action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE ||
                            action == InventoryAction.PLACE_SOME
            ) {
                isPlace = true;
            }

            if (!isPlace) {
                if (cursor.getType() != Material.AIR) {
                    if (action == InventoryAction.SWAP_WITH_CURSOR) {
                        isPlace = true;
                    }
                }
            }
        }

        if (isPlace && noPlace) {
            event.setCancelled(true);
            return false; // No force uncancel as we are cancelling the event
        }

        return furtherClick(event);
    }

    public boolean furtherClick(InventoryClickEvent event) {
        // do more stuff
        return false;
    }

    public void close() {
        player.closeInventory();
    }

    public void redraw() {
        redraw(true);
    }

    public void redraw(boolean reshow) {
        build(inventorySheet);

        if (reshow) {
            reshow();
        }
    }

    public void reshow() {
        if (! TaskManager.isThreadSync()) {
            TaskManager.runTask(player, this::reshow);
            return;
        }

        close();
        open();
    }

    public ConcurrentSkipListMap<String, HumanEntity> getViewers() {
        ConcurrentSkipListMap<String, HumanEntity> map = new ConcurrentSkipListMap<>();
        List<HumanEntity> viewers = new ArrayList<>(getInventory().getViewers());

        viewers.forEach(v -> {
            if (v instanceof Player) {
                map.put(v.getUniqueId().toString(), v);
            }
        });

        return map;
    }
}
