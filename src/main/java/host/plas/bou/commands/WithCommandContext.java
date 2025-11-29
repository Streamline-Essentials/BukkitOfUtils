package host.plas.bou.commands;

import java.util.function.Function;

public interface WithCommandContext<R> extends Function<CommandContext, R> {
    default boolean isEmpty() {
        return false;
    }
}
