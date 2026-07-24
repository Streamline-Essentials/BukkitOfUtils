package host.plas.bou.onemenu;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.firestring.FireStringManager;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Screen backed by a {@link YamlGuiDefinition}.
 */
public class YamlGuiMenu extends ScreenInstance {
    private final YamlGuiDefinition definition;

    public YamlGuiMenu(@NotNull Player player, @NotNull YamlGuiDefinition definition) {
        super(player, new SimpleGuiType("YAML_" + definition.getFileName(), definition.getTitle()),
                buildSheet(definition), true);
        this.definition = definition;
        updateTitle(MessageUtils.codedString(definition.getTitle()));
    }

    public static void open(@NotNull Player player, @NotNull YamlGuiDefinition definition) {
        new YamlGuiMenu(player, definition).open();
    }

    private static InventorySheet buildSheet(YamlGuiDefinition definition) {
        InventorySheet sheet = InventorySheet.empty(definition.getSize());
        YamlGuiDefinition.YamlGuiActionHandler handler = YamlGuiMenu::runActions;
        for (Map.Entry<Integer, YamlGuiDefinition.YamlGuiItem> entry : definition.getItems().entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= definition.getSize()) continue;
            sheet.setIcon(slot, entry.getValue().toIcon(handler));
        }
        return sheet;
    }

    public static void runActions(Player player, List<String> actions) {
        if (player == null || actions == null) return;
        for (String raw : actions) {
            if (raw == null || raw.isBlank()) continue;
            String action = raw.trim();
            String lower = action.toLowerCase(Locale.ROOT);

            if (lower.equals("close")) {
                player.closeInventory();
                continue;
            }
            if (lower.startsWith("message:")) {
                MessageUtils.sendMessage(player, action.substring("message:".length()));
                continue;
            }
            if (lower.startsWith("player-command:")) {
                String cmd = action.substring("player-command:".length()).trim();
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                player.performCommand(cmd.replace("%player%", player.getName()));
                continue;
            }
            if (lower.startsWith("console-command:")) {
                String cmd = action.substring("console-command:".length()).trim();
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        cmd.replace("%player%", player.getName()));
                continue;
            }
            if (lower.startsWith("open-gui:")) {
                String file = action.substring("open-gui:".length()).trim();
                OneMenuManager.openGui(player, file);
                continue;
            }
            if (lower.startsWith("firestring:")) {
                String fs = action.substring("firestring:".length()).trim();
                FireStringManager.fire(fs.replace("%player%", player.getName()));
                continue;
            }
            BukkitOfUtils.getInstance().logWarning("Unknown OneMenu action: " + action);
        }
    }

    public YamlGuiDefinition getDefinition() {
        return definition;
    }
}
