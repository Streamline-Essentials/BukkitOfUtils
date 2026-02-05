package host.plas.bou.compat.luckperms;

import host.plas.bou.compat.CompatManager;
import host.plas.bou.compat.HeldHolder;

public class LPHeld extends HeldHolder {
    public LPHeld() {
        super(CompatManager.LP_IDENTIFIER, new LPHolderCreator());
    }
}
