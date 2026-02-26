package host.plas.bou.helpful.data;

import host.plas.bou.text.TextDocument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Helpful implements HelpfulIdentified {
    private HelpfulInfo info;
    private TextDocument document;

    public Helpful(HelpfulInfo info, TextDocument document) {
        this.info = info;
        this.document = document;
    }

    public Helpful(HelpfulInfo info, String... lines) {
        this(info, TextDocument.ofLines(lines));
    }

    public Helpful(String identifier, String prettyName, String stylizedName, TextDocument document) {
        this(HelpfulInfo.of(identifier, prettyName, stylizedName), document);
    }

    public Helpful(String identifier, String prettyName, String stylizedName, String... lines) {
        this(HelpfulInfo.of(identifier, prettyName, stylizedName), TextDocument.ofLines(lines));
    }

    public void send(CommandSender sender) {
        this.getDocument().readAllTo(sender);
    }

    public HelpfulGui asGui() {
        return HelpfulGui.of(this);
    }

    public void sendGui(Player player) {
        this.asGui().open(player);
    }

    public void setInfo(final HelpfulInfo info) {
        this.info = info;
    }

    public void setDocument(final TextDocument document) {
        this.document = document;
    }

    public HelpfulInfo getInfo() {
        return this.info;
    }

    public TextDocument getDocument() {
        return this.document;
    }
}
