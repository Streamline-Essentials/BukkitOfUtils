package host.plas.bou.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import host.plas.bou.BetterPlugin;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.DatabaseUtils;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Abstract base class for database operations using HikariCP connection pooling.
 * Supports both MySQL and SQLite databases, providing methods for executing
 * statements, queries, and managing the database lifecycle.
 *
 * <p>Connections returned by {@link #getConnection()} are borrowed from the pool and
 * <strong>must be closed</strong> by the caller (prefer try-with-resources). Higher-level
 * {@link #execute} / {@link #executeQuery} helpers already handle borrow/return.</p>
 */
@Getter
@Setter
public abstract class DBOperator implements Comparable<DBOperator> {
    /** The cooldown period in milliseconds between shutdown checks (2 seconds). */
    public static final long COOLDOWN_MILLIS = 1000L * 2;

    private static final Consumer<PreparedStatement> NO_PARAMS = stmt -> {
    };

    /**
     * The unique numeric identifier for this operator.
     *
     * @param id the identifier to set
     * @return the identifier
     */
    private long id;

    /**
     * The connection configuration for this operator.
     *
     * @param connectorSet the connector set to set
     * @return the connector set
     */
    private ConnectorSet connectorSet;
    /**
     * The HikariCP data source used for connection pooling.
     *
     * @param dataSource the data source to set
     * @return the data source
     */
    private HikariDataSource dataSource;
    /**
     * The plugin that owns this database operator.
     *
     * @param pluginUser the plugin to set
     * @return the owning plugin
     */
    private BetterPlugin pluginUser;

    /**
     * Legacy field retained for API compatibility. No longer used as a shared connection;
     * callers should borrow via {@link #getConnection()} and close when finished.
     *
     * @param rawConnection the raw connection to set
     * @return the raw connection
     * @deprecated Connections are borrowed from the pool per operation; do not share this field.
     */
    @Deprecated
    private Connection rawConnection;

    /**
     * A map of version-keyed ALTER statements to apply to the database.
     *
     * @param alterMap the alter map to set
     * @return the alter map
     */
    private ConcurrentSkipListMap<String, String> alterMap;

    /**
     * Whether this operator is currently usable for database operations.
     *
     * @param usable the usable flag to set
     * @return true if this operator is usable
     */
    private boolean usable;

    /**
     * The timestamp of the last database connection made by this operator.
     *
     * @param lastConnection the last connection date to set
     * @return the last connection date
     */
    private Date lastConnection;

    /**
     * Constructs a new DBOperator with the given connector set and plugin.
     * Automatically builds the data source and registers with DatabaseUtils.
     *
     * @param connectorSet the connection configuration
     * @param pluginUser   the plugin that owns this database operator
     */
    public DBOperator(ConnectorSet connectorSet, BetterPlugin pluginUser) {
        this.id = DatabaseUtils.getNextId(pluginUser);
        this.connectorSet = connectorSet;
        this.pluginUser = pluginUser;
        this.alterMap = new ConcurrentSkipListMap<>();
        this.usable = false;

        ensureFile();
        this.dataSource = buildDataSource();

        register();
    }

    /**
     * Registers this operator with the DatabaseUtils registry.
     */
    public void register() {
        DatabaseUtils.put(getPluginUser(), this);
    }

    /**
     * Unregisters this operator from the DatabaseUtils registry.
     */
    public void unregister() {
        DatabaseUtils.remove(getPluginUser(), this);
    }

    /**
     * Checks whether this operator is registered in the DatabaseUtils registry.
     *
     * @return true if this operator is registered
     */
    public boolean isRegistered() {
        return DatabaseUtils.has(getPluginUser(), this);
    }

    /**
     * Returns a unique identifier string combining the plugin identifier and this operator's ID.
     *
     * @return the identifier string
     */
    public String getIdentifier() {
        return pluginUser.getIdentifier() + " - " + id;
    }

    /**
     * Builds and configures a HikariCP data source based on the connector set configuration.
     * Configures connection pooling, timeouts, and driver settings for the appropriate database type.
     *
     * @return the configured HikariDataSource, or {@code null} if construction fails
     */
    public HikariDataSource buildDataSource() {
        if (connectorSet == null || connectorSet.getType() == null) {
            setUnusable();
            if (pluginUser != null) {
                pluginUser.logSevere("Cannot build data source: connector set or type is null.");
            }
            return null;
        }

        try {
            ensureFile();

            HikariConfig config = new HikariConfig();
            DatabaseType type = connectorSet.getType();

            String jdbcUrl = connectorSet.buildJdbcUrl(getDatabaseFolder());
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName(type.getDriver());
            config.setPoolName(getIdentifier() + " - Pool");

            if (type == DatabaseType.MYSQL) {
                config.setUsername(connectorSet.getUsername());
                config.setPassword(connectorSet.getPassword());
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
            }

            config.setMaximumPoolSize(type.recommendedMaxPoolSize());
            config.setMinimumIdle(type.recommendedMinimumIdle());
            config.setConnectionTimeout(30_000);
            config.setIdleTimeout(600_000);
            config.setMaxLifetime(1_800_000);
            config.setLeakDetectionThreshold(60_000);
            // Prefer JDBC4 Connection.isValid() (Hikari default) over a custom test query.

            HikariDataSource source = new HikariDataSource(config);
            this.dataSource = source;
            setUsable();
            return source;
        } catch (Throwable t) {
            this.dataSource = null;
            setUnusable();
            if (pluginUser != null) {
                pluginUser.logSevereWithInfo("Failed to build data source for " + getPrettyName() + "!", t);
            }
            return null;
        }
    }

    /**
     * Updates the last connection timestamp to the current time.
     */
    public void updateLastConnection() {
        lastConnection = new Date();
    }

    /**
     * Whether the pool is open and this operator is marked usable.
     *
     * @return {@code true} if connections can be borrowed
     */
    public boolean isPoolOpen() {
        return usable && dataSource != null && !dataSource.isClosed();
    }

    /**
     * Borrows a database connection from the pool.
     * Rebuilds the data source if it is missing or closed.
     * The unused {@code qStart} parameter is retained for API compatibility.
     *
     * <p>Callers <strong>must</strong> close the returned connection (try-with-resources).</p>
     *
     * @param qStart retained for API compatibility; unused
     * @return a database Connection, or null if an error occurs
     */
    public Connection getConnection(@Nullable Date qStart) {
        return getConnection();
    }

    /**
     * Borrows a database connection from the pool.
     *
     * <p>Callers <strong>must</strong> close the returned connection (try-with-resources).</p>
     *
     * @return a database Connection, or null if an error occurs
     */
    public Connection getConnection() {
        try {
            if (!isPoolOpen()) {
                dataSource = buildDataSource();
            }
            if (dataSource == null || dataSource.isClosed()) {
                return null;
            }

            Connection connection = dataSource.getConnection();
            updateLastConnection();
            return connection;
        } catch (Exception e) {
            if (pluginUser != null) {
                pluginUser.logSevereWithInfo("Failed to get connection!", e);
            }
            return null;
        }
    }

    /**
     * Marks this operator as usable.
     */
    public void setUsable() {
        this.usable = true;
    }

    /**
     * Marks this operator as unusable.
     */
    public void setUnusable() {
        this.usable = false;
    }

    /**
     * Returns a human-readable name for this operator, defaulting to the identifier.
     *
     * @return the pretty name string
     */
    public String getPrettyName() {
        return getIdentifier();
    }

    /**
     * Shuts down this database operator by waiting for the cooldown period to elapse
     * and then performing the threaded shutdown procedure.
     */
    public void shutdown() {
        awaitShutdown(DBOperator::threadedShutdown).join().accept(this);
    }

    /**
     * Best-effort commit helper retained for API compatibility.
     * With pooled auto-commit connections this is usually a no-op; safe to call on shutdown.
     */
    public void forceCommit() {
        if (!isPoolOpen()) return;

        try (Connection connection = getConnection()) {
            if (connection == null || connection.getAutoCommit()) return;
            connection.commit();
        } catch (Exception e) {
            if (pluginUser != null) {
                pluginUser.logSevereWithInfo("Failed to force-commit connection!", e);
            }
        }
    }

    /**
     * Performs a threaded shutdown of the given operator: closes the data source,
     * unregisters, and marks as unusable.
     *
     * @param operator the DBOperator to shut down
     */
    public static void threadedShutdown(DBOperator operator) {
        if (operator == null || !operator.isUsable()) return;

        BetterPlugin plugin = operator.getPluginUser();
        if (plugin != null) {
            plugin.logInfo("Shutting down database connection (" + operator.getPrettyName() + ")...");
        }

        try {
            operator.forceCommit();
        } catch (Throwable ignored) {
            // Best-effort only.
        }

        HikariDataSource source = operator.getDataSource();
        if (source != null) {
            try {
                if (!source.isClosed()) {
                    source.close();
                }
            } catch (Throwable t) {
                if (plugin != null) {
                    plugin.logWarning("Failed to close data source for " + operator.getPrettyName() + ": " + t.getMessage());
                }
            }
            operator.setDataSource(null);
        }

        operator.setRawConnection(null);
        operator.unregister();
        operator.setUnusable();

        if (plugin != null) {
            plugin.logInfo("Database connection (" + operator.getPrettyName() + ") has been shut down.");
        }
    }

    /**
     * Checks whether the cooldown period has elapsed since the last connection.
     *
     * @return true if the cooldown has passed or no connection has been made yet
     */
    public boolean isPastCooldown() {
        if (lastConnection == null) return true;
        return System.currentTimeMillis() - lastConnection.getTime() >= COOLDOWN_MILLIS;
    }

    /**
     * Returns a future that completes with the given consumer once the cooldown period has elapsed.
     * Times out after 10 seconds if the cooldown is not reached.
     *
     * @param whenDone the consumer to invoke after the cooldown
     * @return a CompletableFuture that resolves to the provided consumer
     */
    public CompletableFuture<Consumer<DBOperator>> awaitShutdown(Consumer<DBOperator> whenDone) {
        return CompletableFuture.supplyAsync(() -> {
            while (!isPastCooldown()) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return whenDone;
        }).completeOnTimeout(whenDone, 10, TimeUnit.SECONDS);
    }

    /**
     * Adds an ALTER statement to the alter map, keyed by version.
     *
     * @param version   the version identifier for this alteration
     * @param statement the SQL ALTER statement
     */
    public void addAlter(String version, String statement) {
        if (version == null || statement == null) return;
        alterMap.put(version, statement);
    }

    /**
     * Removes an ALTER statement from the alter map by version.
     *
     * @param version the version identifier of the alteration to remove
     */
    public void removeAlter(String version) {
        if (version == null) return;
        alterMap.remove(version);
    }

    /**
     * Gets the database type from the connector set.
     *
     * @return the DatabaseType
     */
    public DatabaseType getType() {
        return connectorSet == null ? null : connectorSet.getType();
    }

    /**
     * Executes a single SQL statement with the given statement builder.
     *
     * @param statement        the SQL statement to execute
     * @param statementBuilder a consumer that configures the PreparedStatement parameters
     * @param ignoreErrors     if true, errors are silently ignored; otherwise they are logged
     * @return YES if the statement returned a result set, NO if it did not, or ERROR on failure
     */
    public ExecutionResult executeSingle(String statement, Consumer<PreparedStatement> statementBuilder, boolean ignoreErrors) {
        AtomicReference<ExecutionResult> result = new AtomicReference<>(ExecutionResult.ERROR);

        if (!isPoolOpen() && buildDataSource() == null) {
            return ExecutionResult.ERROR;
        }

        String sql = normalizeStatement(statement);
        if (sql == null) return ExecutionResult.ERROR;

        Consumer<PreparedStatement> binder = statementBuilder == null ? NO_PARAMS : statementBuilder;

        try (Connection connection = getConnection()) {
            if (connection == null) return ExecutionResult.ERROR;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                binder.accept(stmt);
                if (stmt.execute()) {
                    result.set(ExecutionResult.YES);
                } else {
                    result.set(ExecutionResult.NO);
                }
            }
        } catch (Exception e) {
            if (!ignoreErrors && pluginUser != null) {
                pluginUser.logSevereWithInfo("Failed to execute statement: " + sql, e);
            }
        }

        return result.get();
    }

    /**
     * Executes one or more SQL statements separated by ";;", logging errors.
     *
     * @param statement        the SQL statement(s) to execute, separated by ";;"
     * @param statementBuilder a consumer that configures each PreparedStatement
     * @return a list of ExecutionResult values, one per statement
     */
    public List<ExecutionResult> execute(String statement, Consumer<PreparedStatement> statementBuilder) {
        return execute(statement, statementBuilder, false);
    }

    /**
     * Executes one or more SQL statements separated by ";;".
     *
     * @param statement        the SQL statement(s) to execute, separated by ";;"
     * @param statementBuilder a consumer that configures each PreparedStatement
     * @param ignoreErrors     if true, errors are silently ignored; otherwise they are logged
     * @return a list of ExecutionResult values, one per statement
     */
    public List<ExecutionResult> execute(String statement, Consumer<PreparedStatement> statementBuilder, boolean ignoreErrors) {
        List<ExecutionResult> results = new ArrayList<>();
        if (statement == null || statement.isBlank()) return results;

        String[] statements = statement.split(";;");
        for (String part : statements) {
            String sql = normalizeStatement(part);
            if (sql == null) continue;
            results.add(executeSingle(sql, statementBuilder, ignoreErrors));
        }

        return results;
    }

    /**
     * Executes a SQL query and passes the result set to the given action, logging errors.
     * The {@link ResultSet} is only valid inside {@code action}; it is closed afterward.
     *
     * @param statement        the SQL query to execute
     * @param statementBuilder a consumer that configures the PreparedStatement parameters
     * @param action           the action to perform on the resulting ResultSet
     */
    public void executeQuery(String statement, Consumer<PreparedStatement> statementBuilder, DBAction action) {
        executeQuery(statement, statementBuilder, action, false);
    }

    /**
     * Executes a SQL query and passes the result set to the given action.
     * The {@link ResultSet} is only valid inside {@code action}; it is closed afterward.
     *
     * @param statement        the SQL query to execute
     * @param statementBuilder a consumer that configures the PreparedStatement parameters
     * @param action           the action to perform on the resulting ResultSet
     * @param ignoreErrors     if true, errors are silently ignored; otherwise they are logged
     */
    public void executeQuery(String statement, Consumer<PreparedStatement> statementBuilder, DBAction action, boolean ignoreErrors) {
        if (action == null) return;

        if (!isPoolOpen() && buildDataSource() == null) {
            return;
        }

        String sql = normalizeStatement(statement);
        if (sql == null) return;

        Consumer<PreparedStatement> binder = statementBuilder == null ? NO_PARAMS : statementBuilder;

        try (Connection connection = getConnection()) {
            if (connection == null) return;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                binder.accept(stmt);
                try (ResultSet set = stmt.executeQuery()) {
                    action.accept(set);
                }
            }
        } catch (Exception e) {
            if (!ignoreErrors && pluginUser != null) {
                pluginUser.logSevereWithInfo("Failed to execute query: " + sql, e);
            }
        }
    }

    /**
     * Creates the SQLite database file if the database type is SQLITE and the file does not exist.
     */
    public void createSqliteFileIfNotExists() {
        if (connectorSet == null || connectorSet.getType() != DatabaseType.SQLITE) return;
        if (!connectorSet.hasSqliteFile()) return;

        File file = new File(getDatabaseFolder(), connectorSet.getSqliteFileName());
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            if (pluginUser != null) {
                pluginUser.logWarning("Failed to create SQLite parent folder: " + parent.getAbsolutePath());
            }
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile() && pluginUser != null) {
                    pluginUser.logWarning("Failed to create SQLite file: " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                if (pluginUser != null) {
                    pluginUser.logSevereWithInfo("Failed to create SQLite file: " + file.getAbsolutePath(), e);
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the database storage folder for this operator.
     *
     * @return the database folder
     */
    public File getDatabaseFolder() {
        return getDatabaseFolder(this);
    }

    /**
     * Ensures the SQLite file exists if the database type is SQLITE and a file name is configured.
     */
    public void ensureFile() {
        if (connectorSet == null || connectorSet.getType() != DatabaseType.SQLITE) return;
        if (!connectorSet.hasSqliteFile()) return;
        createSqliteFileIfNotExists();
    }

    /**
     * Ensures that all required database tables exist.
     * Subclasses must implement this method to create their specific tables.
     */
    public abstract void ensureTables();

    /**
     * Ensures that the database itself exists.
     * Subclasses must implement this method for database creation logic.
     */
    public abstract void ensureDatabase();

    /**
     * Executes all registered ALTER statements from the alter map, ignoring errors.
     */
    public void alterTables() {
        if (alterMap == null || alterMap.isEmpty()) return;

        alterMap.forEach((version, statement) -> {
            if (version == null || version.isBlank()) return;
            if (statement == null || statement.isBlank()) return;
            execute(statement, NO_PARAMS, true);
        });
    }

    /**
     * Ensures the database is fully ready by creating the file (if SQLite),
     * the database, all tables, and applying any ALTER statements.
     */
    public void ensureUsable() {
        this.ensureFile();
        if (!isPoolOpen()) {
            this.dataSource = buildDataSource();
        }
        this.ensureDatabase();
        this.ensureTables();
        this.alterTables();
    }

    @Override
    public int compareTo(@NotNull DBOperator o) {
        return Long.compare(this.id, o.id);
    }

    /**
     * Gets the database storage folder for the given operator, creating it if it does not exist.
     *
     * @param operator the operator whose plugin data folder to use
     * @return the storage folder
     */
    public static File getDatabaseFolder(DBOperator operator) {
        File folder = new File(operator.getPluginUser().getDataFolder(), "storage");
        if (!folder.exists() && !folder.mkdirs() && operator.getPluginUser() != null) {
            operator.getPluginUser().logWarning("Failed to create database folder: " + folder.getAbsolutePath());
        }
        return folder;
    }

    /**
     * Gets the main database storage folder for the base plugin instance, creating it if it does not exist.
     *
     * @return the main storage folder
     */
    public static File getMainDatabaseFolder() {
        File folder = new File(BaseManager.getBaseInstance().getDataFolder(), "storage");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    @Nullable
    private static String normalizeStatement(@Nullable String statement) {
        if (statement == null) return null;
        String sql = statement.trim();
        if (sql.isEmpty()) return null;
        // Keep a trailing semicolon for drivers/tools that expect it; harmless for most JDBC drivers.
        if (!sql.endsWith(";")) {
            sql = sql + ";";
        }
        return sql;
    }

    /**
     * Quietly closes an AutoCloseable, swallowing exceptions.
     *
     * @param closeable resource to close
     */
    public static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception ignored) {
            // Intentionally ignored.
        }
    }

    /**
     * Quietly closes JDBC resources in order: result set, statement, connection.
     *
     * @param resultSet  result set to close
     * @param statement  statement to close
     * @param connection connection to close (returns it to the pool when using Hikari)
     */
    public static void closeQuietly(@Nullable ResultSet resultSet, @Nullable Statement statement, @Nullable Connection connection) {
        closeQuietly(resultSet);
        closeQuietly(statement);
        closeQuietly(connection);
    }
}
