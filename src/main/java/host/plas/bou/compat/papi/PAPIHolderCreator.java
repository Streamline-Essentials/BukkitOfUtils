package host.plas.bou.compat.papi;

import host.plas.bou.compat.ApiHolder;

import java.util.function.Supplier;

/**
 * Supplier that creates {@link PAPIHolder} instances for the PlaceholderAPI integration.
 */
public class PAPIHolderCreator implements Supplier<ApiHolder<?>> {
    /** Constructs a new PAPIHolderCreator. */
    public PAPIHolderCreator() {}

    /**
     * Creates and returns a new {@link PAPIHolder} instance.
     *
     * @return a new PAPIHolder
     */
    @Override
    public PAPIHolder get() {
        return new PAPIHolder();
    }
}
