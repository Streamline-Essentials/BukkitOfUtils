package host.plas.bou.compat;

import gg.drak.thebase.objects.Identifiable;
import java.util.function.Supplier;

public class HeldHolder implements Identifiable {
    private String identifier;
    private Supplier<ApiHolder<?>> supplier;
    private ApiHolder<?> holder;
    private boolean enabled;

    public HeldHolder(String identifier, Supplier<ApiHolder<?>> supplier) {
        this.identifier = identifier;
        this.supplier = supplier;
        try {
            this.holder = supplier.get();
            this.enabled = true;
        } catch (Exception e) {
//            e.printStackTrace();
            this.holder = null;
            this.enabled = false;
        }
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public Supplier<ApiHolder<?>> getSupplier() {
        return this.supplier;
    }

    public ApiHolder<?> getHolder() {
        return this.holder;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public void setSupplier(final Supplier<ApiHolder<?>> supplier) {
        this.supplier = supplier;
    }

    public void setHolder(final ApiHolder<?> holder) {
        this.holder = holder;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
