package host.plas.bou.commands;

import java.util.concurrent.ConcurrentSkipListSet;

public interface CommandTabCompleter extends WithCommandContext<ConcurrentSkipListSet<String>> {
    static EmptyTabCompleter empty() {
        return ctx -> new ConcurrentSkipListSet<>();
    }
}
