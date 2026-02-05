package host.plas.bou.commands;

public interface EmptyTabCompleter extends CommandTabCompleter {
    @Override
    default boolean isEmpty() {
        return true;
    }
}
