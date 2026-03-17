package host.plas.bou.items.droppable;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.math.CosmicMath;
import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.Identifiable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents an item that can be dropped with a configurable chance and amount range.
 * Supports both regular items (via NBT string) and keyed item types.
 */
@Getter @Setter
public class DroppableItem implements Identifiable {
    private String identifier;

    /**
     * The type of droppable item (ITEM or KEYED).
     *
     * @param type the droppable item type to set
     * @return the droppable item type
     */
    private DroppableItemType type;

    /**
     * The item string (NBT for ITEM type, or keyed type name for KEYED type).
     *
     * @param itemString the item string to set
     * @return the item string
     */
    private String itemString;

    /**
     * The drop chance as a percentage (0-100).
     *
     * @param chance the drop chance to set
     * @return the drop chance
     */
    private double chance; // out of 100

    /**
     * The minimum drop amount, or -1 if unspecified.
     *
     * @param minAmount the minimum amount to set
     * @return the minimum amount
     */
    private int minAmount;

    /**
     * The maximum drop amount, or -1 if unspecified.
     *
     * @param maxAmount the maximum amount to set
     * @return the maximum amount
     */
    private int maxAmount;

    /**
     * Constructs a new DroppableItem with all parameters.
     *
     * @param identifier the unique identifier for this droppable item
     * @param type       the type of droppable item (ITEM or KEYED)
     * @param itemString the item string (NBT for ITEM type, or keyed type name for KEYED type)
     * @param chance     the drop chance as a percentage (0-100)
     * @param minAmount  the minimum drop amount, or -1 for unspecified
     * @param maxAmount  the maximum drop amount, or -1 for unspecified
     */
    public DroppableItem(String identifier, DroppableItemType type, String itemString, double chance, int minAmount, int maxAmount) {
        this.identifier = identifier;
        this.type = type;
        this.itemString = itemString;
        this.chance = chance;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    /**
     * Constructs a new DroppableItem with no amount range specified.
     *
     * @param identifier the unique identifier for this droppable item
     * @param type       the type of droppable item
     * @param itemString the item string
     * @param chance     the drop chance as a percentage (0-100)
     */
    public DroppableItem(String identifier, DroppableItemType type, String itemString, double chance) {
        this(identifier, type, itemString, chance, -1, -1);
    }

    /**
     * Constructs a new DroppableItem of type ITEM with an amount range.
     *
     * @param identifier the unique identifier for this droppable item
     * @param itemString the NBT item string
     * @param chance     the drop chance as a percentage (0-100)
     * @param minAmount  the minimum drop amount
     * @param maxAmount  the maximum drop amount
     */
    public DroppableItem(String identifier, String itemString, double chance, int minAmount, int maxAmount) {
        this(identifier, DroppableItemType.ITEM, itemString, chance, minAmount, maxAmount);
    }

    /**
     * Constructs a new DroppableItem of type ITEM with no amount range.
     *
     * @param identifier the unique identifier for this droppable item
     * @param itemString the NBT item string
     * @param chance     the drop chance as a percentage (0-100)
     */
    public DroppableItem(String identifier, String itemString, double chance) {
        this(identifier, DroppableItemType.ITEM, itemString, chance);
    }

    /**
     * Retrieves the keyed type for this item if the type is KEYED.
     *
     * @return an Optional containing the keyed type, or empty if not a KEYED type or parsing fails
     */
    public Optional<DroppableItemKeyedType> getKeyedType() {
        if (getType() != DroppableItemType.KEYED) return Optional.empty();

        try {
            DroppableItemKeyedType keyedType = DroppableItemKeyedType.valueOf(getItemString().toUpperCase());
            return Optional.of(keyedType);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Failed to parse keyed type: " + getItemString(), e);
            return Optional.empty();
        }
    }

    /**
     * Checks whether both minimum and maximum amounts are specified.
     *
     * @return true if both min and max amounts are specified (not -1)
     */
    public boolean hasAmountSpecified() {
        return hasMinSpecified() && hasMaxSpecified();
    }

    /**
     * Checks whether the minimum amount is specified.
     *
     * @return true if the minimum amount is not -1
     */
    public boolean hasMinSpecified() {
        return getMinnedAmount() != -1;
    }

    /**
     * Checks whether the maximum amount is specified.
     *
     * @return true if the maximum amount is not -1
     */
    public boolean hasMaxSpecified() {
        return getMaxedAmount() != -1;
    }

    /**
     * Returns the normalized minimum amount value.
     *
     * @return the minimum of the min amount values
     */
    public int getMinnedAmount() {
        return Math.min(getMinAmount(), getMinAmount());
    }

    /**
     * Returns the normalized maximum amount value.
     *
     * @return the maximum of the max amount values
     */
    public int getMaxedAmount() {
        return Math.max(getMaxAmount(), getMaxAmount());
    }

    /**
     * Returns the effective minimum amount, clamped between 1 and 64.
     * Defaults to 1 if not specified.
     *
     * @return the effective minimum amount
     */
    public int getTrueMinAmount() {
        return hasMinSpecified() ? Math.min(Math.max(getMinnedAmount(), 1), 64) : 1;
    }

    /**
     * Returns the effective maximum amount, clamped between 1 and 64.
     * Defaults to the true minimum amount if not specified.
     *
     * @return the effective maximum amount
     */
    public int getTrueMaxAmount() {
        return hasMaxSpecified() ? Math.max(Math.min(getMaxedAmount(), 64), 1) : getTrueMinAmount();
    }

    /**
     * Generates a random amount between the true minimum and true maximum amounts.
     *
     * @return a random amount within the effective range
     */
    public int pollRandomAmount() {
        int min = getTrueMinAmount();
        int max = getTrueMaxAmount();
        if (min == max) return min;

        return CosmicMath.getRandomInt(min, max);
    }

    /**
     * Applies a consumer action to this droppable item.
     *
     * @param consumer the consumer to apply
     */
    public void apply(Consumer<DroppableItem> consumer) {
        consumer.accept(this);
    }

    /**
     * Rolls to determine whether this item should drop based on its chance percentage.
     *
     * @return true if the random roll is within the chance threshold
     */
    public boolean passesRoll() {
        double roll = CosmicMath.getRandomDouble(0, 100);
        return roll <= getChance();
    }
}
