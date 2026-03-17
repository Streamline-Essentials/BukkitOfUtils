package host.plas.bou.compat.luckperms;

import host.plas.bou.compat.ApiHolder;

import java.util.function.Supplier;

/**
 * Supplier that creates {@link LPHolder} instances for the LuckPerms integration.
 */
public class LPHolderCreator implements Supplier<ApiHolder<?>> {
    /**
     * Constructs a new LPHolderCreator instance.
     */
    public LPHolderCreator() {
        // default constructor
    }

    /**
     * Creates and returns a new {@link LPHolder} instance.
     *
     * @return a new LPHolder
     */
    @Override
    public LPHolder get() {
        return new LPHolder();
    }
}
