package host.plas.bou.text;

import host.plas.bou.commands.Sender;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;

/**
 * Represents a single page of text composed of indexed lines,
 * stored in a sorted map by line index.
 */
@Setter
@Getter
public class TextPage {
    /**
     * A sorted map of line indices to line content for this page.
     *
     * @param lines the lines map to set
     * @return the lines map
     */
    private ConcurrentSkipListMap<Integer, String> lines;

    /**
     * Constructs a new TextPage with the given lines, replacing any existing lines.
     *
     * @param lines the lines to initialize this page with
     */
    public TextPage(String... lines) {
        this.asLines(lines);
    }

    /**
     * Replaces all existing lines with the given lines, clearing any previous content.
     *
     * @param lines the lines to set as the page content
     */
    public void asLines(String... lines) {
        this.ensureLines(true);

        for(int i = 0; i < lines.length; ++i) {
            this.lines.put(i, lines[i]);
        }
    }

    /**
     * Appends the given lines after the last existing line on this page.
     *
     * @param lines the lines to append
     */
    public void withLines(String... lines) {
        this.ensureLines();
        int startIndex = this.getLastLineIndex() + 1;

        for(int i = 0; i < lines.length; ++i) {
            this.lines.put(startIndex + i, lines[i]);
        }
    }

    /**
     * Returns the index of the last line on this page.
     *
     * @return the last line index, or -1 if the page is empty or null
     */
    public int getLastLineIndex() {
        return this.lines != null && !this.lines.isEmpty() ? this.lines.lastKey() : -1;
    }

    /**
     * Ensures that the lines map is initialized without clearing existing content.
     */
    public void ensureLines() {
        this.ensureLines(false);
    }

    /**
     * Ensures that the lines map is initialized, optionally clearing existing content.
     *
     * @param clear if true, clears the existing lines map; if false, only initializes if null
     */
    public void ensureLines(boolean clear) {
        if (this.lines == null) {
            this.lines = new ConcurrentSkipListMap();
        } else if (clear) {
            this.lines.clear();
        }
    }

    /**
     * Retrieves the line at the specified index.
     *
     * @param index the line index to retrieve
     * @return an Optional containing the line if found, or empty if not found or the page is empty
     */
    public Optional<String> getLine(int index) {
        return this.isEmpty() ? Optional.empty() : Optional.ofNullable(this.lines.get(index));
    }

    /**
     * Inserts a line at the specified index, replacing any existing line at that index.
     *
     * @param index the index to insert the line at
     * @param line  the line content to insert
     */
    public void insertLine(int index, String line) {
        this.ensureLines();
        this.lines.put(index, line);
    }

    /**
     * Checks whether this page has no lines.
     *
     * @return true if the lines map is null or empty
     */
    public boolean isEmpty() {
        return this.lines == null || this.lines.isEmpty();
    }

    /**
     * Reads all lines on this page to the specified command sender with default settings.
     *
     * @param sender the command sender to send the lines to
     */
    public void readTo(CommandSender sender) {
        this.readTo(sender, false, true);
    }

    /**
     * Reads all lines on this page to the specified command sender with a formatting option.
     *
     * @param sender the command sender to send the lines to
     * @param format whether to apply color formatting to the lines
     */
    public void readTo(CommandSender sender, boolean format) {
        this.readTo(sender, false, format);
    }

    /**
     * Reads all lines on this page to the specified command sender with literal indexing and formatting options.
     * When literal indexing is enabled, missing line indices are filled with empty strings.
     *
     * @param sender          the command sender to send the lines to
     * @param literalIndexing  whether to use literal (sequential) line indexing, filling gaps with empty strings
     * @param format          whether to apply color formatting to the lines
     */
    public void readTo(CommandSender sender, boolean literalIndexing, boolean format) {
        if (!this.isEmpty()) {
            Sender s = new Sender(sender);
            List<String> lines = new ArrayList();
            if (literalIndexing) {
                int lastIndex = (Integer)this.lines.lastKey();

                for(int i = 0; i <= lastIndex; ++i) {
                    String line = (String)this.lines.get(i);
                    lines.add((String)Objects.requireNonNullElse(line, ""));
                }
            } else {
                this.lines.forEach((index, linex) -> lines.add(linex));
            }

            lines.forEach((linex) -> s.sendMessage(linex, format));
        }
    }

    /**
     * Converts the lines on this page to a String array suitable for use as item lore.
     *
     * @param literalIndexing whether to use literal indexing (filling gaps with null) or pack lines sequentially
     * @return a String array of the lines on this page
     */
    public String[] asLore(boolean literalIndexing) {
        if (this.isEmpty()) {
            return new String[0];
        } else if (literalIndexing) {
            String[] lore = new String[(Integer)this.lines.lastKey()];
            this.lines.forEach((index, line) -> lore[index] = line);
            return lore;
        } else {
            List<String> loreList = new ArrayList();
            this.lines.forEach((index, line) -> loreList.add(line));
            return (String[])loreList.toArray(new String[0]);
        }
    }

    /**
     * Converts the lines on this page to a String array with sequential indexing.
     *
     * @return a String array of the lines on this page
     */
    public String[] asLore() {
        return this.asLore(false);
    }

    /**
     * Creates a new TextPage from the given lines.
     *
     * @param lines the lines to include in the page
     * @return a new TextPage containing the specified lines
     */
    public static TextPage of(String... lines) {
        return new TextPage(lines);
    }

    /**
     * Creates a new TextPage from a raw map of line indices to line content.
     *
     * @param raw a sorted map of line indices to line content
     * @return a new TextPage constructed from the raw line data
     */
    public static TextPage of(ConcurrentSkipListMap<Integer, String> raw) {
        TextPage page = of();
        Objects.requireNonNull(page);
        raw.forEach(page::insertLine);
        return page;
    }

}
