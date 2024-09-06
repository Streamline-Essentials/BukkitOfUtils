package host.plas.bou.items;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.bukkitver.VersionSplitter;
import host.plas.bou.utils.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Optional;

public class ItemUtils {
    public static void registerRecipe(CraftingConfig config) {
        Bukkit.getServer().removeRecipe(PluginUtils.getPluginKey(BukkitOfUtils.getInstance(), config.getIdentifier()));

        Bukkit.getServer().addRecipe(getRecipe(config));
    }

    public static Recipe getRecipe(CraftingConfig config) {
        ShapedRecipe recipe = new ShapedRecipe(PluginUtils.getPluginKey(BukkitOfUtils.getInstance(), config.getIdentifier()), getCraftingResult(config));
        recipe.shape(config.getLine1(), config.getLine2(), config.getLine3());

        for (String key : config.getIngredients().keySet()) {
            recipe.setIngredient(key.charAt(0), getItem(config.getIngredients().get(key)).getType());
        }

        return recipe;
    }

    public static ItemStack getCraftingResult(CraftingConfig config) {
        return getItem(config.getResult());
    }

    public static ItemStack getItem(String nbt) {
        Optional<ItemStack> optional = VersionSplitter.getItem(nbt);
        return optional.orElse(new ItemStack(Material.AIR));
    }

    public static String getItemNBT(ItemStack item) {
        Optional<String> optional = VersionSplitter.getItemNBT(item);
        return optional.orElse("");
    }

    public static boolean isItemEqual(ItemStack item1, ItemStack item2) {
        return getItemNBT(item1).equals(getItemNBT(item2));
    }
}
