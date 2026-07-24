package host.plas.bou.gui;

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

    public static ItemStack cornerPane(CornerColor color) {
        return filler(color == null ? CornerColor.YELLOW.paneMaterial() : color.paneMaterial());
    }

    public static ItemStack filler(Material material) {
        ItemStack item = new ItemStack(material == null ? Material.BLACK_STAINED_GLASS_PANE : material);
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
                Material.OAK_DOOR,
                "#FFED6A&lBack",
                "#bdc8c9Return to the previous menu.",
                "",
                "#bdc8c9Click: #bbff6aBack"
        );
    }

    public static ItemStack pagePreviousButton(int displayPage) {
        return button(
                Material.OAK_BUTTON,
                "#FFED6APrevious Page",
                "#bdc8c9Page " + displayPage
        );
    }

    public static ItemStack pageNextButton(int displayPage) {
        return button(
                Material.OAK_BUTTON,
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
        return button(Material.LIME_STAINED_GLASS_PANE, title, lore);
    }
}
