package host.plas.bou.bukkitver;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.ClassHelper;
//import net.minecraft.nbt.MojangsonParser;
//import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VersionSplitter {
    public static Optional<String> getVersion() {
        return ClassHelper.getServerVersion();
    }

    public static Optional<Object> runNMSMethod(String name, String className, Object object, Object... params) {
        if (getVersion().isEmpty()) {
            BukkitOfUtils.getInstance().logWarning("Failed to run nms method due to improper version.");
            return Optional.empty();
        }

        String version = getVersion().get();

        try {
            Class nmsClass = Class.forName("net.minecraft.server." + version + "." + className);
            List<Class<?>> classes = new ArrayList<>();
            for(Object object1 : params) {
                classes.add(object1.getClass());
            }
            Object obj = nmsClass.getMethod(name, (Class[]) classes.toArray()).invoke(object, params);
            return Optional.of(obj);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static Optional<ItemStack> getItem(String nbt) {
        try {
//            CompoundTag compound = TagParser.parseTag(nbt);
//            NBTTagCompound compound = MojangsonParser.a(nbt);
            NBTTagCompound compound = MojangsonParser.parse(nbt);

            net.minecraft.server.v1_16_R3.ItemStack item = net.minecraft.server.v1_16_R3.ItemStack.a(compound);

            Optional<Object> optional = runNMSMethod("asBukkitCopy", "inventory.CraftItemStack", item);
            if (optional.isEmpty()) return Optional.empty();

            try {
                return Optional.of((ItemStack) optional.get());
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logSevereWithInfo("Failed to get item: " + nbt, e);
            }
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevereWithInfo("Failed to parse NBT: " + nbt, e);
        }

        return Optional.empty();
    }

    public static Optional<String> getItemNBT(ItemStack item) {
        Optional<Object> optional = runNMSMethod("asNMSCopy", "inventory.CraftItemStack", item);
        if (optional.isEmpty()) return Optional.empty();

        try {
            net.minecraft.server.v1_16_R3.ItemStack nmsItem = optional.map(o -> (net.minecraft.server.v1_16_R3.ItemStack) o).get();
//        CompoundTag compound = new CompoundTag();
//        nmsItem.save(compound);
//
//        return compound.toString();

            NBTTagCompound compound = new NBTTagCompound();
            nmsItem.save(compound);

            return Optional.of(compound.toString());
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevereWithInfo("Failed to get NBT: " + item, e);

            return Optional.empty();
        }
    }
}
