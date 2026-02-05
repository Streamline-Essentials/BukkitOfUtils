package host.plas.bou.sql;

import lombok.Getter;
import lombok.Setter;

/**
 * A class that holds the information for a database connection.
 */
@Getter @Setter
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
}
