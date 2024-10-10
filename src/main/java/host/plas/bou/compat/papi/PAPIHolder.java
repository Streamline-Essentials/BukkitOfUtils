package host.plas.bou.compat.papi;

import host.plas.bou.compat.ApiHolder;
import host.plas.bou.compat.CompatManager;
import me.clip.placeholderapi.PlaceholderAPIPlugin;

public class PAPIHolder extends ApiHolder<PlaceholderAPIPlugin> {
    public PAPIHolder() {
        super(CompatManager.PAPI_IDENTIFIER, (v) -> PlaceholderAPIPlugin.getInstance());
    }
}
