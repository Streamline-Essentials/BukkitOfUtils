package host.plas.bou.gui.menus;

import host.plas.bou.BetterPlugin;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.icons.BasicIcon;
import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.gui.type.BouGuiTypes;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.MessageUtils;
import host.plas.bou.utils.PluginUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Lists registered BetterPlugins; clicking an icon opens {@link BouPluginInfoMenu}.
 * Opened via {@code /boup menu}.
 */
public class BouPluginsMenu extends ScreenInstance {

    public BouPluginsMenu(@NotNull Player player) {
        super(player, BouGuiTypes.BOU_PLUGINS_LIST, buildSheet(), true);
        updateTitle(MessageUtils.codedString("&8BOU &7| &bPlugins"));
    }

    public static void open(@NotNull Player player) {
        new BouPluginsMenu(player).open();
    }

    private static InventorySheet buildSheet() {
        ConcurrentSkipListSet<BetterPlugin> plugins = PluginUtils.getAllBOUPlugins();
        List<BetterPlugin> ordered = new ArrayList<>(plugins);
        int count = Math.max(1, ordered.size());
        int rows = Math.min(6, Math.max(1, (count + 8) / 9));
        InventorySheet sheet = InventorySheet.empty(rows * 9);

        int slot = 0;
        for (BetterPlugin plugin : ordered) {
            if (slot >= sheet.getSize()) {
                break;
            }
            BetterPlugin target = plugin;
            ItemStack stack = ItemUtils.make(
                    plugin.isEnabled() ? Material.NETHER_STAR : Material.GRAY_DYE,
                    plugin.getColorizedIdentifier(),
                    "&7v&b" + plugin.getDescription().getVersion(),
                    "&7Status: " + (plugin.isEnabled() ? "&aEnabled" : "&cDisabled"),
                    "&7Uptime: &b" + plugin.getFormattedUptime(),
                    "",
                    "&eClick to open plugin menu"
            );
            sheet.setIcon(slot++, new BasicIcon(stack).onClick(e -> {
                if (!(e.getWhoClicked() instanceof Player)) {
                    return;
                }
                BouPluginInfoMenu.open((Player) e.getWhoClicked(), target);
            }));
        }

        return sheet;
    }
}
