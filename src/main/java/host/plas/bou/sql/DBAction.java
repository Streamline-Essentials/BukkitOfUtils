package host.plas.bou.sql;

import java.sql.ResultSet;
import java.util.function.Consumer;

/**
 * A functional interface representing an action to perform on a database {@link ResultSet}.
 * The result set is only valid for the duration of {@link #accept(ResultSet)}; it is closed afterward.
 */
@FunctionalInterface
public interface DBAction extends Consumer<ResultSet> {
}
