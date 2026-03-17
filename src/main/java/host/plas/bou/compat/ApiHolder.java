package host.plas.bou.compat;

import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.Identifiable;

import java.util.Optional;
import java.util.function.Function;

/**
 * Abstract base class for holding an API reference, managing its availability
 * and lifecycle. Subclasses provide the specific API retrieval logic.
 *
 * @param <A> the type of the API being held
 */
@Getter @Setter
public abstract class ApiHolder<A> implements Identifiable {
    /**
     * Represents a tri-state boolean with TRUE, FALSE, and NULL values.
     */
    public enum ApiTristate {
        /** Represents a true state. */
        TRUE,
        /** Represents a false state. */
        FALSE,
        /** Represents an unknown or unset state. */
        NULL,
        ;
    }

    private String identifier;

    /**
     * The optional API instance, present if the API is available.
     * @param api the optional API instance to set
     * @return the optional API instance
     */
    private Optional<A> api;
    /**
     * The function used to retrieve the API instance.
     * @param getter the function to set for retrieving the API instance
     * @return the function used to retrieve the API instance
     */
    private Function<Void, A> getter;

    /**
     * Constructs a new ApiHolder with the given identifier and API getter function,
     * then attempts to enable the API immediately.
     *
     * @param identifier the unique identifier for this API holder
     * @param getter the function used to retrieve the API instance
     */
    public ApiHolder(String identifier, Function<Void, A> getter) {
        this.identifier = identifier;
        this.getter = getter;

        tryEnable();
    }

    /**
     * Attempts to retrieve the API instance using the getter function.
     * Returns null if any error occurs during retrieval.
     *
     * @return the API instance, or null if retrieval fails
     */
    public A checkApi() {
        try {
            return getter.apply(null);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Attempts to enable the API by checking its availability.
     * Sets the internal API optional to present if the API is available,
     * or empty if it is not.
     */
    public void tryEnable() {
        A api = checkApi();
        if (api != null) {
            this.api = Optional.of(api);
        } else {
            this.api = Optional.empty();
        }
    }

    /**
     * Checks whether the API is currently available and enabled.
     *
     * @return true if the API is present, false otherwise
     */
    public boolean isEnabled() {
        return api.isPresent();
    }

    /**
     * Returns the API instance if available, or null if not.
     *
     * @return the API instance, or null if not present
     */
    public A api() {
        return api.orElse(null);
    }
}
