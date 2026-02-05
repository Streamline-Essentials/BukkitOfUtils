package host.plas.bou.utils;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.sql.DBOperator;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseUtils {
    private static ConcurrentSkipListMap<BetterPlugin, ConcurrentSkipListSet<DBOperator>> dbOperators = new ConcurrentSkipListMap<>();

    public static void put(BetterPlugin plugin, DBOperator operator) {
        ConcurrentSkipListSet<DBOperator> operators = get(plugin);
        operators.add(operator);
        dbOperators.put(plugin, operators);

        BukkitOfUtils.getInstance().logInfo("Loaded a Database Operator for '" + plugin.getIdentifier() + "' with ID '" + operator.getId() + "'.");
    }

    public static void remove(String identifier, long id) {
        getPlugin(identifier).ifPresent(plugin -> {
            ConcurrentSkipListSet<DBOperator> operators = get(plugin);
            operators.removeIf(operator -> operator.getId() == id);
            dbOperators.put(plugin, operators);
        });
    }

    public static void remove(BetterPlugin plugin, long id) {
        remove(plugin.getIdentifier(), id);
    }

    public static void remove(String identifier, DBOperator operator) {
        remove(identifier, operator.getId());
    }

    public static void remove(BetterPlugin plugin, DBOperator operator) {
        remove(plugin, operator.getId());
    }

    public static ConcurrentSkipListSet<DBOperator> get(String identifier) {
        ConcurrentSkipListSet<DBOperator> operators = new ConcurrentSkipListSet<>();

        getPlugin(identifier).ifPresent(plugin -> {
            ConcurrentSkipListSet<DBOperator> pluginOperators = dbOperators.get(plugin);
            if (pluginOperators != null) operators.addAll(pluginOperators);
        });

        return operators;
    }

    public static int count(String identifier) {
        return get(identifier).size();
    }

    public static boolean hasAny(String identifier) {
        return count(identifier) > 0;
    }

    public static ConcurrentSkipListSet<DBOperator> get(BetterPlugin plugin) {
        return get(plugin.getIdentifier());
    }

    public static Optional<BetterPlugin> getPlugin(String identifier) {
        AtomicReference<BetterPlugin> found = new AtomicReference<>(null);

        dbOperators.forEach((plugin, operators) -> {
            if (found.get() != null) return;
            if (plugin.getIdentifier().equals(identifier)) found.set(plugin);
        });

        return Optional.ofNullable(found.get());
    }

    public static void clear(String identifier) {
        getPlugin(identifier).ifPresent(dbOperators::remove);
    }

    public static void clear(BetterPlugin plugin) {
        clear(plugin.getIdentifier());
    }

    public static boolean has(String identifier, long id) {
        return get(identifier).stream().anyMatch(operator -> operator.getId() == id);
    }

    public static boolean has(BetterPlugin plugin, long id) {
        return has(plugin.getIdentifier(), id);
    }

    public static boolean has(String identifier, DBOperator operator) {
        return has(identifier, operator.getId());
    }

    public static boolean has(BetterPlugin plugin, DBOperator operator) {
        return has(plugin.getIdentifier(), operator.getId());
    }

    public static long getNextId(String identifier) {
        return get(identifier).isEmpty() ? 0 : get(identifier).last().getId() + 1;
    }

    public static long getNextId(BetterPlugin plugin) {
        return getNextId(plugin.getIdentifier());
    }

    public static void flush(String identifier) {
        get(identifier).forEach(DBOperator::shutdown);
    }

    public static void flush(BetterPlugin plugin) {
        flush(plugin.getIdentifier());
    }
}
