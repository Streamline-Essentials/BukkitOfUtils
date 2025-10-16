package host.plas.bou.commands;

public interface CommandExecution extends WithCommandContext<Boolean> {
    static CommandExecution emptyFalse() {
        return ctx -> false;
    }

    static CommandExecution emptyTrue() {
        return ctx -> true;
    }
}
