package host.plas.bou.gui.items;

import host.plas.bou.items.ItemUtils;
import org.bukkit.inventory.ItemStack;
import java.math.BigInteger;

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

    public String getIdentifier() {
        return this.identifier;
    }

    public BigInteger getAmount() {
        return this.amount;
    }

    public String getData() {
        return this.data;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public void setAmount(final BigInteger amount) {
        this.amount = amount;
    }

    public void setData(final String data) {
        this.data = data;
    }
}
