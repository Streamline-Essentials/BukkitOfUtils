package host.plas.bou.utils;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.sql.DBOperator;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for managing {@link DBOperator} instances associated with {@link BetterPlugin} instances.
 * Provides methods to register, retrieve, remove, and flush database operators.
 */
public class DatabaseUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DatabaseUtils() {
        // Utility class
    }

    private static ConcurrentSkipListMap<BetterPlugin, ConcurrentSkipListSet<DBOperator>> dbOperators = new ConcurrentSkipListMap<>();

    /**
     * Registers a database operator for the specified plugin.
     *
     * @param plugin   the plugin to associate the operator with
     * @param operator the database operator to register
     */
    public static void put(BetterPlugin plugin, DBOperator operator) {
        ConcurrentSkipListSet<DBOperator> operators = get(plugin);
        operators.add(operator);
        dbOperators.put(plugin, operators);

        BukkitOfUtils.getInstance().logInfo("Loaded a Database Operator for '" + plugin.getIdentifier() + "' with ID '" + operator.getId() + "'.");
    }

    /**
     * Removes a database operator by its ID from the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     * @param id         the ID of the database operator to remove
     */
    public static void remove(String identifier, long id) {
        getPlugin(identifier).ifPresent(plugin -> {
            ConcurrentSkipListSet<DBOperator> operators = get(plugin);
            operators.removeIf(operator -> operator.getId() == id);
            dbOperators.put(plugin, operators);
        });
    }

    /**
     * Removes a database operator by its ID from the specified plugin.
     *
     * @param plugin the plugin to remove the operator from
     * @param id     the ID of the database operator to remove
     */
    public static void remove(BetterPlugin plugin, long id) {
        remove(plugin.getIdentifier(), id);
    }

    /**
     * Removes a database operator from the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     * @param operator   the database operator to remove
     */
    public static void remove(String identifier, DBOperator operator) {
        remove(identifier, operator.getId());
    }

    /**
     * Removes a database operator from the specified plugin.
     *
     * @param plugin   the plugin to remove the operator from
     * @param operator the database operator to remove
     */
    public static void remove(BetterPlugin plugin, DBOperator operator) {
        remove(plugin, operator.getId());
    }

    /**
     * Retrieves all database operators associated with the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     * @return a set of database operators, or an empty set if none are found
     */
    public static ConcurrentSkipListSet<DBOperator> get(String identifier) {
        ConcurrentSkipListSet<DBOperator> operators = new ConcurrentSkipListSet<>();

        getPlugin(identifier).ifPresent(plugin -> {
            ConcurrentSkipListSet<DBOperator> pluginOperators = dbOperators.get(plugin);
            if (pluginOperators != null) operators.addAll(pluginOperators);
        });

        return operators;
    }

    /**
     * Returns the number of database operators registered for the given plugin identifier.
     *
     * @param identifier the plugin identifier
     * @return the count of registered operators
     */
    public static int count(String identifier) {
        return get(identifier).size();
    }

    /**
     * Checks whether there are any database operators registered for the given plugin identifier.
     *
     * @param identifier the plugin identifier
     * @return true if at least one operator is registered
     */
    public static boolean hasAny(String identifier) {
        return count(identifier) > 0;
    }

    /**
     * Retrieves all database operators associated with the specified plugin.
     *
     * @param plugin the plugin to retrieve operators for
     * @return a set of database operators, or an empty set if none are found
     */
    public static ConcurrentSkipListSet<DBOperator> get(BetterPlugin plugin) {
        return get(plugin.getIdentifier());
    }

    /**
     * Finds a registered plugin by its identifier.
     *
     * @param identifier the plugin identifier to search for
     * @return an Optional containing the plugin if found, or empty otherwise
     */
    public static Optional<BetterPlugin> getPlugin(String identifier) {
        AtomicReference<BetterPlugin> found = new AtomicReference<>(null);

        dbOperators.forEach((plugin, operators) -> {
            if (found.get() != null) return;
            if (plugin.getIdentifier().equals(identifier)) found.set(plugin);
        });

        return Optional.ofNullable(found.get());
    }

    /**
     * Removes all database operators for the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     */
    public static void clear(String identifier) {
        getPlugin(identifier).ifPresent(dbOperators::remove);
    }

    /**
     * Removes all database operators for the specified plugin.
     *
     * @param plugin the plugin to clear operators from
     */
    public static void clear(BetterPlugin plugin) {
        clear(plugin.getIdentifier());
    }

    /**
     * Checks whether a database operator with the given ID exists for the specified plugin identifier.
     *
     * @param identifier the plugin identifier
     * @param id         the database operator ID to check
     * @return true if an operator with the given ID exists
     */
    public static boolean has(String identifier, long id) {
        return get(identifier).stream().anyMatch(operator -> operator.getId() == id);
    }

    /**
     * Checks whether a database operator with the given ID exists for the specified plugin.
     *
     * @param plugin the plugin to check
     * @param id     the database operator ID to check
     * @return true if an operator with the given ID exists
     */
    public static boolean has(BetterPlugin plugin, long id) {
        return has(plugin.getIdentifier(), id);
    }

    /**
     * Checks whether the specified database operator is registered for the given plugin identifier.
     *
     * @param identifier the plugin identifier
     * @param operator   the database operator to check
     * @return true if the operator is registered
     */
    public static boolean has(String identifier, DBOperator operator) {
        return has(identifier, operator.getId());
    }

    /**
     * Checks whether the specified database operator is registered for the given plugin.
     *
     * @param plugin   the plugin to check
     * @param operator the database operator to check
     * @return true if the operator is registered
     */
    public static boolean has(BetterPlugin plugin, DBOperator operator) {
        return has(plugin.getIdentifier(), operator.getId());
    }

    /**
     * Returns the next available ID for a new database operator under the given plugin identifier.
     *
     * @param identifier the plugin identifier
     * @return the next available operator ID
     */
    public static long getNextId(String identifier) {
        return get(identifier).isEmpty() ? 0 : get(identifier).last().getId() + 1;
    }

    /**
     * Returns the next available ID for a new database operator under the given plugin.
     *
     * @param plugin the plugin to get the next ID for
     * @return the next available operator ID
     */
    public static long getNextId(BetterPlugin plugin) {
        return getNextId(plugin.getIdentifier());
    }

    /**
     * Shuts down all database operators for the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     */
    public static void flush(String identifier) {
        get(identifier).forEach(DBOperator::shutdown);
    }

    /**
     * Shuts down all database operators for the specified plugin.
     *
     * @param plugin the plugin to flush operators for
     */
    public static void flush(BetterPlugin plugin) {
        flush(plugin.getIdentifier());
    }
}
