package host.plas.bou.compat.luckperms;

import host.plas.bou.compat.ApiHolder;
import host.plas.bou.compat.CompatManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class LPHolder extends ApiHolder<LuckPerms> {
    public LPHolder() {
        super(CompatManager.LP_IDENTIFIER, (v) -> LuckPermsProvider.get());
    }
}
