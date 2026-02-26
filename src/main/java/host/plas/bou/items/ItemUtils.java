package host.plas.bou.items;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.compat.papi.PAPICompat;
import host.plas.bou.serialization.items.ItemStackSerializer;
import host.plas.bou.utils.ColorUtils;
import host.plas.bou.utils.EntityUtils;
import host.plas.bou.utils.PluginUtils;
import host.plas.bou.utils.VersionTool;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

public class ItemUtils {
    public static void registerRecipe(CraftingConfig config) {
        try {
            Bukkit.getServer().removeRecipe(PluginUtils.getPluginKey(BukkitOfUtils.getInstance(), config.getIdentifier()));
        } catch (Exception e) {
            // do nothing
        }

        Bukkit.getServer().addRecipe(getRecipe(config));
    }

    public static Recipe getRecipe(CraftingConfig config) {
        ShapedRecipe recipe = new ShapedRecipe(PluginUtils.getPluginKey(BukkitOfUtils.getInstance(), config.getIdentifier()), getCraftingResult(config));
        recipe.shape(config.getLine1(), config.getLine2(), config.getLine3());

        for (String key : config.getIngredients().keySet()) {
            recipe.setIngredient(key.charAt(0), getItemAbs(config.getIngredients().get(key)).getType());
        }

        return recipe;
    }

    public static ItemStack getCraftingResult(CraftingConfig config) {
        return getItemAbs(config.getResult());
    }

    public static ItemStack getItemAbs(String nbt) {
        return getItem(nbt).orElse(new ItemStack(Material.AIR));
    }

    public static Optional<ItemStack> getItem(String nbt) {
        try {
            return Optional.ofNullable(VersionTool.getBukkitItemStackFromJsonString(nbt));
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get item from NBT: ", e);
            return Optional.empty();
        }
    }

    public static String getItemNBT(ItemStack item) {
        try {
            return VersionTool.getJsonStringFromBukkitItemStack(item);
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get NBT from item: ", e);
            return "{}";
        }
    }

    @Getter @Setter
    private static ItemStackSerializer stackSerializer = new ItemStackSerializer();

    public static Optional<ItemStack> getItemStrict(String serialized) {
        try {
            ItemStack stack = getStackSerializer().fromString(serialized);
            return Optional.ofNullable(stack);
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get item from NBT: ", e);
            return Optional.empty();
        }
    }

    public static String getItemNBTStrict(ItemStack item) {
        try {
            return getStackSerializer().toString(item);
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get NBT from item: ", e);
            return "{}";
        }
    }

    public static boolean isItemEqual(ItemStack item1, ItemStack item2) {
        return getItemNBT(item1).equals(getItemNBT(item2));
    }

    public static boolean isNothingItem(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR;
    }

    public static ConcurrentSkipListMap<Integer, String> loreToMap(String... loreLines) {
        ConcurrentSkipListMap<Integer, String> lore = new ConcurrentSkipListMap<>();

        for (int i = 0; i < loreLines.length; i++) {
            lore.put(i, loreLines[i]);
        }

        return lore;
    }

    public static List<String> mapToLore(ConcurrentSkipListMap<Integer, String> loreMap) {
        return new ArrayList<>(loreMap.values());
    }

    public static String compute(OfflinePlayer player, String input) {
        input = PAPICompat.replace(player, input);
        input = ColorUtils.colorizeHard(input);

        return input;
    }

    public static String compute(String input) {
        return compute(EntityUtils.getDummyOfflinePlayer(), input);
    }

    public static ItemStack make(OfflinePlayer player, Material material, String displayName, boolean format, Collection<String> loreLines) {
        return make(player, material, displayName, format, loreLines.toArray(new String[0]));
    }

    public static ItemStack make(OfflinePlayer player, String material, String displayName, boolean format, Collection<String> loreLines) {
        return make(player, material, displayName, format, loreLines.toArray(new String[0]));
    }

    public static ItemStack make(OfflinePlayer player, Material material, String displayName, Collection<String> loreLines) {
        return make(player, material, displayName, true, loreLines.toArray(new String[0]));
    }

    public static ItemStack make(OfflinePlayer player, String material, String displayName, Collection<String> loreLines) {
        return make(player, material, displayName, true, loreLines.toArray(new String[0]));
    }

    public static ItemStack make(OfflinePlayer player, Material material, String displayName, boolean format, String... loreLines) {
        ItemStack item = new ItemStack(material);

        ConcurrentSkipListMap<Integer, String> lore = loreToMap(loreLines);
        lore.forEach((index, line) -> {
            if (format) line = compute(player, line);

            lore.put(index, line);
        });

        List<String> loreList = mapToLore(lore);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(compute(player, displayName));
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack make(OfflinePlayer player, String material, String displayName, boolean format, String... loreLines) {
        return make(player, material, displayName, format, loreLines);
    }

    public static ItemStack make(OfflinePlayer player, Material material, String displayName, String... loreLines) {
        return make(player, material, displayName, true, loreLines);
    }

    public static ItemStack make(OfflinePlayer player, String material, String displayName, String... loreLines) {
        return make(player, material, displayName, true, loreLines);
    }

    public static ItemStack make(Material material, String displayName, Collection<String> loreLines) {
        return make(material, displayName, loreLines.toArray(new String[0]));
    }

    public static ItemStack make(String material, String displayName, Collection<String> loreLines) {
        return make(material, displayName, loreLines.toArray(new String[0]));
    }

    public static ItemStack make(Material material, String displayName, String... loreLines) {
        ItemStack item = new ItemStack(material);

        ConcurrentSkipListMap<Integer, String> lore = loreToMap(loreLines);
        lore.forEach((index, line) -> {
            line = compute(line);

            lore.put(index, line);
        });

        List<String> loreList = mapToLore(lore);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(compute(displayName));
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack make(String material, String displayName, String... loreLines) {
        return make(getMaterial(material).orElse(Material.AIR), displayName, loreLines);
    }

    public static Optional<Material> getMaterial(String material) {
        try {
            return Optional.of(Material.valueOf(material.toUpperCase()));
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get material: ", e);
            return Optional.empty();
        }
    }

    public static void setTag(ItemStack stack, JavaPlugin plugin, String key, String value) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(PluginUtils.getPluginKey(plugin, key), PersistentDataType.STRING, value);
            stack.setItemMeta(meta);
        }
    }

    public static Optional<String> getTag(ItemStack stack, JavaPlugin plugin, String key) {
        if (stack == null) return Optional.empty();

        ItemMeta meta = stack.getItemMeta();

        String value = null;
        if (meta != null) {
            if (meta.getPersistentDataContainer().has(PluginUtils.getPluginKey(plugin, key), PersistentDataType.STRING))
                value = meta.getPersistentDataContainer().get(PluginUtils.getPluginKey(plugin, key), PersistentDataType.STRING);
        }

        return Optional.ofNullable(value);
    }
}
