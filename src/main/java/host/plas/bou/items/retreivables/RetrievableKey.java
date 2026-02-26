package host.plas.bou.items.retreivables;

import gg.drak.thebase.objects.Identified;
import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RetrievableKey implements Identified {
    private String plugin;
    private String key;

    public RetrievableKey(String plugin, String key) {
        this.plugin = plugin;
        this.key = key;
    }

    @Override
    public String getIdentifier() {
        return plugin + ":" + key;
    }

    public static RetrievableKey of(String plugin, String key) {
        return new RetrievableKey(plugin, key);
    }

    public static RetrievableKey of(BetterPlugin plugin, String key) {
        return new RetrievableKey(plugin.getIdentifier(), key);
    }
}
