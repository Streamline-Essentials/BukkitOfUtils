package host.plas.bou.events;

import host.plas.bou.BukkitOfUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class MainListener implements Listener {
    public MainListener() {
        Bukkit.getPluginManager().registerEvents(this, BukkitOfUtils.getInstance());

        BukkitOfUtils.getInstance().logInfo("&cBukkitOfUtils &dMain Listener &fregistered.");
    }
}
