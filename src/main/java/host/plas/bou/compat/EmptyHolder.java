package host.plas.bou.compat;

import lombok.Getter;
import lombok.Setter;

/**
 * A placeholder held holder that represents an unavailable or missing API.
 * Always has its enabled state set to false and provides a null API supplier.
 */
@Getter @Setter
public class EmptyHolder extends HeldHolder {
    /**
     * Constructs an empty holder with the given identifier.
     * The holder is immediately set to disabled.
     *
     * @param identifier the identifier for this empty holder
     */
    public EmptyHolder(String identifier) {
        super(identifier, () -> null);

        setEnabled(false);
    }
}
