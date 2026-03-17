package host.plas.bou.commands;

/**
 * Holds constant boolean values representing command execution results.
 */
public class CommandResult {
    /**
     * Private constructor to prevent instantiation.
     */
    private CommandResult() {
    }

    /** Indicates a successful command execution. */
    public static final boolean SUCCESS = true;
    /** Indicates a failed command execution. */
    public static final boolean FAILURE = false;
    /** Indicates a command execution that resulted in an error. */
    public static final boolean ERROR = false;
}
