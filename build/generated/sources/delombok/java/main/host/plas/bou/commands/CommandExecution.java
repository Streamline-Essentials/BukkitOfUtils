package host.plas.bou.commands;

public interface CommandExecution extends WithCommandContext<Boolean> {
    static EmptyCommandExecution emptyFalse() {
        return ctx -> false;
    }

    static EmptyCommandExecution emptyTrue() {
        return ctx -> true;
    }
}
