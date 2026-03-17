package host.plas.bou.helpful.data;

import gg.drak.thebase.objects.Identifiable;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds metadata for a helpful entry, including an identifier,
 * a human-readable pretty name, and a stylized display name.
 */
@Setter
@Getter
public class HelpfulInfo implements Identifiable {
    /**
     * The unique identifier for this helpful info entry.
     *
     * @param identifier the unique identifier to set
     * @return the unique identifier
     */
    private String identifier;
    /**
     * The human-readable pretty name for display purposes.
     *
     * @param prettyName the pretty name to set
     * @return the pretty name
     */
    private String prettyName;
    /**
     * The stylized display name, typically with color codes or formatting.
     *
     * @param stylizedName the stylized name to set
     * @return the stylized name
     */
    private String stylizedName;

    /**
     * Creates a new HelpfulInfo with the given identifier and names.
     *
     * @param identifier   the unique identifier
     * @param prettyName   the human-readable name
     * @param stylizedName the stylized display name
     * @return a new HelpfulInfo instance
     */
    public static HelpfulInfo of(String identifier, String prettyName, String stylizedName) {
        return new HelpfulInfo(identifier, prettyName, stylizedName);
    }

    /**
     * Constructs a new HelpfulInfo with the specified identifier and names.
     *
     * @param identifier   the unique identifier
     * @param prettyName   the human-readable name
     * @param stylizedName the stylized display name
     */
    public HelpfulInfo(String identifier, String prettyName, String stylizedName) {
        this.identifier = identifier;
        this.prettyName = prettyName;
        this.stylizedName = stylizedName;
    }
}
