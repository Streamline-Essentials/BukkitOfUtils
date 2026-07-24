package host.plas.bou.sql;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Holds connection configuration for a MySQL or SQLite database.
 */
@Getter
@Setter
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
     * For SQLite, prefer {@link #buildJdbcUrl(File)} so the file path is absolute.
     *
     * @return the JDBC URI string, or an empty string for unsupported types
     */
    public String getUri() {
        if (type == null) return "";
        switch (type) {
            case MYSQL:
                return type.getUrlPrefix() + nullToEmpty(host) + ":" + port + "/" + nullToEmpty(database);
            case SQLITE:
                return type.getUrlPrefix();
            default:
                return "";
        }
    }

    /**
     * Builds a full JDBC URL, resolving SQLite against the given folder.
     *
     * @param sqliteFolder folder containing the SQLite file (ignored for MySQL)
     * @return JDBC URL string
     */
    public String buildJdbcUrl(@Nullable File sqliteFolder) {
        if (type == null) return "";
        switch (type) {
            case MYSQL:
                return appendMysqlParams(getUri());
            case SQLITE:
                if (sqliteFolder == null || sqliteFileName == null || sqliteFileName.isBlank()) {
                    return type.getUrlPrefix();
                }
                File dbFile = new File(sqliteFolder, sqliteFileName);
                return type.getUrlPrefix() + dbFile.getAbsolutePath();
            default:
                return "";
        }
    }

    /**
     * Applies a table prefix to a bare table name when a prefix is configured.
     *
     * @param tableName bare table name
     * @return prefixed table name, or the original when no prefix is set
     */
    public String prefixTable(String tableName) {
        if (tableName == null) return null;
        if (tablePrefix == null || tablePrefix.isBlank()) return tableName;
        return tablePrefix + tableName;
    }

    /**
     * Whether this connector is configured for SQLite with a usable file name.
     *
     * @return {@code true} when type is SQLITE and a non-blank file name is set
     */
    public boolean hasSqliteFile() {
        return type == DatabaseType.SQLITE && sqliteFileName != null && !sqliteFileName.isBlank();
    }

    private static String appendMysqlParams(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) return "";
        StringBuilder url = new StringBuilder(jdbcUrl);
        if (!jdbcUrl.contains("?")) {
            url.append('?');
        } else if (!jdbcUrl.endsWith("&") && !jdbcUrl.endsWith("?")) {
            url.append('&');
        }
        if (!jdbcUrl.contains("autoReconnect=")) {
            url.append("autoReconnect=true&");
        }
        if (!jdbcUrl.contains("useSSL=")) {
            // Keep older MySQL drivers happy without forcing SSL for local/plugin use.
            url.append("useSSL=false&");
        }
        if (!jdbcUrl.contains("allowPublicKeyRetrieval=")) {
            url.append("allowPublicKeyRetrieval=true&");
        }
        // Trim trailing separator
        char last = url.charAt(url.length() - 1);
        if (last == '&' || last == '?') {
            url.deleteCharAt(url.length() - 1);
        }
        return url.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
