package host.plas.bou.sql;

/**
 * Represents the result of a SQL statement execution.
 */
public enum ExecutionResult {
    /** Indicates that the SQL execution failed with an error. */
    ERROR,
    /** Indicates that the SQL statement returned a result set. */
    YES,
    /** Indicates that the SQL statement did not return a result set. */
    NO,
    ;

    /**
     * @return {@code true} when execution completed without error
     */
    public boolean isSuccess() {
        return this == YES || this == NO;
    }

    /**
     * @return {@code true} when execution failed
     */
    public boolean isError() {
        return this == ERROR;
    }
}
