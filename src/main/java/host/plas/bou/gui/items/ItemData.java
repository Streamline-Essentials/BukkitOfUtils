package host.plas.bou.gui.items;

import host.plas.bou.items.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

/**
 * Represents serialized item data with an identifier, amount, and NBT data string.
 * Provides methods for serializing and deserializing {@link ItemStack} objects.
 */
@Getter @Setter
public class ItemData {
    /**
     * The unique identifier for this item data entry.
     *
     * @param identifier the unique identifier to set
     * @return the unique identifier
     */
    private String identifier;
    /**
     * The quantity of items represented by this data entry.
     *
     * @param amount the item quantity to set
     * @return the item quantity
     */
    private BigInteger amount;
    /**
     * The serialized NBT data string representing the item stack.
     *
     * @param data the serialized NBT data string to set
     * @return the serialized NBT data string
     */
    private String data;

    /**
     * Constructs a new ItemData with the given identifier, amount, and serialized data string.
     *
     * @param identifier the unique identifier for this item data
     * @param amount     the quantity of items
     * @param data       the serialized NBT data string
     */
    public ItemData(String identifier, BigInteger amount, String data) {
        this.identifier = identifier;
        this.amount = amount;
        this.data = data;
    }

    /**
     * Constructs a new ItemData by serializing the given item stack.
     *
     * @param identifier the unique identifier for this item data
     * @param amount     the quantity of items
     * @param stack      the item stack to serialize
     */
    public ItemData(String identifier, BigInteger amount, ItemStack stack) {
        this(identifier, amount, serialize(stack));
    }

    /**
     * Deserializes the stored data string back into an {@link ItemStack}.
     *
     * @return the deserialized item stack
     */
    public ItemStack getStack() {
        return deserialize(data);
    }

    /**
     * Serializes an item stack into its NBT string representation.
     *
     * @param item the item stack to serialize
     * @return the serialized NBT data string
     */
    public static String serialize(ItemStack item) {
        return ItemUtils.getItemNBT(item);
    }

    /**
     * Deserializes an NBT data string back into an {@link ItemStack}.
     *
     * @param data the serialized NBT data string
     * @return the deserialized item stack
     */
    public static ItemStack deserialize(String data) {
        return ItemUtils.getItemAbs(data);
    }
}
