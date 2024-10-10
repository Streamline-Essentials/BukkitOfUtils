package host.plas.bou.serialization.items;

import host.plas.bou.serialization.ISerializer;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer implements ISerializer<ItemStack> {
    @Override
    public String toString(ItemStack object) {
        if (object == null) {
            return "-null-";
        }

        return ISerializer.super.toString(object);
    }

    @Override
    public ItemStack fromString(String value) {
        if (value.equals("-null-")) {
            return null;
        }

        return ISerializer.super.fromString(value);
    }
}
