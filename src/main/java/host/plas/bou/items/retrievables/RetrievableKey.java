//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package host.plas.bou.items.retrievables;

import gg.drak.thebase.objects.Identified;
import host.plas.bou.BetterPlugin;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RetrievableKey implements Identified {
    private String plugin;
    private String key;

    public RetrievableKey(String plugin, String key) {
        this.plugin = plugin;
        this.key = key;
    }

    public String getIdentifier() {
        return this.plugin + ":" + this.key;
    }

    public static RetrievableKey of(String plugin, String key) {
        return new RetrievableKey(plugin, key);
    }

    public static RetrievableKey of(BetterPlugin plugin, String key) {
        return new RetrievableKey(plugin.getIdentifier(), key);
    }
}
