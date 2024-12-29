package host.plas.bou.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class ItemBin {
    @Getter @Setter
    private static ConcurrentSkipListSet<ConvertableItemStack> bin = new ConcurrentSkipListSet<>();
    @Getter @Setter
    private static AtomicInteger atomicNextId = new AtomicInteger(0);

    public static void add(ConvertableItemStack itemStack) {
        bin.add(itemStack);
    }

    public static void remove(ConvertableItemStack itemStack) {
        bin.remove(itemStack);
    }

    public static void removeIf(Predicate<ConvertableItemStack> predicate) {
        bin.removeIf(predicate);
    }

    public static void clear() {
        bin.clear();
    }

    public static Optional<ConvertableItemStack> get(Predicate<ConvertableItemStack> predicate) {
        return bin.stream().filter(predicate).findFirst();
    }

    public static Optional<ConvertableItemStack> get(ItemStack stack) {
        return get(itemStack -> {
            AtomicBoolean found = new AtomicBoolean(false);

            if (itemStack.getItemStackOptional().isPresent()) {
                found.set(itemStack.getItemStack().equals(stack));
            }

            return found.get();
        });
    }

    public static Optional<ConvertableItemStack> get(String itemString) {
        return get(itemStack -> {
            AtomicBoolean found = new AtomicBoolean(false);

            if (itemStack.getItemStringOptional().isPresent()) {
                found.set(itemStack.getItemString().equals(itemString));
            }

            return found.get();
        });
    }

    public static Optional<ConvertableItemStack> get(UUID uuid) {
        return get(itemStack -> itemStack.getUuid().equals(uuid));
    }

    public static Optional<ConvertableItemStack> get(int stashedId) {
        return get(itemStack -> itemStack.getStashedId() == stashedId);
    }

    public static boolean contains(ConvertableItemStack itemStack) {
        return bin.contains(itemStack);
    }

    public static boolean contains(ItemStack stack) {
        return get(stack).isPresent();
    }

    public static boolean contains(String itemString) {
        return get(itemString).isPresent();
    }

    public static boolean has(UUID uuid) {
        return get(uuid).isPresent();
    }

    public static boolean has(int stashedId) {
        return get(stashedId).isPresent();
    }

    public static boolean has(Predicate<ConvertableItemStack> predicate) {
        return get(predicate).isPresent();
    }

    public static UUID getNextUUID() {
        AtomicReference<UUID> uuidRef = new AtomicReference<>(UUID.randomUUID());

        while (bin.stream().anyMatch(itemStack -> itemStack.getUuid().equals(uuidRef.get()))) {
            uuidRef.set(UUID.randomUUID());
        }

        return uuidRef.get();
    }

    public static int getNextId() {
        return atomicNextId.getAndIncrement();
    }

    public static ConcurrentSkipListSet<Integer> getStashedIds() {
        return bin.stream().collect(ConcurrentSkipListSet::new, (set, itemStack) -> set.add(itemStack.getStashedId()), Collection::addAll);
    }

    public static ConcurrentSkipListSet<String> getStashedIdsAsStrings() {
        return bin.stream().collect(ConcurrentSkipListSet::new, (set, itemStack) -> set.add(String.valueOf(itemStack.getStashedId())), Collection::addAll);
    }
}
