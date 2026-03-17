package host.plas.bou.compat.luckperms;

import host.plas.bou.compat.ApiHolder;
import host.plas.bou.compat.CompatManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

/**
 * An API holder for the LuckPerms API.
 * Retrieves the LuckPerms API instance via {@link LuckPermsProvider#get()}.
 */
public class LPHolder extends ApiHolder<LuckPerms> {
    /**
     * Constructs a new LPHolder that retrieves the LuckPerms API from the provider.
     */
    public LPHolder() {
        super(CompatManager.LP_IDENTIFIER, (v) -> LuckPermsProvider.get());
    }
}
