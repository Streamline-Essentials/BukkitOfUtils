package host.plas.bou.gui;

import host.plas.bou.compat.LegacySupport;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Standard inventory chrome items for shell GUIs (fillers, nav, help, create, balance).
 */
public final class GuiItems {
    private GuiItems() {
    }

    /**
     * Standard black border pane, resolved per server version.
     *
     * @return the black stained glass pane material, or a legacy/fallback equivalent
     */
    public static Material borderPaneMaterial() {
        return LegacySupport.material(Material.AIR, "BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
    }

    public static ItemStack cornerPane(CornerColor color) {
        CornerColor resolved = color == null ? CornerColor.YELLOW : color;

        ItemStack item = filler(resolved.paneMaterial());
        // On pre-1.13 every pane color shares one material and is selected by data value.
        return LegacySupport.applyLegacyData(item, resolved.legacyData());
    }

    public static ItemStack filler(Material material) {
        ItemStack item = new ItemStack(material == null ? borderPaneMaterial() : material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack button(Material material, String name, List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            return ItemUtils.make(material, name);
        }
        List<String> colored = new ArrayList<>(lore.size());
        for (String line : lore) {
            colored.add(ColorUtils.colorizeHard(line));
        }
        return ItemUtils.make(material, name, colored);
    }

    public static ItemStack button(Material material, String name, String... lore) {
        if (lore == null || lore.length == 0) {
            return button(material, name, Collections.emptyList());
        }
        return button(material, name, Arrays.asList(lore));
    }

    public static ItemStack returnButton() {
        return button(
                LegacySupport.material(Material.CHEST, "OAK_DOOR", "WOOD_DOOR", "WOODEN_DOOR"),
                "#FFED6A&lBack",
                "#bdc8c9Return to the previous menu.",
                "",
                "#bdc8c9Click: #bbff6aBack"
        );
    }

    public static ItemStack pagePreviousButton(int displayPage) {
        return button(
                LegacySupport.material(Material.STONE_BUTTON, "OAK_BUTTON", "WOOD_BUTTON"),
                "#FFED6APrevious Page",
                "#bdc8c9Page " + displayPage
        );
    }

    public static ItemStack pageNextButton(int displayPage) {
        return button(
                LegacySupport.material(Material.STONE_BUTTON, "OAK_BUTTON", "WOOD_BUTTON"),
                "#FFED6ANext Page",
                "#bdc8c9Page " + displayPage
        );
    }

    public static ItemStack balanceButton(String formattedBalance) {
        return balanceButton("#FFD700&lYour Balance", formattedBalance);
    }

    public static ItemStack balanceButton(String title, String formattedBalance) {
        return button(
                Material.GOLD_NUGGET,
                title,
                "#AAAAAABalance: #FFD700" + formattedBalance
        );
    }

    public static ItemStack helpButton(String... lines) {
        List<String> lore = new ArrayList<>();
        lore.add("#bdc8c9Drag items from your inventory");
        lore.add("#bdc8c9into editable slots to set them.");
        lore.add("");
        if (lines != null) {
            Collections.addAll(lore, lines);
        }
        return button(Material.BOOK, "#FFED6A&lHelp", lore);
    }

    public static ItemStack createButton(String title, String... lines) {
        List<String> lore = new ArrayList<>();
        if (lines != null) {
            Collections.addAll(lore, lines);
        }
        return button(LegacySupport.material(Material.PAPER, "LIME_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), title, lore);
    }
}
