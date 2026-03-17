package host.plas.bou.sql;

import lombok.Getter;
import lombok.Setter;

/**
 * A class that holds the information for a database connection.
 */
@Getter @Setter
public class ConnectorSet {
    /**
     * The database type (MYSQL or SQLITE).
     *
     * @param type the database type to set
     * @return the database type
     */
    private DatabaseType type;

    /**
     * The database host address.
     *
     * @param host the host address to set
     * @return the host address
     */
    private String host;
    /**
     * The database port number.
     *
     * @param port the port number to set
     * @return the port number
     */
    private int port;
    /**
     * The database name.
     *
     * @param database the database name to set
     * @return the database name
     */
    private String database;
    /**
     * The username for database authentication.
     *
     * @param username the username to set
     * @return the username
     */
    private String username;
    /**
     * The password for database authentication.
     *
     * @param password the password to set
     * @return the password
     */
    private String password;
    /**
     * The prefix to use for table names.
     *
     * @param tablePrefix the table prefix to set
     * @return the table prefix
     */
    private String tablePrefix;

    /**
     * The SQLite file name (used only for SQLITE type).
     *
     * @param sqliteFileName the SQLite file name to set
     * @return the SQLite file name
     */
    private String sqliteFileName;

    /**
     * Constructs a new ConnectorSet with all connection parameters.
     *
     * @param type           the database type (MYSQL or SQLITE)
     * @param host           the database host address
     * @param port           the database port number
     * @param database       the database name
     * @param username       the username for authentication
     * @param password       the password for authentication
     * @param tablePrefix    the prefix to use for table names
     * @param sqliteFileName the SQLite file name (used only for SQLITE type)
     */
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

    /**
     * Builds the JDBC connection URI based on the database type and connection parameters.
     *
     * @return the JDBC URI string, or an empty string for unsupported types
     */
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
}
