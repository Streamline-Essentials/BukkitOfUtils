package host.plas.bou.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import host.plas.bou.BetterPlugin;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.DatabaseUtils;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
 */
@Getter @Setter
public abstract class DBOperator implements Comparable<DBOperator> {
    /** The cooldown period in milliseconds between shutdown checks (2 seconds). */
    public static final long COOLDOWN_MILLIS = 1000 * 2; // 2 seconds

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
     * The raw JDBC connection currently held by this operator.
     *
     * @param rawConnection the raw connection to set
     * @return the raw connection
     */
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
     * @return the configured HikariDataSource
     */
    public HikariDataSource buildDataSource() {
        HikariConfig config = new HikariConfig();

        switch (connectorSet.getType()) {
            case MYSQL:
                String mysqlJdbcUrl = connectorSet.getUri();
                if (! mysqlJdbcUrl.contains("?")) {
                    mysqlJdbcUrl += "?autoReconnect=true";
                } else {
                    mysqlJdbcUrl += "&autoReconnect=true";
                }

                config.setJdbcUrl(mysqlJdbcUrl);
                config.setUsername(connectorSet.getUsername());
                config.setPassword(connectorSet.getPassword());

                break;
            case SQLITE:
                config.setJdbcUrl(connectorSet.getUri() + getDatabaseFolder().getPath() + File.separator + connectorSet.getSqliteFileName());

                break;
        }
        config.setPoolName(getIdentifier() + " - Pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setDriverClassName(connectorSet.getType().getDriver());
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);

        setUsable();

        return dataSource;
    }

    /**
     * Updates the last connection timestamp to the current time.
     */
    public void updateLastConnection() {
        lastConnection = new Date();
    }

    /**
     * Gets a database connection, reusing an existing open connection if available.
     * Rebuilds the data source if it is null.
     *
     * @param qStart the timestamp of the query start (used for connection tracking)
     * @return a database Connection, or null if an error occurs
     */
    public Connection getConnection(Date qStart) {
        try {
            if (dataSource == null) {
                dataSource = buildDataSource();
            }

//            Connection rawConnection = getConnectionMap().get(qStart);

            if (rawConnection != null && !rawConnection.isClosed()) {
                updateLastConnection();
                return rawConnection;
            }

            rawConnection = dataSource.getConnection();

            updateLastConnection();
            return rawConnection;
        } catch (Exception e) {
            getPluginUser().logSevereWithInfo("Failed to get connection!", e);
            return null;
        }
    }

    /**
     * Gets a database connection using the current time as the query start.
     *
     * @return a database Connection, or null if an error occurs
     */
    public Connection getConnection() {
        return getConnection(new Date()); // TODO: Fix this
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
     * Forces a commit on the current connection by disabling auto-commit and committing.
     */
    public void forceCommit() {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            connection.commit();
        } catch (Exception e) {
            getPluginUser().logSevereWithInfo("Failed to close connection!", e);
        }
    }

    /**
     * Performs a threaded shutdown of the given operator: commits pending changes,
     * closes the data source, unregisters, and marks as unusable.
     *
     * @param operator the DBOperator to shut down
     */
    public static void threadedShutdown(DBOperator operator) {
        if (! operator.isUsable()) return;

        operator.getPluginUser().logInfo("Shutting down database connection (" + operator.getPrettyName() + ")...");

        if (operator.getDataSource() != null) {
            operator.forceCommit();
            operator.getDataSource().close();
            operator.setDataSource(null);
        }

        operator.unregister();
        operator.setUnusable();

        operator.getPluginUser().logInfo("Database connection (" + operator.getPrettyName() + ") has been shut down.");
    }

    /**
     * Checks whether the cooldown period has elapsed since the last connection.
     *
     * @return true if the cooldown has passed or no connection has been made yet
     */
    public boolean isPastCooldown() {
        if (lastConnection == null) return true;

        return new Date().getTime() - lastConnection.getTime() >= COOLDOWN_MILLIS;
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
            while (! isPastCooldown()) {
                Thread.onSpinWait();
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
        alterMap.put(version, statement);
    }

    /**
     * Removes an ALTER statement from the alter map by version.
     *
     * @param version the version identifier of the alteration to remove
     */
    public void removeAlter(String version) {
        alterMap.remove(version);
    }

    /**
     * Gets the database type from the connector set.
     *
     * @return the DatabaseType
     */
    public DatabaseType getType() {
        return connectorSet.getType();
    }

    /**
     * Executes a single SQL statement with the given statement builder.
     *
     * @param statementBuilder a consumer that configures the PreparedStatement parameters
     * @param statement        the SQL statement to execute
     * @param ignoreErrors     if true, errors are silently ignored; otherwise they are logged
     * @return YES if the statement returned a result set, NO if it did not, or ERROR on failure
     */
    public ExecutionResult executeSingle(String statement, Consumer<PreparedStatement> statementBuilder, boolean ignoreErrors) {
        AtomicReference<ExecutionResult> result = new AtomicReference<>(ExecutionResult.ERROR);

        try {
            Date qStart = new Date();
            Connection connection = getConnection(qStart);
            PreparedStatement stmt = connection.prepareStatement(statement);

            statementBuilder.accept(stmt);

            if (stmt.execute()) result.set(ExecutionResult.YES);
            else result.set(ExecutionResult.NO);
        } catch (Exception e) {
            if (! ignoreErrors) getPluginUser().logSevereWithInfo("Failed to execute statement: " + statement, e);
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

        String[] statements = statement.split(";;");

        for (String s : statements) {
            if (s == null || s.isEmpty() || s.isBlank()) continue;
            String fs = s;
            if (!fs.endsWith(";")) fs += ";";
            results.add(executeSingle(fs, statementBuilder, ignoreErrors));
        }

        return results;
    }

    /**
     * Executes a SQL query and passes the result set to the given action, logging errors.
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
     *
     * @param statement        the SQL query to execute
     * @param statementBuilder a consumer that configures the PreparedStatement parameters
     * @param action           the action to perform on the resulting ResultSet
     * @param ignoreErrors     if true, errors are silently ignored; otherwise they are logged
     */
    public void executeQuery(String statement, Consumer<PreparedStatement> statementBuilder, DBAction action, boolean ignoreErrors) {
        try {
            Date qStart = new Date();
            Connection connection = getConnection(qStart);
            PreparedStatement stmt = connection.prepareStatement(statement);

            statementBuilder.accept(stmt);

            ResultSet set = stmt.executeQuery();

            action.accept(set);
        } catch (Exception e) {
            if (! ignoreErrors) getPluginUser().logSevereWithInfo("Failed to execute query: " + statement, e);
        }
    }

    /**
     * Creates the SQLite database file if the database type is SQLITE and the file does not exist.
     */
    public void createSqliteFileIfNotExists() {
        if (connectorSet.getType() != DatabaseType.SQLITE) return;

        File file = new File(getDatabaseFolder(), connectorSet.getSqliteFileName());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
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
        if (this.getConnectorSet().getType() != DatabaseType.SQLITE) return;

        String s1 = this.getConnectorSet().getSqliteFileName();
        if (s1 == null) return;
        if (s1.isBlank() || s1.isEmpty()) return;

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
        getAlterMap().forEach((version, statement) -> {
            if (version == null || version.isEmpty() || version.isBlank()) return;
            if (statement == null || statement.isEmpty() || statement.isBlank()) return;

            execute(statement, stmt -> {}, true);
        });
    }

    /**
     * Ensures the database is fully ready by creating the file (if SQLite),
     * the database, all tables, and applying any ALTER statements.
     */
    public void ensureUsable() {
        this.ensureFile();
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

        if (! folder.exists()) {
            folder.mkdirs();
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

        if (! folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }
}
