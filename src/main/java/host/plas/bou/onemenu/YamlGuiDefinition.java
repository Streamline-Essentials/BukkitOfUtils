package host.plas.bou.onemenu;

import host.plas.bou.gui.icons.BasicIcon;
import host.plas.bou.items.ItemUtils;
import lombok.Getter;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed YAML GUI definition under {@code plugins/BukkitOfUtils/guis/}.
 */
@Getter
public class YamlGuiDefinition {
    private final String fileName;
    private final String title;
    private final int rows;
    private final Map<Integer, YamlGuiItem> items;

    public YamlGuiDefinition(String fileName, String title, int rows, Map<Integer, YamlGuiItem> items) {
        this.fileName = fileName;
        this.title = title;
        this.rows = Math.max(1, Math.min(6, rows));
        this.items = items == null ? Collections.emptyMap() : items;
    }

    public int getSize() {
        return rows * 9;
    }

    public static YamlGuiDefinition load(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String title = yaml.getString("title", "&8Menu");
        int rows = yaml.getInt("rows", 3);
        Map<Integer, YamlGuiItem> items = new HashMap<>();
        ConfigurationSection section = yaml.getConfigurationSection("items");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    continue;
                }
                ConfigurationSection itemSec = section.getConfigurationSection(key);
                if (itemSec == null) continue;
                items.put(slot, YamlGuiItem.from(itemSec));
            }
        }
        return new YamlGuiDefinition(file.getName(), title, rows, items);
    }

    @Getter
    public static class YamlGuiItem {
        private final Material material;
        private final String name;
        private final List<String> lore;
        private final List<String> leftClick;
        private final List<String> rightClick;

        public YamlGuiItem(Material material, String name, List<String> lore,
                           List<String> leftClick, List<String> rightClick) {
            this.material = material == null ? Material.STONE : material;
            this.name = name == null ? "" : name;
            this.lore = lore == null ? Collections.emptyList() : lore;
            this.leftClick = leftClick == null ? Collections.emptyList() : leftClick;
            this.rightClick = rightClick == null ? Collections.emptyList() : rightClick;
        }

        public static YamlGuiItem from(ConfigurationSection section) {
            Material material = BasicIcon.getMaterial(section.getString("material", "STONE"));
            String name = section.getString("name", "");
            List<String> lore = section.getStringList("lore");
            List<String> left = section.getStringList("left-click");
            if (left.isEmpty()) {
                left = section.getStringList("click");
            }
            List<String> right = section.getStringList("right-click");
            return new YamlGuiItem(material, name, new ArrayList<>(lore), new ArrayList<>(left), new ArrayList<>(right));
        }

        public ItemStack toItemStack() {
            return ItemUtils.make(material, name, lore);
        }

        public Icon toIcon(YamlGuiActionHandler handler) {
            return new BasicIcon(toItemStack()).onClick(event -> {
                boolean right = event.isRightClick();
                List<String> actions = right && !rightClick.isEmpty() ? rightClick : leftClick;
                handler.runActions(event.getWhoClicked() instanceof org.bukkit.entity.Player
                        ? (org.bukkit.entity.Player) event.getWhoClicked()
                        : null, actions);
            });
        }
    }

    @FunctionalInterface
    public interface YamlGuiActionHandler {
        void runActions(org.bukkit.entity.Player player, List<String> actions);
    }
}
