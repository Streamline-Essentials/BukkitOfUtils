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

/**
 * Utility class for item stack creation, NBT serialization, recipe management,
 * material lookups, and persistent data tag operations.
 */
public class ItemUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ItemUtils() {
        // utility class
    }

    /**
     * Registers a crafting recipe on the server, removing any existing recipe with the same key first.
     *
     * @param config the crafting configuration to register
     */
    public static void registerRecipe(CraftingConfig config) {
        try {
            Bukkit.getServer().removeRecipe(PluginUtils.getPluginKey(BukkitOfUtils.getInstance(), config.getIdentifier()));
        } catch (Exception e) {
            // do nothing
        }

        Bukkit.getServer().addRecipe(getRecipe(config));
    }

    /**
     * Builds a Bukkit shaped recipe from a crafting configuration.
     *
     * @param config the crafting configuration
     * @return the constructed shaped recipe
     */
    public static Recipe getRecipe(CraftingConfig config) {
        ShapedRecipe recipe = new ShapedRecipe(PluginUtils.getPluginKey(BukkitOfUtils.getInstance(), config.getIdentifier()), getCraftingResult(config));
        recipe.shape(config.getLine1(), config.getLine2(), config.getLine3());

        for (String key : config.getIngredients().keySet()) {
            recipe.setIngredient(key.charAt(0), getItemAbs(config.getIngredients().get(key)).getType());
        }

        return recipe;
    }

    /**
     * Returns the result item stack for a crafting configuration.
     *
     * @param config the crafting configuration
     * @return the result item stack
     */
    public static ItemStack getCraftingResult(CraftingConfig config) {
        return getItemAbs(config.getResult());
    }

    /**
     * Returns an item stack from an NBT string, falling back to air if parsing fails.
     *
     * @param nbt the NBT JSON string
     * @return the parsed item stack, or an air item stack if parsing fails
     */
    public static ItemStack getItemAbs(String nbt) {
        return getItem(nbt).orElse(new ItemStack(Material.AIR));
    }

    /**
     * Parses an item stack from an NBT JSON string.
     *
     * @param nbt the NBT JSON string
     * @return an Optional containing the parsed item stack, or empty on failure
     */
    public static Optional<ItemStack> getItem(String nbt) {
        try {
            return Optional.ofNullable(VersionTool.getBukkitItemStackFromJsonString(nbt));
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get item from NBT: ", e);
            return Optional.empty();
        }
    }

    /**
     * Serializes an item stack to its NBT JSON string representation.
     *
     * @param item the item stack to serialize
     * @return the NBT JSON string, or "{}" on failure
     */
    public static String getItemNBT(ItemStack item) {
        try {
            return VersionTool.getJsonStringFromBukkitItemStack(item);
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get NBT from item: ", e);
            return "{}";
        }
    }

    /**
     * The serializer used for strict item stack serialization and deserialization.
     *
     * @param stackSerializer the item stack serializer to use
     * @return the current item stack serializer
     */
    @Getter @Setter
    private static ItemStackSerializer stackSerializer = new ItemStackSerializer();

    /**
     * Deserializes an item stack from a strict serialized string format.
     *
     * @param serialized the serialized item string
     * @return an Optional containing the deserialized item stack, or empty on failure
     */
    public static Optional<ItemStack> getItemStrict(String serialized) {
        try {
            ItemStack stack = getStackSerializer().fromString(serialized);
            return Optional.ofNullable(stack);
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get item from NBT: ", e);
            return Optional.empty();
        }
    }

    /**
     * Serializes an item stack to a strict string format using the stack serializer.
     *
     * @param item the item stack to serialize
     * @return the serialized string, or "{}" on failure
     */
    public static String getItemNBTStrict(ItemStack item) {
        try {
            return getStackSerializer().toString(item);
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get NBT from item: ", e);
            return "{}";
        }
    }

    /**
     * Checks whether two item stacks are equal by comparing their NBT representations.
     *
     * @param item1 the first item stack
     * @param item2 the second item stack
     * @return true if the NBT strings of both items are equal
     */
    public static boolean isItemEqual(ItemStack item1, ItemStack item2) {
        return getItemNBT(item1).equals(getItemNBT(item2));
    }

    /**
     * Checks whether an item stack is null or air.
     *
     * @param stack the item stack to check
     * @return true if the stack is null or has an air material type
     */
    public static boolean isNothingItem(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR;
    }

    /**
     * Converts an array of lore line strings into a sorted map keyed by line index.
     *
     * @param loreLines the lore lines to convert
     * @return a sorted map of line indices to lore strings
     */
    public static ConcurrentSkipListMap<Integer, String> loreToMap(String... loreLines) {
        ConcurrentSkipListMap<Integer, String> lore = new ConcurrentSkipListMap<>();

        for (int i = 0; i < loreLines.length; i++) {
            lore.put(i, loreLines[i]);
        }

        return lore;
    }

    /**
     * Converts a sorted lore map back into a list of strings.
     *
     * @param loreMap the sorted map of line indices to lore strings
     * @return a list of lore strings in order
     */
    public static List<String> mapToLore(ConcurrentSkipListMap<Integer, String> loreMap) {
        return new ArrayList<>(loreMap.values());
    }

    /**
     * Computes a string by applying PlaceholderAPI replacements and color formatting
     * for a specific player.
     *
     * @param player the player context for placeholder replacement
     * @param input  the input string to process
     * @return the processed string with placeholders replaced and colors applied
     */
    public static String compute(OfflinePlayer player, String input) {
        input = PAPICompat.replace(player, input);
        input = ColorUtils.colorizeHard(input);

        return input;
    }

    /**
     * Computes a string by applying PlaceholderAPI replacements and color formatting
     * using a dummy player context.
     *
     * @param input the input string to process
     * @return the processed string with placeholders replaced and colors applied
     */
    public static String compute(String input) {
        return compute(EntityUtils.getDummyOfflinePlayer(), input);
    }

    /**
     * Creates an item stack with a display name and lore, applying placeholder and color formatting.
     *
     * @param player      the player context for placeholder replacement
     * @param material    the material type for the item
     * @param displayName the display name for the item
     * @param format      whether to apply placeholder and color formatting
     * @param loreLines   the lore lines as a collection
     * @return the constructed item stack
     */
    public static ItemStack make(OfflinePlayer player, Material material, String displayName, boolean format, Collection<String> loreLines) {
        return make(player, material, displayName, format, loreLines.toArray(new String[0]));
    }

    /**
     * Creates an item stack with a display name and lore, applying placeholder and color formatting.
     *
     * @param player      the player context for placeholder replacement
     * @param material    the material name as a string
     * @param displayName the display name for the item
     * @param format      whether to apply placeholder and color formatting
     * @param loreLines   the lore lines as a collection
     * @return the constructed item stack
     */
    public static ItemStack make(OfflinePlayer player, String material, String displayName, boolean format, Collection<String> loreLines) {
        return make(player, material, displayName, format, loreLines.toArray(new String[0]));
    }

    /**
     * Creates an item stack with a display name and lore, applying formatting by default.
     *
     * @param player      the player context for placeholder replacement
     * @param material    the material type for the item
     * @param displayName the display name for the item
     * @param loreLines   the lore lines as a collection
     * @return the constructed item stack
     */
    public static ItemStack make(OfflinePlayer player, Material material, String displayName, Collection<String> loreLines) {
        return make(player, material, displayName, true, loreLines.toArray(new String[0]));
    }

    /**
     * Creates an item stack with a display name and lore, applying formatting by default.
     *
     * @param player      the player context for placeholder replacement
     * @param material    the material name as a string
     * @param displayName the display name for the item
     * @param loreLines   the lore lines as a collection
     * @return the constructed item stack
     */
    public static ItemStack make(OfflinePlayer player, String material, String displayName, Collection<String> loreLines) {
        return make(player, material, displayName, true, loreLines.toArray(new String[0]));
    }

    /**
     * Creates an item stack with a display name and lore, optionally applying placeholder
     * and color formatting for each line.
     *
     * @param player      the player context for placeholder replacement
     * @param material    the material type for the item
     * @param displayName the display name for the item
     * @param format      whether to apply placeholder and color formatting
     * @param loreLines   the lore lines as varargs
     * @return the constructed item stack
     */
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

    /**
     * Creates an item stack with a display name and lore using a string material name.
     *
     * @param player      the player context for placeholder replacement
     * @param material    the material name as a string
     * @param displayName the display name for the item
     * @param format      whether to apply placeholder and color formatting
     * @param loreLines   the lore lines as varargs
     * @return the constructed item stack
     */
    public static ItemStack make(OfflinePlayer player, String material, String displayName, boolean format, String... loreLines) {
        return make(player, material, displayName, format, loreLines);
    }

    /**
     * Creates an item stack with a display name and lore, applying formatting by default.
     *
     * @param player      the player context for placeholder replacement
     * @param material    the material type for the item
     * @param displayName the display name for the item
     * @param loreLines   the lore lines as varargs
     * @return the constructed item stack
     */
    public static ItemStack make(OfflinePlayer player, Material material, String displayName, String... loreLines) {
        return make(player, material, displayName, true, loreLines);
    }

    /**
     * Creates an item stack with a display name and lore using a string material name,
     * applying formatting by default.
     *
     * @param player      the player context for placeholder replacement
     * @param material    the material name as a string
     * @param displayName the display name for the item
     * @param loreLines   the lore lines as varargs
     * @return the constructed item stack
     */
    public static ItemStack make(OfflinePlayer player, String material, String displayName, String... loreLines) {
        return make(player, material, displayName, true, loreLines);
    }

    /**
     * Creates an item stack with a display name and lore using a dummy player context.
     *
     * @param material    the material type for the item
     * @param displayName the display name for the item
     * @param loreLines   the lore lines as a collection
     * @return the constructed item stack
     */
    public static ItemStack make(Material material, String displayName, Collection<String> loreLines) {
        return make(material, displayName, loreLines.toArray(new String[0]));
    }

    /**
     * Creates an item stack with a display name and lore using a dummy player context
     * and a string material name.
     *
     * @param material    the material name as a string
     * @param displayName the display name for the item
     * @param loreLines   the lore lines as a collection
     * @return the constructed item stack
     */
    public static ItemStack make(String material, String displayName, Collection<String> loreLines) {
        return make(material, displayName, loreLines.toArray(new String[0]));
    }

    /**
     * Creates an item stack with a display name and lore, applying formatting
     * using a dummy player context.
     *
     * @param material    the material type for the item
     * @param displayName the display name for the item
     * @param loreLines   the lore lines as varargs
     * @return the constructed item stack
     */
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

    /**
     * Creates an item stack with a display name and lore using a string material name
     * and a dummy player context.
     *
     * @param material    the material name as a string
     * @param displayName the display name for the item
     * @param loreLines   the lore lines as varargs
     * @return the constructed item stack
     */
    public static ItemStack make(String material, String displayName, String... loreLines) {
        return make(getMaterial(material).orElse(Material.AIR), displayName, loreLines);
    }

    /**
     * Parses a material from its string name (case-insensitive).
     *
     * @param material the material name string
     * @return an Optional containing the Material, or empty if not found
     */
    public static Optional<Material> getMaterial(String material) {
        try {
            return Optional.of(Material.valueOf(material.toUpperCase()));
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get material: ", e);
            return Optional.empty();
        }
    }

    /**
     * Sets a string tag in an item stack's persistent data container.
     *
     * @param stack  the item stack to tag
     * @param plugin the plugin owning the namespaced key
     * @param key    the tag key
     * @param value  the tag value
     */
    public static void setTag(ItemStack stack, JavaPlugin plugin, String key, String value) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(PluginUtils.getPluginKey(plugin, key), PersistentDataType.STRING, value);
            stack.setItemMeta(meta);
        }
    }

    /**
     * Retrieves a string tag from an item stack's persistent data container.
     *
     * @param stack  the item stack to read from
     * @param plugin the plugin owning the namespaced key
     * @param key    the tag key
     * @return an Optional containing the tag value, or empty if not present
     */
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
