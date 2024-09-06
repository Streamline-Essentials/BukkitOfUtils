package host.plas.bou.sql;

import host.plas.bou.BetterPlugin;
import host.plas.bou.events.self.plugin.PluginDisableEvent;
import host.plas.bou.instances.BaseManager;
import lombok.Getter;
import lombok.Setter;
import tv.quaint.thebase.lib.hikari.HikariConfig;
import tv.quaint.thebase.lib.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Getter @Setter
public abstract class DBOperator {
    private ConnectorSet connectorSet;
    private HikariDataSource dataSource;
    private BetterPlugin pluginUser;

    private Connection rawConnection;

    private ConcurrentSkipListMap<String, String> alterMap;

    public DBOperator(ConnectorSet connectorSet, BetterPlugin pluginUser) {
        this.connectorSet = connectorSet;
        this.pluginUser = pluginUser;

        this.alterMap = new ConcurrentSkipListMap<>();

        pluginUser.subscribeDisableIfSame(this::shutdown);

//        this.connectionMap = new ConcurrentSkipListMap<>();
//        this.connectionTimers = new ConcurrentSkipListMap<>();

        this.dataSource = buildDataSource();
    }

    public HikariDataSource buildDataSource() {
        HikariConfig config = new HikariConfig();

        switch (connectorSet.getType()) {
            case MYSQL:
                config.setJdbcUrl(connectorSet.getUri());
                config.setUsername(connectorSet.getUsername());
                config.setPassword(connectorSet.getPassword());

                break;
            case SQLITE:
                config.setJdbcUrl(connectorSet.getUri() + getDatabaseFolder().getPath() + File.separator + connectorSet.getSqliteFileName());

                break;
        }
        config.setPoolName(pluginUser.getIdentifier() + " - Pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setDriverClassName(connectorSet.getType().getDriver());
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);
        return dataSource;
    }

    public Connection getConnection(Date qStart) {
        try {
            if (dataSource == null) {
                dataSource = buildDataSource();
            }

//            Connection rawConnection = getConnectionMap().get(qStart);

            if (rawConnection != null && !rawConnection.isClosed()) {
                return rawConnection;
            }

            rawConnection = dataSource.getConnection();

            return rawConnection;
        } catch (Exception e) {
            getPluginUser().logSevereWithInfo("Failed to get connection!", e);
            return null;
        }
    }

    public Connection getConnection() {
        return getConnection(new Date()); // TODO: Fix this
    }

    public void shutdown(PluginDisableEvent event) {
        BetterPlugin plugin = event.getPlugin();

        plugin.logInfo("Shutting down database connection...");

        shutdown();

        plugin.logInfo("Shut down database connection!");
    }

    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();

            dataSource = null;
        }
    }

    public void addAlter(String version, String statement) {
        alterMap.put(version, statement);
    }

    public void removeAlter(String version) {
        alterMap.remove(version);
    }

    public DatabaseType getType() {
        return connectorSet.getType();
    }

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

    public List<ExecutionResult> execute(String statement, Consumer<PreparedStatement> statementBuilder) {
        return execute(statement, statementBuilder, false);
    }

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

    public void executeQuery(String statement, Consumer<PreparedStatement> statementBuilder, DBAction action) {
        executeQuery(statement, statementBuilder, action, false);
    }

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

    public File getDatabaseFolder() {
        return getDatabaseFolder(this);
    }

    public void ensureFile() {
        if (this.getConnectorSet().getType() != DatabaseType.SQLITE) return;

        String s1 = this.getConnectorSet().getSqliteFileName();
        if (s1 == null) return;
        if (s1.isBlank() || s1.isEmpty()) return;

        createSqliteFileIfNotExists();
    }

    public abstract void ensureTables();

    public abstract void ensureDatabase();

    public void alterTables() {
        getAlterMap().forEach((version, statement) -> {
            if (version == null || version.isEmpty() || version.isBlank()) return;
            if (statement == null || statement.isEmpty() || statement.isBlank()) return;

            execute(statement, stmt -> {});
        });
    }

    public void ensureUsable() {
        this.ensureFile();
        this.ensureDatabase();
        this.ensureTables();
        this.alterTables();
    }

    public static File getDatabaseFolder(DBOperator operator) {
        File folder = new File(operator.getPluginUser().getDataFolder(), "storage");

        if (! folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }

    public static File getMainDatabaseFolder() {
        File folder = new File(BaseManager.getBaseInstance().getDataFolder(), "storage");

        if (! folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }
}
