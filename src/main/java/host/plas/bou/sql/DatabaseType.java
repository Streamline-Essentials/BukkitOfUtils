package host.plas.bou.sql;

import lombok.Getter;

/**
 * Enumerates the supported database types with their JDBC URL prefixes and driver class names.
 */
@Getter
public enum DatabaseType {
    /** MySQL database type using MySQL Connector/J ({@code com.mysql:mysql-connector-j}). */
    MYSQL("jdbc:mysql://", "com.mysql.cj.jdbc.Driver"),
    /** SQLite database type using the SQLite JDBC driver. */
    SQLITE("jdbc:sqlite:", "org.sqlite.JDBC"),
    ;

    /**
     * The JDBC URL prefix for this database type.
     *
     * @return the JDBC URL prefix
     */
    private final String urlPrefix;
    /**
     * The fully qualified JDBC driver class name.
     *
     * @return the driver class name
     */
    private final String driver;

    /**
     * Constructs a DatabaseType with the given JDBC URL prefix and driver class name.
     *
     * @param urlPrefix the JDBC URL prefix for this database type
     * @param driver    the fully qualified JDBC driver class name
     */
    DatabaseType(String urlPrefix, String driver) {
        this.urlPrefix = urlPrefix;
        this.driver = driver;
    }

    /**
     * Recommended Hikari maximum pool size for this database type.
     * SQLite is file-locked and should generally use a single connection.
     *
     * @return recommended max pool size
     */
    public int recommendedMaxPoolSize() {
        return this == SQLITE ? 1 : 10;
    }

    /**
     * Recommended Hikari minimum idle connections for this database type.
     *
     * @return recommended minimum idle count
     */
    public int recommendedMinimumIdle() {
        return this == SQLITE ? 1 : 2;
    }
}
