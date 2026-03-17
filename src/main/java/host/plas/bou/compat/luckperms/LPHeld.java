package host.plas.bou.compat.luckperms;

import host.plas.bou.compat.CompatManager;
import host.plas.bou.compat.HeldHolder;

/**
 * A held holder specifically for the LuckPerms integration.
 * Creates an {@link LPHolder} via the {@link LPHolderCreator} on construction.
 */
public class LPHeld extends HeldHolder {
    /**
     * Constructs a new LPHeld using the LuckPerms identifier and holder creator.
     */
    public LPHeld() {
        super(CompatManager.LP_IDENTIFIER, new LPHolderCreator());
    }
}
