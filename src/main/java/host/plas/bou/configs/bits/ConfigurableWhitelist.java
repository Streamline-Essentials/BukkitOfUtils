package host.plas.bou.configs.bits;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;

@Getter @Setter
public class ConfigurableWhitelist<T extends Comparable<T>> implements Identifiable {
    private String identifier;

    private ConcurrentSkipListSet<T> whitelist;
    private boolean blacklist;

    public ConfigurableWhitelist(String identifier) {
        this.identifier = identifier;

        whitelist = new ConcurrentSkipListSet<>();

        blacklist = false;
    }

    public boolean contains(T item) {
        return whitelist.contains(item) != blacklist;
    }

    public void add(T item) {
        whitelist.add(item);
    }

    public void remove(T item) {
        whitelist.remove(item);
    }

    public void removeIf(Predicate<T> predicate) {
        whitelist.removeIf(predicate);
    }

    public void clear() {
        whitelist.clear();
    }

    public boolean check(Predicate<T> predicate) {
        return whitelist.stream().anyMatch(predicate) && ! blacklist;
    }

    public boolean checkAll(Predicate<T> predicate) {
        return whitelist.stream().allMatch(predicate) && ! blacklist;
    }

    public boolean checkNone(Predicate<T> predicate) {
        return whitelist.stream().noneMatch(predicate) && ! blacklist;
    }

    public boolean check(T item) {
        return ( whitelist.contains(item) && ! blacklist ) || ( ! whitelist.contains(item) && blacklist );
    }
}
