package host.plas.bou.compat.papi;

import host.plas.bou.compat.CompatManager;
import host.plas.bou.compat.HeldHolder;

/**
 * A held holder specifically for the PlaceholderAPI integration.
 * Creates a {@link PAPIHolder} via the {@link PAPIHolderCreator} on construction.
 */
public class PAPIHeld extends HeldHolder {
    /**
     * Constructs a new PAPIHeld using the PlaceholderAPI identifier and holder creator.
     */
    public PAPIHeld() {
        super(CompatManager.PAPI_IDENTIFIER, new PAPIHolderCreator());
    }
}
