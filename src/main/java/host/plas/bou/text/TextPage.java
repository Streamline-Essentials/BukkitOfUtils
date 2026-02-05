package host.plas.bou.text;

import host.plas.bou.commands.Sender;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

@Getter @Setter
public class TextPage {
    private ConcurrentSkipListMap<Integer, String> lines;

    public TextPage(String... lines) {
        asLines(lines);
    }

    public void asLines(String... lines) {
        ensureLines(true);

        for (int i = 0; i < lines.length; i++) {
            this.lines.put(i, lines[i]);
        }
    }

    public void withLines(String... lines) {
        ensureLines();

        int startIndex = getLastLineIndex() + 1;
        for (int i = 0; i < lines.length; i++) {
            this.lines.put(startIndex + i, lines[i]);
        }
    }

    public int getLastLineIndex() {
        if (this.lines == null || this.lines.isEmpty()) return -1;
        return this.lines.lastKey();
    }

    public void ensureLines() {
        ensureLines(false);
    }

    public void ensureLines(boolean clear) {
        if (this.lines == null) this.lines = new ConcurrentSkipListMap<>();
        else {
            if (clear) this.lines.clear();
        }
    }

    public Optional<String> getLine(int index) {
        if (isEmpty()) return Optional.empty();

        return Optional.ofNullable(this.lines.get(index));
    }

    public void insertLine(int index, String line) {
        ensureLines();

        this.lines.put(index, line);
    }

    public boolean isEmpty() {
        return this.lines == null || this.lines.isEmpty();
    }

    public void readTo(Player player) {
        readTo(player, false, true);
    }

    public void readTo(Player player, boolean format) {
        readTo(player, false, format);
    }

    public void readTo(Player player, boolean literalIndexing, boolean format) {
        if (isEmpty()) return;

        Sender sender = new Sender(player);

        List<String> lines = new ArrayList<>();
        if (literalIndexing) {
//            int firstIndex = this.lines.firstKey();
            int lastIndex = this.lines.lastKey();

            for (int i = 0; i <= lastIndex; i++) {
                String line = this.lines.get(i);
                lines.add(Objects.requireNonNullElse(line, ""));
            }
        } else {
            this.lines.forEach((index, line) -> {
                lines.add(line);
            });
        }

        lines.forEach(line -> {
            sender.sendMessage(line, format);
        });
    }

    public String[] asLore(boolean literalIndexing) {
        if (isEmpty()) return new String[0];

        if (literalIndexing) {
            String[] lore = new String[this.lines.lastKey()];
            this.lines.forEach((index, line) -> {
                lore[index] = line;
            });

            return lore;
        } else {
            List<String> loreList = new ArrayList<>();
            this.lines.forEach((index, line) -> {
                loreList.add(line);
            });

            return loreList.toArray(new String[0]);
        }
    }

    public String[] asLore() {
        return asLore(false);
    }

    public static TextPage of(String... lines) {
        return new TextPage(lines);
    }

    public static TextPage of(ConcurrentSkipListMap<Integer, String> raw) {
        TextPage page = of();
        raw.forEach(page::insertLine);

        return page;
    }
}
