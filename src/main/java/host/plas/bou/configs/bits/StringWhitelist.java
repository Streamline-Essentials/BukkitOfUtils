package host.plas.bou.configs.bits;

/**
 * A configurable whitelist specialized for {@link String} elements.
 */
public class StringWhitelist extends ConfigurableWhitelist<String> {
    /**
     * Constructs a new StringWhitelist with the given identifier.
     *
     * @param identifier the unique identifier for this whitelist
     */
    public StringWhitelist(String identifier) {
        super(identifier);
    }
}
