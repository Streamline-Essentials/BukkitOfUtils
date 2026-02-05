package host.plas.bou.sql;

/**
 * A class that holds the information for a database connection.
 */
public class ConnectorSet {
    private DatabaseType type;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String tablePrefix;
    private String sqliteFileName;

    public ConnectorSet(DatabaseType type, String host, int port, String database, String username, String password, String tablePrefix, String sqliteFileName) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
        this.sqliteFileName = sqliteFileName;
    }

    public String getUri() {
        switch (type) {
        case MYSQL: 
            return type.getUrlPrefix() + host + ":" + port + "/" + database;
        case SQLITE: 
            return type.getUrlPrefix();
        default: 
            return "";
        }
    }

    public DatabaseType getType() {
        return this.type;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getTablePrefix() {
        return this.tablePrefix;
    }

    public String getSqliteFileName() {
        return this.sqliteFileName;
    }

    public void setType(final DatabaseType type) {
        this.type = type;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setDatabase(final String database) {
        this.database = database;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setTablePrefix(final String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public void setSqliteFileName(final String sqliteFileName) {
        this.sqliteFileName = sqliteFileName;
    }
}
