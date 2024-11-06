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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class ItemUtils {
    public static final Gson GSON = new GsonBuilder().create();

    public static String stackToJson(ItemStack stack) {
        try {
            Map<String, Object> map = stack.serialize();

            return GSON.toJson(map);
        } catch (Exception err) {
            try {
                // Get NMS ItemStack
                Object nmsItemStack = VersionTool.getNMSItemStack(stack);

                if (nmsItemStack == null) {
                    return "{}";  // Return empty JSON if the item is null or incompatible
                }

                // Get the NBT tag compound using reflection
                Class<?> nmsItemStackClass = nmsItemStack.getClass();
                Method saveMethod = nmsItemStackClass.getMethod("save", VersionTool.getNBTTagCompoundClass());
                Object nbtTagCompound = saveMethod.invoke(nmsItemStack, VersionTool.getNBTTagCompoundClass().newInstance());

                // Convert NBT to JSON using `toString()`
                return nbtTagCompound.toString();
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logWarning("Failed to serialize ItemStack to JSON: ", e);
                return "{}";
            }
        }
    }

    public static ItemStack jsonToStack(String nbtJson) {
        try {
            // Get an instance of NBTTagCompound from the JSON string
            Object nbtTagCompound = VersionTool.parseNBT(nbtJson);
            if (nbtTagCompound == null) {
                return null;
            }

            // Create an NMS ItemStack from the NBT data
            Object nmsItemStack = VersionTool.getNMSItemStackFromNBT(nbtTagCompound);

            // Convert the NMS ItemStack back to Bukkit's ItemStack
            return VersionTool.getBukkitItemStack(nmsItemStack);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
            return Optional.ofNullable(jsonToStack(nbt));
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get item from NBT: ", e);
            return Optional.empty();
        }
    }

    public static String getItemNBT(ItemStack item) {
        try {
            return stackToJson(item);
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to get NBT from item: ", e);
            return "{}";
        }
    }

    public static boolean isItemEqual(ItemStack item1, ItemStack item2) {
        return getItemNBT(item1).equals(getItemNBT(item2));
    }
}
