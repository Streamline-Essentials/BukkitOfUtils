package host.plas.bou.gui.screens.blocks;

import host.plas.bou.gui.GuiType;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.ScreenManager;
import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.gui.screens.events.BlockCloseEvent;
import host.plas.bou.gui.screens.events.BlockOpenEvent;
import host.plas.bou.gui.screens.events.BlockRedrawEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import gg.drak.thebase.objects.Identifiable;

@Getter @Setter
public abstract class ScreenBlock implements Identifiable {
    private GuiType type;
    private Location location;

    @Override
    public String getIdentifier() {
        return type.name().toLowerCase();
    }

    public ScreenBlock(GuiType type, Location location) {
        this.type = type;
        this.location = location;
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public void onRightClick(Player player) {
        BlockOpenEvent event = new BlockOpenEvent(player, this).fire();

        if (! event.isCancelled()) onOpen(event);
    }

    public void onClose(Player player) {
        BlockCloseEvent event = new BlockCloseEvent(player, this).fire();
        if (! event.isCancelled()) onClose(event);
    }

    public void onRedraw() {
        BlockRedrawEvent event = new BlockRedrawEvent(this).fire();
        if (! event.isCancelled()) onRedraw(event);
    }

    public void onOpen(BlockOpenEvent event) {
        Player player = event.getPlayer();

        if (ScreenManager.hasScreen(player) && ! event.isOverride()) {
            ScreenManager.getScreen(player).ifPresent(ScreenInstance::open);
            return;
        }

        buildScreen(event).open();
    }

    public void onClose(BlockCloseEvent event) {
        Player player = event.getPlayer();

        ScreenManager.removeScreen(player);
    }

    public abstract InventorySheet buildInventorySheet(Player player, ScreenBlock block);

    public abstract String buildTitle(Player player, ScreenBlock block);

    public ScreenInstance buildScreen(BlockOpenEvent event) {
        Player player = event.getPlayer();
        ScreenBlock block = event.getScreenBlock();

        InventorySheet inventorySheet = buildInventorySheet(player, block);

        ScreenInstance instance = new ScreenInstance(player, getType(), inventorySheet);
        instance.setBlock(block);

        instance.setTitle(buildTitle(player, block));

        return instance;
    }

    public void onRedraw(BlockRedrawEvent event) {
        ScreenBlock block = event.getScreenBlock();
        if (! block.getIdentifier().equals(getIdentifier())) return; // ensure this block

        ScreenManager.getPlayersOf(block).forEach(screenInstance -> {
            screenInstance.redraw(); // re-builds and re-shows the inventory without closing it
            screenInstance.setBlock(this); // for redundancy
        });
    }

    public void redraw() {
        BlockRedrawEvent event = new BlockRedrawEvent(this).fire();
    }
}
