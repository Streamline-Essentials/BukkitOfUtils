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
     * The standard black border pane used to frame shell GUIs.
     *
     * <p>Returns a finished stack rather than a {@link Material} because on pre-1.13 servers
     * the color lives in the stack's data value, not in the material — every pane color there
     * shares the single {@code STAINED_GLASS_PANE} material. A bare material cannot carry that
     * information, so callers that need a border pane must use this.</p>
     *
     * @return a black (or legacy-equivalent) filler pane
     */
    public static ItemStack borderPane() {
        return cornerPane(CornerColor.BLACK);
    }

    /**
     * Builds a filler pane in the given accent color, correct on both modern and legacy servers.
     *
     * @param color the accent color; {@link CornerColor#YELLOW} when {@code null}
     * @return the colored filler pane
     */
    public static ItemStack cornerPane(CornerColor color) {
        CornerColor resolved = color == null ? CornerColor.YELLOW : color;

        ItemStack item = filler(resolved.paneMaterial());
        // On pre-1.13 every pane color shares one material and is selected by data value.
        return LegacySupport.applyLegacyData(item, resolved.legacyData());
    }

    public static ItemStack filler(Material material) {
        ItemStack item = new ItemStack(material == null
                ? LegacySupport.material(Material.AIR, "BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE")
                : material);
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

    /**
     * Builds a button and applies a legacy data value, for items whose color on pre-1.13
     * servers is carried by the data value rather than the material.
     *
     * @param material    the button material
     * @param name        the display name
     * @param lore        the lore lines
     * @param legacyData  the legacy data value to apply on legacy servers
     * @return the built button
     */
    public static ItemStack button(Material material, String name, List<String> lore, int legacyData) {
        return LegacySupport.applyLegacyData(button(material, name, lore), legacyData);
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
        // Built from a colored pane so the lime tint survives on pre-1.13 servers,
        // where the color is a data value rather than part of the material.
        ItemStack base = cornerPane(CornerColor.LIME);
        return button(base.getType(), title, lore, base.getDurability());
    }
}
