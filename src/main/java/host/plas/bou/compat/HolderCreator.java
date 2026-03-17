package host.plas.bou.compat;

import java.util.function.Supplier;

/**
 * Functional interface for creating {@link ApiHolder} instances.
 * Extends {@link Supplier} to provide a standardized way of constructing API holders.
 */
public interface HolderCreator extends Supplier<ApiHolder<?>> {
}
