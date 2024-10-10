package host.plas.bou.compat.papi;

import host.plas.bou.compat.CompatManager;
import host.plas.bou.compat.HeldHolder;

public class PAPIHeld extends HeldHolder {
    public PAPIHeld() {
        super(CompatManager.PAPI_IDENTIFIER, new PAPIHolderCreator());
    }
}
