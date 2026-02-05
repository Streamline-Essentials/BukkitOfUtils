package host.plas.bou.items.droppable;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.math.CosmicMath;
import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.Identifiable;

import java.util.Optional;
import java.util.function.Consumer;

@Getter @Setter
public class DroppableItem implements Identifiable {
    private String identifier;

    private DroppableItemType type;
    private String itemString;
    private double chance; // out of 100
    private int minAmount;
    private int maxAmount;

    public DroppableItem(String identifier, DroppableItemType type, String itemString, double chance, int minAmount, int maxAmount) {
        this.identifier = identifier;
        this.type = type;
        this.itemString = itemString;
        this.chance = chance;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public DroppableItem(String identifier, DroppableItemType type, String itemString, double chance) {
        this(identifier, type, itemString, chance, -1, -1);
    }

    public DroppableItem(String identifier, String itemString, double chance, int minAmount, int maxAmount) {
        this(identifier, DroppableItemType.ITEM, itemString, chance, minAmount, maxAmount);
    }

    public DroppableItem(String identifier, String itemString, double chance) {
        this(identifier, DroppableItemType.ITEM, itemString, chance);
    }

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

    public boolean hasAmountSpecified() {
        return hasMinSpecified() && hasMaxSpecified();
    }

    public boolean hasMinSpecified() {
        return getMinnedAmount() != -1;
    }

    public boolean hasMaxSpecified() {
        return getMaxedAmount() != -1;
    }

    public int getMinnedAmount() {
        return Math.min(getMinAmount(), getMinAmount());
    }

    public int getMaxedAmount() {
        return Math.max(getMaxAmount(), getMaxAmount());
    }

    public int getTrueMinAmount() {
        return hasMinSpecified() ? Math.min(Math.max(getMinnedAmount(), 1), 64) : 1;
    }

    public int getTrueMaxAmount() {
        return hasMaxSpecified() ? Math.max(Math.min(getMaxedAmount(), 64), 1) : getTrueMinAmount();
    }

    public int pollRandomAmount() {
        int min = getTrueMinAmount();
        int max = getTrueMaxAmount();
        if (min == max) return min;

        return CosmicMath.getRandomInt(min, max);
    }

    public void apply(Consumer<DroppableItem> consumer) {
        consumer.accept(this);
    }

    public boolean passesRoll() {
        double roll = CosmicMath.getRandomDouble(0, 100);
        return roll <= getChance();
    }
}
