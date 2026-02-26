package host.plas.bou.text;

import host.plas.bou.commands.Sender;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import org.bukkit.command.CommandSender;

public class TextPage {
    private ConcurrentSkipListMap<Integer, String> lines;

    public TextPage(String... lines) {
        this.asLines(lines);
    }

    public void asLines(String... lines) {
        this.ensureLines(true);
        for (int i = 0; i < lines.length; ++i) {
            this.lines.put(i, lines[i]);
        }
    }

    public void withLines(String... lines) {
        this.ensureLines();
        int startIndex = this.getLastLineIndex() + 1;
        for (int i = 0; i < lines.length; ++i) {
            this.lines.put(startIndex + i, lines[i]);
        }
    }

    public int getLastLineIndex() {
        return this.lines != null && !this.lines.isEmpty() ? this.lines.lastKey() : -1;
    }

    public void ensureLines() {
        this.ensureLines(false);
    }

    public void ensureLines(boolean clear) {
        if (this.lines == null) {
            this.lines = new ConcurrentSkipListMap();
        } else if (clear) {
            this.lines.clear();
        }
    }

    public Optional<String> getLine(int index) {
        return this.isEmpty() ? Optional.empty() : Optional.ofNullable(this.lines.get(index));
    }

    public void insertLine(int index, String line) {
        this.ensureLines();
        this.lines.put(index, line);
    }

    public boolean isEmpty() {
        return this.lines == null || this.lines.isEmpty();
    }

    public void readTo(CommandSender sender) {
        this.readTo(sender, false, true);
    }

    public void readTo(CommandSender sender, boolean format) {
        this.readTo(sender, false, format);
    }

    public void readTo(CommandSender sender, boolean literalIndexing, boolean format) {
        if (!this.isEmpty()) {
            Sender s = new Sender(sender);
            List<String> lines = new ArrayList();
            if (literalIndexing) {
                int lastIndex = (Integer) this.lines.lastKey();
                for (int i = 0; i <= lastIndex; ++i) {
                    String line = (String) this.lines.get(i);
                    lines.add((String) Objects.requireNonNullElse(line, ""));
                }
            } else {
                this.lines.forEach((index, linex) -> lines.add(linex));
            }
            lines.forEach(linex -> s.sendMessage(linex, format));
        }
    }

    public String[] asLore(boolean literalIndexing) {
        if (this.isEmpty()) {
            return new String[0];
        } else if (literalIndexing) {
            String[] lore = new String[(Integer) this.lines.lastKey()];
            this.lines.forEach((index, line) -> lore[index] = line);
            return lore;
        } else {
            List<String> loreList = new ArrayList();
            this.lines.forEach((index, line) -> loreList.add(line));
            return (String[]) loreList.toArray(new String[0]);
        }
    }

    public String[] asLore() {
        return this.asLore(false);
    }

    public static TextPage of(String... lines) {
        return new TextPage(lines);
    }

    public static TextPage of(ConcurrentSkipListMap<Integer, String> raw) {
        TextPage page = of();
        Objects.requireNonNull(page);
        raw.forEach(page::insertLine);
        return page;
    }

    public void setLines(final ConcurrentSkipListMap<Integer, String> lines) {
        this.lines = lines;
    }

    public ConcurrentSkipListMap<Integer, String> getLines() {
        return this.lines;
    }
}
