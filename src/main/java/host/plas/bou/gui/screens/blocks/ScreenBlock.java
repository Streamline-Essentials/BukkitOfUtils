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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import gg.drak.thebase.objects.Identifiable;

/**
 * Abstract representation of a world block that can open a GUI screen when interacted with.
 * Manages open, close, and redraw lifecycle events for associated screen instances.
 */
@Getter
@Setter
public abstract class ScreenBlock implements Identifiable {
    /**
     * The GUI type for screens opened by this block.
     *
     * @param type the GUI type to set
     * @return the GUI type
     */
    private GuiType type;

    /**
     * The world location of this screen block.
     *
     * @param location the location to set
     * @return the world location
     */
    private Location location;

    @Override
    public String getIdentifier() {
        String typeName = type == null ? "unknown" : type.name().toLowerCase();
        if (location == null) {
            return typeName;
        }
        World world = location.getWorld();
        String worldName = world == null ? "null" : world.getName();
        return typeName + "@" + worldName + ","
                + location.getBlockX() + ","
                + location.getBlockY() + ","
                + location.getBlockZ();
    }

    /**
     * Constructs a new ScreenBlock with the given GUI type and world location.
     *
     * @param type     the GUI type for screens opened by this block
     * @param location the world location of this block
     */
    public ScreenBlock(GuiType type, Location location) {
        this.type = type;
        this.location = location;
    }

    /**
     * Returns the Bukkit block at this screen block's location.
     *
     * @return the {@link Block} at the stored location
     */
    public Block getBlock() {
        return location.getBlock();
    }

    /**
     * Handles a player right-clicking this block by firing a {@link BlockOpenEvent}.
     *
     * @param player the player who right-clicked the block
     */
    public void onRightClick(Player player) {
        BlockOpenEvent event = new BlockOpenEvent(player, this).fire();
        if (!event.isCancelled()) onOpen(event);
    }

    /**
     * Handles a player closing the screen associated with this block by firing a {@link BlockCloseEvent}.
     *
     * @param player the player who closed the screen
     */
    public void onClose(Player player) {
        BlockCloseEvent event = new BlockCloseEvent(player, this).fire();
        if (!event.isCancelled()) onClose(event);
    }

    /**
     * Initiates a redraw of all screens associated with this block by firing a {@link BlockRedrawEvent}.
     * Handling is performed by the event listener to avoid double-processing.
     */
    public void onRedraw() {
        redraw();
    }

    /**
     * Opens or reopens the screen for the player based on the open event.
     * If the player already has a screen and override is not set, the existing screen is reopened.
     *
     * @param event the block open event
     */
    public void onOpen(BlockOpenEvent event) {
        Player player = event.getPlayer();

        if (ScreenManager.hasScreen(player) && !event.isOverride()) {
            ScreenManager.getScreen(player).ifPresent(ScreenInstance::open);
            return;
        }

        buildScreen(event).open();
    }

    /**
     * Handles the close event by removing the player's screen from the screen manager.
     *
     * @param event the block close event
     */
    public void onClose(BlockCloseEvent event) {
        ScreenManager.removeScreen(event.getPlayer());
    }

    /**
     * Builds the inventory sheet layout for a player viewing this screen block.
     *
     * @param player the player viewing the screen
     * @param block  the screen block being viewed
     * @return the constructed {@link InventorySheet}
     */
    public abstract InventorySheet buildInventorySheet(Player player, ScreenBlock block);

    /**
     * Builds the display title for a player viewing this screen block.
     *
     * @param player the player viewing the screen
     * @param block  the screen block being viewed
     * @return the title string
     */
    public abstract String buildTitle(Player player, ScreenBlock block);

    /**
     * Builds a complete screen instance from a block open event.
     *
     * @param event the block open event containing the player and block information
     * @return the constructed {@link ScreenInstance}
     */
    public ScreenInstance buildScreen(BlockOpenEvent event) {
        Player player = event.getPlayer();
        ScreenBlock block = event.getScreenBlock();

        InventorySheet inventorySheet = buildInventorySheet(player, block);

        ScreenInstance instance = new ScreenInstance(player, getType(), inventorySheet);
        instance.setBlock(block);
        instance.setTitle(buildTitle(player, block));

        return instance;
    }

    /**
     * Handles a redraw event by rebuilding all screen instances viewing this block.
     *
     * @param event the block redraw event
     */
    public void onRedraw(BlockRedrawEvent event) {
        ScreenBlock block = event.getScreenBlock();
        if (block == null || !block.getIdentifier().equals(getIdentifier())) return;

        ScreenManager.getPlayersOf(block).forEach(screenInstance -> {
            screenInstance.redraw();
            screenInstance.setBlock(this);
        });
    }

    /**
     * Triggers a redraw of this screen block by firing a {@link BlockRedrawEvent}.
     */
    public void redraw() {
        new BlockRedrawEvent(this).fire();
    }
}
