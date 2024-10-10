package host.plas.bou.compat.papi;

import host.plas.bou.compat.ApiHolder;

import java.util.function.Supplier;

public class PAPIHolderCreator implements Supplier<ApiHolder<?>> {
    @Override
    public PAPIHolder get() {
        return new PAPIHolder();
    }
}
