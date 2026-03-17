package host.plas.bou.compat;

import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.Identifiable;

import java.util.function.Supplier;

/**
 * Wraps an {@link ApiHolder} with lifecycle management.
 * Attempts to create the API holder on construction and tracks
 * whether the creation was successful via the enabled flag.
 */
@Getter @Setter
public class HeldHolder implements Identifiable {
    /**
     * The unique identifier for this held holder.
     *
     * @param identifier the unique identifier to set
     * @return the unique identifier
     */
    private String identifier;
    /**
     * The supplier used to create the underlying API holder.
     *
     * @param supplier the API holder supplier to set
     * @return the API holder supplier
     */
    private Supplier<ApiHolder<?>> supplier;
    /**
     * The underlying API holder instance, or null if creation failed.
     *
     * @param holder the API holder to set
     * @return the API holder instance
     */
    private ApiHolder<?> holder;
    /**
     * Whether the API holder was successfully created and is enabled.
     *
     * @param enabled true if the holder is enabled
     * @return true if the holder is enabled
     */
    private boolean enabled;

    /**
     * Constructs a new HeldHolder with the given identifier and API holder supplier.
     * Attempts to create the API holder immediately; sets enabled to true on success,
     * or false if an exception occurs.
     *
     * @param identifier the unique identifier for this held holder
     * @param supplier the supplier that creates the underlying API holder
     */
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
}
