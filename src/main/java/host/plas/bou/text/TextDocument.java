package host.plas.bou.text;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Represents a document composed of multiple {@link TextPage} instances,
 * stored in a sorted map by page index.
 */
@Setter
@Getter
public class TextDocument {
    /**
     * A sorted map of page indices to page instances for this document.
     *
     * @param pages the pages map to set
     * @return the pages map
     */
    private ConcurrentSkipListMap<Integer, TextPage> pages;

    /**
     * Constructs a new TextDocument with the given pages, replacing any existing pages.
     *
     * @param pages the pages to initialize this document with
     */
    public TextDocument(TextPage... pages) {
        this.asPages(pages);
    }

    /**
     * Replaces all existing pages with the given pages, clearing any previous content.
     *
     * @param pages the pages to set as the document content
     */
    public void asPages(TextPage... pages) {
        this.ensurePages(true);

        for(int i = 0; i < pages.length; ++i) {
            this.pages.put(i, pages[i]);
        }
    }

    /**
     * Appends the given pages after the last existing page in this document.
     *
     * @param pages the pages to append
     */
    public void withPages(TextPage... pages) {
        this.ensurePages();
        int startIndex = this.getLastPageIndex() + 1;

        for(int i = 0; i < pages.length; ++i) {
            this.pages.put(startIndex + i, pages[i]);
        }
    }

    /**
     * Returns the index of the last page in this document.
     *
     * @return the last page index, or -1 if the document is empty or null
     */
    public int getLastPageIndex() {
        return this.pages != null && !this.pages.isEmpty() ? this.pages.lastKey() : -1;
    }

    /**
     * Ensures that the pages map is initialized without clearing existing content.
     */
    public void ensurePages() {
        this.ensurePages(false);
    }

    /**
     * Ensures that the pages map is initialized, optionally clearing existing content.
     *
     * @param clear if true, clears the existing pages map; if false, only initializes if null
     */
    public void ensurePages(boolean clear) {
        if (this.pages == null) {
            this.pages = new ConcurrentSkipListMap<>();
        } else if (clear) {
            this.pages.clear();
        }
    }

    /**
     * Checks whether this document has no pages.
     *
     * @return true if the pages map is null or empty
     */
    public boolean isEmpty() {
        return this.pages == null || this.pages.isEmpty();
    }

    /**
     * Retrieves the page at the specified index.
     *
     * @param index the page index to retrieve
     * @return an Optional containing the page if found, or empty if not found or the document is empty
     */
    public Optional<TextPage> getPage(int index) {
        return this.isEmpty() ? Optional.empty() : Optional.ofNullable(this.pages.get(index));
    }

    /**
     * Inserts a page at the specified index, replacing any existing page at that index.
     *
     * @param index the index to insert the page at
     * @param page  the page to insert
     */
    public void insertPage(int index, TextPage page) {
        this.ensurePages();
        this.pages.put(index, page);
    }

    /**
     * Reads a specific page to a player with default formatting.
     *
     * @param index  the page index to read
     * @param player the player to send the page content to
     */
    public void readPageTo(int index, Player player) {
        if (!this.isEmpty()) {
            this.getPage(index).ifPresent((page) -> page.readTo(player));
        }
    }

    /**
     * Reads a specific page to a player with the specified formatting option.
     *
     * @param index  the page index to read
     * @param player the player to send the page content to
     * @param format whether to apply color formatting to the content
     */
    public void readPageTo(int index, Player player, boolean format) {
        if (!this.isEmpty()) {
            this.getPage(index).ifPresent((page) -> page.readTo(player, format));
        }
    }

    /**
     * Reads a specific page to a player with literal indexing and formatting options.
     *
     * @param index           the page index to read
     * @param player          the player to send the page content to
     * @param literalIndexing whether to use literal (sequential) line indexing
     * @param format          whether to apply color formatting to the content
     */
    public void readPageTo(int index, Player player, boolean literalIndexing, boolean format) {
        if (!this.isEmpty()) {
            this.getPage(index).ifPresent((page) -> page.readTo(player, literalIndexing, format));
        }
    }

    /**
     * Reads all pages to a command sender with default formatting.
     *
     * @param sender the command sender to send all page content to
     */
    public void readAllTo(CommandSender sender) {
        if (!this.isEmpty()) {
            this.pages.values().forEach((page) -> page.readTo(sender));
        }
    }

    /**
     * Reads all pages to a command sender with the specified formatting option.
     *
     * @param sender the command sender to send all page content to
     * @param format whether to apply color formatting to the content
     */
    public void readAllTo(CommandSender sender, boolean format) {
        if (!this.isEmpty()) {
            this.pages.values().forEach((page) -> page.readTo(sender, format));
        }
    }

    /**
     * Reads all pages to a command sender with literal indexing and formatting options.
     *
     * @param sender          the command sender to send all page content to
     * @param literalIndexing whether to use literal (sequential) line indexing
     * @param format          whether to apply color formatting to the content
     */
    public void readAllTo(CommandSender sender, boolean literalIndexing, boolean format) {
        if (!this.isEmpty()) {
            this.pages.values().forEach((page) -> page.readTo(sender, literalIndexing, format));
        }
    }

    /**
     * Creates a new TextDocument from the given pages.
     *
     * @param pages the pages to include in the document
     * @return a new TextDocument containing the specified pages
     */
    public static TextDocument of(TextPage... pages) {
        return new TextDocument(pages);
    }

    /**
     * Creates a new TextDocument from a single page containing the given lines.
     *
     * @param lines the lines to include in the single-page document
     * @return a new TextDocument containing one page with the specified lines
     */
    public static TextDocument ofLines(String... lines) {
        return of(TextPage.of(lines));
    }

    /**
     * Creates a new TextDocument from a raw map of page indices to line maps.
     *
     * @param rawPages a map of page indices to maps of line indices to line content
     * @return a new TextDocument constructed from the raw page data
     */
    public static TextDocument of(ConcurrentSkipListMap<Integer, ConcurrentSkipListMap<Integer, String>> rawPages) {
        TextDocument document = of();
        rawPages.forEach((pageIndex, rawLines) -> {
            TextPage page = TextPage.of(rawLines);
            document.insertPage(pageIndex, page);
        });
        return document;
    }
}
