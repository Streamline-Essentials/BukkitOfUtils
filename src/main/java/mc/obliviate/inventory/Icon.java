package mc.obliviate.inventory;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A clickable inventory item with a fluent builder API.
 *
 * <p>BOU's own implementation of the OblivateInvs API. The published
 * {@code mc.obliviate:core} artifact is compiled to class file version 65.0 (Java 21) and
 * cannot load on the Java 11 runtimes BOU supports, so the API is reimplemented here at
 * the original package names — every dependent plugin keeps compiling unchanged.</p>
 *
 * <p>Every Bukkit call in this class exists in the 1.8 API, so icons work unchanged on
 * legacy servers.</p>
 */
public class Icon implements GuiIcon {
    private final ItemStack item;
    private Consumer<InventoryClickEvent> clickAction = e -> {};
    private Consumer<InventoryDragEvent> dragAction = e -> {};

    /**
     * @param item the item to display; used directly, not copied
     */
    public Icon(ItemStack item) {
        this.item = item;
    }

    /**
     * @param material the material to display
     */
    public Icon(Material material) {
        this(new ItemStack(material));
    }

    /**
     * Sets the item's damage value. On pre-1.13 servers this doubles as the colour/variant
     * selector for wool, dyes, panes and similar blocks.
     *
     * @param durability the damage value
     * @return this icon
     */
    public Icon setDurability(short durability) {
        item.setDurability(durability);
        return this;
    }

    /**
     * @param durability the damage value
     * @return this icon
     * @see #setDurability(short)
     */
    public Icon setDurability(int durability) {
        return setDurability((short) durability);
    }

    /**
     * @param name the display name, colour codes already translated
     * @return this icon
     */
    public Icon setName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * @param lore the lore lines, replacing any existing lore
     * @return this icon
     */
    public Icon setLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * @param lore the lore lines, replacing any existing lore
     * @return this icon
     */
    public Icon setLore(String... lore) {
        return setLore(new ArrayList<>(Arrays.asList(lore)));
    }

    /**
     * @param lore lines to add after the existing lore
     * @return this icon
     */
    public Icon appendLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        List<String> lines = meta.getLore();
        if (lines == null) lines = new ArrayList<>();
        lines.addAll(lore);

        meta.setLore(lines);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * @param lore lines to add after the existing lore
     * @return this icon
     */
    public Icon appendLore(String... lore) {
        return appendLore(new ArrayList<>(Arrays.asList(lore)));
    }

    /**
     * @param index the position to insert at
     * @param lore  the lines to insert
     * @return this icon
     */
    public Icon insertLore(int index, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        List<String> lines = meta.getLore();
        if (lines == null) lines = new ArrayList<>();
        lines.addAll(Math.min(index, lines.size()), lore);

        meta.setLore(lines);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * @param index the position to insert at
     * @param lore  the lines to insert
     * @return this icon
     */
    public Icon insertLore(int index, String... lore) {
        return insertLore(index, new ArrayList<>(Arrays.asList(lore)));
    }

    /**
     * @param amount the stack size
     * @return this icon
     */
    public Icon setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * @param flags the flags to hide
     * @return this icon
     */
    public Icon hideFlags(ItemFlag... flags) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Hides every item flag the running server knows about.
     *
     * @return this icon
     */
    public Icon hideFlags() {
        return hideFlags(ItemFlag.values());
    }

    /**
     * @param enchantment the enchantment to add at level 1
     * @return this icon
     */
    public Icon enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    /**
     * @param enchantment the enchantment to add
     * @param level       the level, which may exceed the vanilla maximum
     * @return this icon
     */
    public Icon enchant(Enchantment enchantment, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        } else {
            item.addUnsafeEnchantment(enchantment, level);
        }
        return this;
    }

    /**
     * @param enchantments the enchantments to add, keyed by level
     * @return this icon
     */
    public Icon enchant(Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            enchant(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Consumer<InventoryClickEvent> getClickAction() {
        return clickAction;
    }

    /**
     * @param clickAction the handler to run when the icon is clicked
     * @return this icon
     */
    public Icon onClick(Consumer<InventoryClickEvent> clickAction) {
        this.clickAction = clickAction == null ? e -> {} : clickAction;
        return this;
    }

    @Override
    public Consumer<InventoryDragEvent> getDragAction() {
        return dragAction;
    }

    /**
     * @param dragAction the handler to run when the icon is dragged
     * @return this icon
     */
    public Icon onDrag(Consumer<InventoryDragEvent> dragAction) {
        this.dragAction = dragAction == null ? e -> {} : dragAction;
        return this;
    }

    @Override
    public ItemStack getItem() {
        return item;
    }
}
