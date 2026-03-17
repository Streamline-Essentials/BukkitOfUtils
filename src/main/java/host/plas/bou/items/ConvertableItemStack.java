package host.plas.bou.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents an item stack that can be converted between its {@link ItemStack}
 * and string (NBT) representations. Supports stashing in the {@link ItemBin}
 * for later retrieval.
 */
@Getter @Setter
public class ConvertableItemStack implements Comparable<ConvertableItemStack> {
    /**
     * The unique identifier for this convertable item stack.
     *
     * @param uuid the unique identifier to set
     * @return the unique identifier
     */
    private UUID uuid;

    /**
     * The stashed ID used for {@link ItemBin} storage.
     *
     * @param stashedId the stashed ID to set
     * @return the stashed ID
     */
    private int stashedId;

    /**
     * The optional Bukkit item stack representation.
     *
     * @param itemStackOptional the item stack optional to set
     * @return the item stack optional
     */
    private Optional<ItemStack> itemStackOptional;

    /**
     * The optional NBT string representation of the item.
     *
     * @param itemStringOptional the item string optional to set
     * @return the item string optional
     */
    private Optional<String> itemStringOptional;

    /**
     * Constructs a new ConvertableItemStack with both an item stack and item string.
     * Automatically converts whichever representation is missing.
     *
     * @param itemStack  the Bukkit item stack, or null if not available
     * @param itemString the NBT string representation, or null if not available
     */
    public ConvertableItemStack(ItemStack itemStack, String itemString) {
        this.uuid = ItemBin.getNextUUID();
        this.stashedId = ItemBin.getNextId();

        this.itemStackOptional = Optional.ofNullable(itemStack);
        this.itemStringOptional = Optional.ofNullable(itemString);

        convert();
    }

    /**
     * Constructs a new ConvertableItemStack from a Bukkit item stack.
     *
     * @param itemStack the Bukkit item stack to wrap
     */
    public ConvertableItemStack(ItemStack itemStack) {
        this(itemStack, null);
    }

    /**
     * Constructs a new ConvertableItemStack from an NBT string representation.
     *
     * @param itemString the NBT string representation of the item
     */
    public ConvertableItemStack(String itemString) {
        this(null, itemString);
    }

    /**
     * Constructs an empty ConvertableItemStack with no item data.
     */
    public ConvertableItemStack() {
        this(null, null);
    }

    /**
     * Converts between ItemStack and string representations. If only one
     * representation is present, derives the other from it.
     *
     * @return this instance for method chaining
     */
    public ConvertableItemStack convert() {
        if (itemStackOptional.isPresent() && itemStringOptional.isEmpty()) {
            itemStringOptional = Optional.of(ItemUtils.getItemNBTStrict(itemStackOptional.get()));
        } else if (itemStackOptional.isEmpty() && itemStringOptional.isPresent()) {
            if (isNbtStrict()) {
                itemStackOptional = ItemUtils.getItemStrict(itemStringOptional.get());
            } else {
                itemStackOptional = ItemUtils.getItem(itemStringOptional.get());
            }
        }

        return this;
    }

    /**
     * Checks whether the item string uses strict NBT format (not wrapped in curly braces).
     *
     * @return true if the item string is present and does not start and end with curly braces
     */
    public boolean isNbtStrict() {
        return itemStringOptional.isPresent() && ! itemStringOptional.get().startsWith("{") && ! itemStringOptional.get().endsWith("}");
    }

    /**
     * Sets the item stack and returns this instance for method chaining.
     *
     * @param itemStack the item stack to set, or null to clear
     * @return this instance for method chaining
     */
    public ConvertableItemStack setItemStack(ItemStack itemStack) {
        this.itemStackOptional = Optional.ofNullable(itemStack);

        return this;
    }

    /**
     * Sets the item string and returns this instance for method chaining.
     *
     * @param itemString the NBT string to set, or null to clear
     * @return this instance for method chaining
     */
    public ConvertableItemStack setItemString(String itemString) {
        this.itemStringOptional = Optional.ofNullable(itemString);

        return this;
    }

    /**
     * Returns the item stack, or null if not present.
     *
     * @return the item stack or null
     */
    public ItemStack getItemStack() {
        return itemStackOptional.orElse(null);
    }

    /**
     * Returns the item string, or an empty JSON object string if not present.
     *
     * @return the item string or "{}"
     */
    public String getItemString() {
        return itemStringOptional.orElse("{}");
    }

    /**
     * Ensures both representations are populated by converting if exactly
     * one representation is present.
     *
     * @return this instance for method chaining
     */
    public ConvertableItemStack prepare() {
        if (
                // Make sure that at least one of the two is present
                ( itemStackOptional.isEmpty() || itemStringOptional.isEmpty() ) &&
                ! ( itemStackOptional.isEmpty() && itemStringOptional.isEmpty() )
        ) {
            convert();
        }

        return this;
    }

    /**
     * Adds this item to the {@link ItemBin} for later retrieval.
     *
     * @return this instance for method chaining
     */
    public ConvertableItemStack stash() {
        ItemBin.add(this);

        return this;
    }

    /**
     * Removes this item from the {@link ItemBin}.
     *
     * @return this instance for method chaining
     */
    public ConvertableItemStack unstash() {
        ItemBin.remove(this);

        return this;
    }

    /**
     * Compares this ConvertableItemStack to another. Compares by item stack equality
     * if present, otherwise falls back to UUID comparison.
     *
     * @param o the other ConvertableItemStack to compare to
     * @return 0 if equal, 1 if not equal by item stack, or UUID comparison result
     */
    @Override
    public int compareTo(@NotNull ConvertableItemStack o) {
        prepare();

        if (getItemStackOptional().isPresent()) {
            return getItemStack().equals(o.getItemStack()) ? 0 : 1;
        } else {
            return getUuid().compareTo(o.getUuid());
        }
    }
}
