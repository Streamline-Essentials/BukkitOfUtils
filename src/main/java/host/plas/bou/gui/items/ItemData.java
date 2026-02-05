package host.plas.bou.gui.items;

import host.plas.bou.items.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

@Getter @Setter
public class ItemData {
    private String identifier;
    private BigInteger amount;
    private String data;

    public ItemData(String identifier, BigInteger amount, String data) {
        this.identifier = identifier;
        this.amount = amount;
        this.data = data;
    }

    public ItemData(String identifier, BigInteger amount, ItemStack stack) {
        this(identifier, amount, serialize(stack));
    }

    public ItemStack getStack() {
        return deserialize(data);
    }

    public static String serialize(ItemStack item) {
        return ItemUtils.getItemNBT(item);
    }

    public static ItemStack deserialize(String data) {
        return ItemUtils.getItemAbs(data);
    }
}
