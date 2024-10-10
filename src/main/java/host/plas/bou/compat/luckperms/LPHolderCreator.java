package host.plas.bou.compat.luckperms;

import host.plas.bou.compat.ApiHolder;

import java.util.function.Supplier;

public class LPHolderCreator implements Supplier<ApiHolder<?>> {
    @Override
    public LPHolder get() {
        return new LPHolder();
    }
}
