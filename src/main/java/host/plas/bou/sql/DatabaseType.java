package host.plas.bou.sql;

import lombok.Getter;

/**
 * Enumerates the supported database types with their JDBC URL prefixes and driver class names.
 */
@Getter
public enum DatabaseType {
    /** MySQL database type using the MySQL Connector/J driver. */
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
}
