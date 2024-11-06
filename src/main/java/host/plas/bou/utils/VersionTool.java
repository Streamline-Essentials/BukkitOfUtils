package host.plas.bou.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class VersionTool {
    public static Object getNMSItemStack(ItemStack itemStack) throws Throwable {
        Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit" + getServerVersionDotted() + ".inventory.CraftItemStack");
        Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        return asNMSCopy.invoke(null, itemStack);
    }

    public static Class<?> getNBTTagCompoundClass() throws ClassNotFoundException {
        return Class.forName("net.minecraft.nbt.NBTTagCompound");
    }

    public static Object parseNBT(String nbtJson) throws Exception {
        Class<?> mojangsonParserClass = Class.forName("net.minecraft.nbt.MojangsonParser");
        Method parseMethod = mojangsonParserClass.getMethod("parse", String.class);
        return parseMethod.invoke(null, nbtJson);
    }

    public static Object getNMSItemStackFromNBT(Object nbtTagCompound) throws Exception {
        Class<?> itemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
        Method createStackMethod = itemStackClass.getMethod("a", getNBTTagCompoundClass());
        return createStackMethod.invoke(null, nbtTagCompound);
    }

    public static ItemStack getBukkitItemStack(Object nmsItemStack) throws Exception {
        Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit" + getServerVersionDotted() + ".inventory.CraftItemStack");
        Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStack.getClass());
        return (ItemStack) asBukkitCopy.invoke(null, nmsItemStack);
    }

    public static String getServerVersionDotted() {
        return getServerVersionDotted(true);
    }

    public static String getServerVersionDotted(boolean before) {
        String r;
        if (before) {
            r = "." + getServerVersion();
        } else {
            r = getServerVersion() + ".";
        }

        if (r.equals(".")) {
            r = "";
        }

        return r;
    }

    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        String r;

        try {
            r = packageName.substring("org.bukkit.craftbukkit.".length());
            if (r.contains(".")) {
                r = r.substring(0, r.indexOf("."));
            }
            if (r.isBlank() || r.isEmpty()) {
                r = getNewServerVersion();
            }
        } catch (Throwable e) {
            r = getNewServerVersion();
        }

        return r;
    }

    public static String getNewServerVersion() {
        return "";
    }
}
