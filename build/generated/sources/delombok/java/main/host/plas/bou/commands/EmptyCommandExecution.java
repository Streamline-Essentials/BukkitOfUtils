package host.plas.bou.commands;

public interface EmptyCommandExecution extends CommandExecution {
    @Override
    default boolean isEmpty() {
        return true;
    }
}
