package host.plas.bou.utils;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.sql.DBOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for managing {@link DBOperator} instances associated with {@link BetterPlugin} instances.
 * Provides methods to register, retrieve, remove, and flush database operators.
 */
public final class DatabaseUtils {
    private DatabaseUtils() {
    }

    private static final ConcurrentHashMap<String, BetterPlugin> PLUGINS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ConcurrentSkipListSet<DBOperator>> OPERATORS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicLong> NEXT_IDS = new ConcurrentHashMap<>();

    /**
     * Registers a database operator for the specified plugin.
     *
     * @param plugin   the plugin to associate the operator with
     * @param operator the database operator to register
     */
    public static void put(BetterPlugin plugin, DBOperator operator) {
        if (plugin == null || operator == null) return;

        String id = plugin.getIdentifier();
        PLUGINS.put(id, plugin);
        OPERATORS.computeIfAbsent(id, key -> new ConcurrentSkipListSet<>()).add(operator);

        if (BukkitOfUtils.getInstance() != null) {
            BukkitOfUtils.getInstance().logInfo("Loaded a Database Operator for '" + id + "' with ID '" + operator.getId() + "'.");
        }
    }

    /**
     * Removes a database operator by its ID from the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     * @param id         the ID of the database operator to remove
     */
    public static void remove(String identifier, long id) {
        if (identifier == null) return;
        ConcurrentSkipListSet<DBOperator> operators = OPERATORS.get(identifier);
        if (operators == null) return;
        operators.removeIf(operator -> operator.getId() == id);
        if (operators.isEmpty()) {
            OPERATORS.remove(identifier, operators);
        }
    }

    /**
     * Removes a database operator by its ID from the specified plugin.
     *
     * @param plugin the plugin to remove the operator from
     * @param id     the ID of the database operator to remove
     */
    public static void remove(BetterPlugin plugin, long id) {
        if (plugin == null) return;
        remove(plugin.getIdentifier(), id);
    }

    /**
     * Removes a database operator from the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     * @param operator   the database operator to remove
     */
    public static void remove(String identifier, DBOperator operator) {
        if (operator == null) return;
        remove(identifier, operator.getId());
    }

    /**
     * Removes a database operator from the specified plugin.
     *
     * @param plugin   the plugin to remove the operator from
     * @param operator the database operator to remove
     */
    public static void remove(BetterPlugin plugin, DBOperator operator) {
        if (plugin == null || operator == null) return;
        remove(plugin.getIdentifier(), operator.getId());
    }

    /**
     * Retrieves all database operators associated with the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     * @return a set of database operators, or an empty set if none are found
     */
    public static ConcurrentSkipListSet<DBOperator> get(String identifier) {
        ConcurrentSkipListSet<DBOperator> copy = new ConcurrentSkipListSet<>();
        if (identifier == null) return copy;

        ConcurrentSkipListSet<DBOperator> operators = OPERATORS.get(identifier);
        if (operators != null) {
            copy.addAll(operators);
        }
        return copy;
    }

    /**
     * Returns the number of database operators registered for the given plugin identifier.
     *
     * @param identifier the plugin identifier
     * @return the count of registered operators
     */
    public static int count(String identifier) {
        if (identifier == null) return 0;
        ConcurrentSkipListSet<DBOperator> operators = OPERATORS.get(identifier);
        return operators == null ? 0 : operators.size();
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
        if (plugin == null) return new ConcurrentSkipListSet<>();
        return get(plugin.getIdentifier());
    }

    /**
     * Finds a registered plugin by its identifier.
     *
     * @param identifier the plugin identifier to search for
     * @return an Optional containing the plugin if found, or empty otherwise
     */
    public static Optional<BetterPlugin> getPlugin(String identifier) {
        if (identifier == null) return Optional.empty();
        return Optional.ofNullable(PLUGINS.get(identifier));
    }

    /**
     * Removes all database operators for the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     */
    public static void clear(String identifier) {
        if (identifier == null) return;
        OPERATORS.remove(identifier);
        // Keep plugin mapping so getNextId remains stable across reloads of operators.
    }

    /**
     * Removes all database operators for the specified plugin.
     *
     * @param plugin the plugin to clear operators from
     */
    public static void clear(BetterPlugin plugin) {
        if (plugin == null) return;
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
        if (identifier == null) return false;
        ConcurrentSkipListSet<DBOperator> operators = OPERATORS.get(identifier);
        if (operators == null) return false;
        for (DBOperator operator : operators) {
            if (operator.getId() == id) return true;
        }
        return false;
    }

    /**
     * Checks whether a database operator with the given ID exists for the specified plugin.
     *
     * @param plugin the plugin to check
     * @param id     the database operator ID to check
     * @return true if an operator with the given ID exists
     */
    public static boolean has(BetterPlugin plugin, long id) {
        if (plugin == null) return false;
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
        if (operator == null) return false;
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
        if (plugin == null || operator == null) return false;
        return has(plugin.getIdentifier(), operator.getId());
    }

    /**
     * Returns the next available ID for a new database operator under the given plugin identifier.
     *
     * @param identifier the plugin identifier
     * @return the next available operator ID
     */
    public static long getNextId(String identifier) {
        if (identifier == null) return 0L;
        return NEXT_IDS.computeIfAbsent(identifier, key -> new AtomicLong(0L)).getAndIncrement();
    }

    /**
     * Returns the next available ID for a new database operator under the given plugin.
     *
     * @param plugin the plugin to get the next ID for
     * @return the next available operator ID
     */
    public static long getNextId(BetterPlugin plugin) {
        if (plugin == null) return 0L;
        return getNextId(plugin.getIdentifier());
    }

    /**
     * Shuts down all database operators for the plugin identified by the given identifier.
     *
     * @param identifier the plugin identifier
     */
    public static void flush(String identifier) {
        if (identifier == null) return;

        List<DBOperator> snapshot = new ArrayList<>(get(identifier));
        for (DBOperator operator : snapshot) {
            try {
                operator.shutdown();
            } catch (Throwable t) {
                if (BukkitOfUtils.getInstance() != null) {
                    BukkitOfUtils.getInstance().logWarning("Failed to shut down database operator id=" + operator.getId()
                            + " for '" + identifier + "': " + t.getMessage());
                }
            }
        }
        clear(identifier);
    }

    /**
     * Shuts down all database operators for the specified plugin.
     *
     * @param plugin the plugin to flush operators for
     */
    public static void flush(BetterPlugin plugin) {
        if (plugin == null) return;
        flush(plugin.getIdentifier());
    }
}
