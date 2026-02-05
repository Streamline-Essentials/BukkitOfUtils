package host.plas.bou.items;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;
import java.util.UUID;

public class ConvertableItemStack implements Comparable<ConvertableItemStack> {
    private UUID uuid;
    private int stashedId;
    private Optional<ItemStack> itemStackOptional;
    private Optional<String> itemStringOptional;

    public ConvertableItemStack(ItemStack itemStack, String itemString) {
        this.uuid = ItemBin.getNextUUID();
        this.stashedId = ItemBin.getNextId();
        this.itemStackOptional = Optional.ofNullable(itemStack);
        this.itemStringOptional = Optional.ofNullable(itemString);
        convert();
    }

    public ConvertableItemStack(ItemStack itemStack) {
        this(itemStack, null);
    }

    public ConvertableItemStack(String itemString) {
        this(null, itemString);
    }

    public ConvertableItemStack() {
        this(null, null);
    }

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

    public boolean isNbtStrict() {
        return itemStringOptional.isPresent() && !itemStringOptional.get().startsWith("{") && !itemStringOptional.get().endsWith("}");
    }

    public ConvertableItemStack setItemStack(ItemStack itemStack) {
        this.itemStackOptional = Optional.ofNullable(itemStack);
        return this;
    }

    public ConvertableItemStack setItemString(String itemString) {
        this.itemStringOptional = Optional.ofNullable(itemString);
        return this;
    }

    public ItemStack getItemStack() {
        return itemStackOptional.orElse(null);
    }

    public String getItemString() {
        return itemStringOptional.orElse("{}");
    }

    public ConvertableItemStack prepare() {
        if (
        // Make sure that at least one of the two is present
        (itemStackOptional.isEmpty() || itemStringOptional.isEmpty()) && !(itemStackOptional.isEmpty() && itemStringOptional.isEmpty())) {
            convert();
        }
        return this;
    }

    public ConvertableItemStack stash() {
        ItemBin.add(this);
        return this;
    }

    public ConvertableItemStack unstash() {
        ItemBin.remove(this);
        return this;
    }

    @Override
    public int compareTo(@NotNull ConvertableItemStack o) {
        prepare();
        if (getItemStackOptional().isPresent()) {
            return getItemStack().equals(o.getItemStack()) ? 0 : 1;
        } else {
            return getUuid().compareTo(o.getUuid());
        }
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public int getStashedId() {
        return this.stashedId;
    }

    public Optional<ItemStack> getItemStackOptional() {
        return this.itemStackOptional;
    }

    public Optional<String> getItemStringOptional() {
        return this.itemStringOptional;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    public void setStashedId(final int stashedId) {
        this.stashedId = stashedId;
    }

    public void setItemStackOptional(final Optional<ItemStack> itemStackOptional) {
        this.itemStackOptional = itemStackOptional;
    }

    public void setItemStringOptional(final Optional<String> itemStringOptional) {
        this.itemStringOptional = itemStringOptional;
    }
}
