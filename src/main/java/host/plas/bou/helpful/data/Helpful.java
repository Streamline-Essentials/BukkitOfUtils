package host.plas.bou.helpful.data;

import host.plas.bou.text.TextDocument;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Represents a helpful data object that combines metadata ({@link HelpfulInfo})
 * with a text document to provide help content to players.
 */
@Setter
@Getter
public class Helpful implements HelpfulIdentified {
    /**
     * The metadata containing identifier and display names for this helpful entry.
     *
     * @param info the helpful info to set
     * @return the current helpful info
     */
    private HelpfulInfo info;
    /**
     * The text document containing the help content lines.
     *
     * @param document the text document to set
     * @return the current text document
     */
    private TextDocument document;

    /**
     * Constructs a new Helpful with the given info and text document.
     *
     * @param info     the helpful metadata containing identifier and names
     * @param document the text document containing the help content
     */
    public Helpful(HelpfulInfo info, TextDocument document) {
        this.info = info;
        this.document = document;
    }

    /**
     * Constructs a new Helpful with the given info and text lines.
     *
     * @param info  the helpful metadata containing identifier and names
     * @param lines the lines of text to include in the help content
     */
    public Helpful(HelpfulInfo info, String... lines) {
        this(info, TextDocument.ofLines(lines));
    }

    /**
     * Constructs a new Helpful with explicit identifier, names, and a text document.
     *
     * @param identifier   the unique identifier for this helpful entry
     * @param prettyName   the human-readable name
     * @param stylizedName the stylized display name
     * @param document     the text document containing the help content
     */
    public Helpful(String identifier, String prettyName, String stylizedName, TextDocument document) {
        this(HelpfulInfo.of(identifier, prettyName, stylizedName), document);
    }

    /**
     * Constructs a new Helpful with explicit identifier, names, and text lines.
     *
     * @param identifier   the unique identifier for this helpful entry
     * @param prettyName   the human-readable name
     * @param stylizedName the stylized display name
     * @param lines        the lines of text to include in the help content
     */
    public Helpful(String identifier, String prettyName, String stylizedName, String... lines) {
        this(HelpfulInfo.of(identifier, prettyName, stylizedName), TextDocument.ofLines(lines));
    }

    /**
     * Sends the help content to a command sender.
     *
     * @param sender the command sender to receive the help text
     */
    public void send(CommandSender sender) {
        this.getDocument().readAllTo(sender);
    }

    /**
     * Creates a GUI representation of this helpful data.
     *
     * @return a new {@link HelpfulGui} wrapping this helpful instance
     */
    public HelpfulGui asGui() {
        return HelpfulGui.of(this);
    }

    /**
     * Opens a GUI displaying this help content for the specified player.
     *
     * @param player the player to open the GUI for
     */
    public void sendGui(Player player) {
        this.asGui().open(player);
    }

}
