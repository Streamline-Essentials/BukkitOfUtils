package host.plas.bou.bukkitver.v1_20_4;

import host.plas.bou.BukkitOfUtils;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemHelper {
    public static ItemStack getItem(String nbt) {
        try {
//            CompoundTag compound = TagParser.parseTag(nbt);
            NBTTagCompound compound = MojangsonParser.a(nbt);

            net.minecraft.world.item.ItemStack item = net.minecraft.world.item.ItemStack.a(compound);

            return CraftItemStack.asBukkitCopy(item);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logSevereWithInfo("Failed to parse NBT: " + nbt, e);
        }

        return new ItemStack(Material.AIR);
    }

    public static String getItemNBT(ItemStack item) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
//        CompoundTag compound = new CompoundTag();
//        nmsItem.save(compound);
//
//        return compound.toString();

        NBTTagCompound compound = new NBTTagCompound();
        nmsItem.b(compound);

        return compound.toString();
    }
}
