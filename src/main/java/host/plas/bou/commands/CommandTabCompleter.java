package host.plas.bou.commands;

import java.util.concurrent.ConcurrentSkipListSet;

public interface CommandTabCompleter extends WithCommandContext<ConcurrentSkipListSet<String>> {
    static CommandTabCompleter empty() {
        return ctx -> new ConcurrentSkipListSet<>();
    }
}
