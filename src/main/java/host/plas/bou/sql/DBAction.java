package host.plas.bou.sql;

import java.sql.ResultSet;
import java.util.function.Consumer;

/**
 * A functional interface representing an action to perform on a database ResultSet.
 * Extends Consumer to allow direct use as a lambda or method reference.
 */
public interface DBAction extends Consumer<ResultSet> {
}
