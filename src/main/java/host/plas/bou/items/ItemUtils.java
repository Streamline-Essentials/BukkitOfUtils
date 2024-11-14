package host.plas.bou.items;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.PluginUtils;
import host.plas.bou.utils.VersionTool;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Optional;

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

    public static boolean isItemEqual(ItemStack item1, ItemStack item2) {
        return getItemNBT(item1).equals(getItemNBT(item2));
    }
}
