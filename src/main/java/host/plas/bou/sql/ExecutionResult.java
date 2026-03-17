package host.plas.bou.sql;

/**
 * Represents the result of a SQL statement execution.
 * ERROR indicates a failure, YES indicates the statement returned a result set,
 * and NO indicates the statement did not return a result set.
 */
public enum ExecutionResult {
    /** Indicates that the SQL execution failed with an error. */
    ERROR,
    /** Indicates that the SQL statement returned a result set. */
    YES,
    /** Indicates that the SQL statement did not return a result set. */
    NO,
    ;
}
