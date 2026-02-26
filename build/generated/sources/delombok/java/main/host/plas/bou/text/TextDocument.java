package host.plas.bou.text;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TextDocument {
    private ConcurrentSkipListMap<Integer, TextPage> pages;

    public TextDocument(TextPage... pages) {
        this.asPages(pages);
    }

    public void asPages(TextPage... pages) {
        this.ensurePages(true);
        for (int i = 0; i < pages.length; ++i) {
            this.pages.put(i, pages[i]);
        }
    }

    public void withPages(TextPage... pages) {
        this.ensurePages();
        int startIndex = this.getLastPageIndex() + 1;
        for (int i = 0; i < pages.length; ++i) {
            this.pages.put(startIndex + i, pages[i]);
        }
    }

    public int getLastPageIndex() {
        return this.pages != null && !this.pages.isEmpty() ? this.pages.lastKey() : -1;
    }

    public void ensurePages() {
        this.ensurePages(false);
    }

    public void ensurePages(boolean clear) {
        if (this.pages == null) {
            this.pages = new ConcurrentSkipListMap<>();
        } else if (clear) {
            this.pages.clear();
        }
    }

    public boolean isEmpty() {
        return this.pages == null || this.pages.isEmpty();
    }

    public Optional<TextPage> getPage(int index) {
        return this.isEmpty() ? Optional.empty() : Optional.ofNullable(this.pages.get(index));
    }

    public void insertPage(int index, TextPage page) {
        this.ensurePages();
        this.pages.put(index, page);
    }

    public void readPageTo(int index, Player player) {
        if (!this.isEmpty()) {
            this.getPage(index).ifPresent(page -> page.readTo(player));
        }
    }

    public void readPageTo(int index, Player player, boolean format) {
        if (!this.isEmpty()) {
            this.getPage(index).ifPresent(page -> page.readTo(player, format));
        }
    }

    public void readPageTo(int index, Player player, boolean literalIndexing, boolean format) {
        if (!this.isEmpty()) {
            this.getPage(index).ifPresent(page -> page.readTo(player, literalIndexing, format));
        }
    }

    public void readAllTo(CommandSender sender) {
        if (!this.isEmpty()) {
            this.pages.values().forEach(page -> page.readTo(sender));
        }
    }

    public void readAllTo(CommandSender sender, boolean format) {
        if (!this.isEmpty()) {
            this.pages.values().forEach(page -> page.readTo(sender, format));
        }
    }

    public void readAllTo(CommandSender sender, boolean literalIndexing, boolean format) {
        if (!this.isEmpty()) {
            this.pages.values().forEach(page -> page.readTo(sender, literalIndexing, format));
        }
    }

    public static TextDocument of(TextPage... pages) {
        return new TextDocument(pages);
    }

    public static TextDocument ofLines(String... lines) {
        return of(TextPage.of(lines));
    }

    public static TextDocument of(ConcurrentSkipListMap<Integer, ConcurrentSkipListMap<Integer, String>> rawPages) {
        TextDocument document = of();
        rawPages.forEach((pageIndex, rawLines) -> {
            TextPage page = TextPage.of(rawLines);
            document.insertPage(pageIndex, page);
        });
        return document;
    }

    public void setPages(final ConcurrentSkipListMap<Integer, TextPage> pages) {
        this.pages = pages;
    }

    public ConcurrentSkipListMap<Integer, TextPage> getPages() {
        return this.pages;
    }
}
