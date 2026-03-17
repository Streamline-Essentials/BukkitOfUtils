package host.plas.bou.serialization.items;

import host.plas.bou.serialization.ISerializer;
import org.bukkit.inventory.ItemStack;

/**
 * A serializer for Bukkit ItemStack objects that handles null values
 * by using a special "-null-" sentinel string.
 */
public class ItemStackSerializer implements ISerializer<ItemStack> {
    /**
     * Constructs a new ItemStackSerializer instance.
     */
    public ItemStackSerializer() {
        // default constructor
    }

    /**
     * Serializes an ItemStack to a string. Returns "-null-" for null items.
     *
     * @param object the ItemStack to serialize, or null
     * @return the serialized string, or "-null-" if the item is null
     */
    @Override
    public String toString(ItemStack object) {
        if (object == null) {
            return "-null-";
        }

        return ISerializer.super.toString(object);
    }

    /**
     * Deserializes an ItemStack from a string. Returns null if the string is "-null-".
     *
     * @param value the serialized string
     * @return the deserialized ItemStack, or null if the value is "-null-"
     */
    @Override
    public ItemStack fromString(String value) {
        if (value.equals("-null-")) {
            return null;
        }

        return ISerializer.super.fromString(value);
    }
}
