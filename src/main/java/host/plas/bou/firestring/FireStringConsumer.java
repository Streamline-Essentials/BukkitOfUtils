package host.plas.bou.firestring;

import java.util.function.Consumer;

/**
 * A functional interface that extends {@link Consumer} for processing fire string input.
 * Implementations define how a particular string payload should be handled when a
 * {@link FireString} is fired.
 */
public interface FireStringConsumer extends Consumer<String> {
}
