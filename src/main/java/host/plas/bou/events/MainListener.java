package host.plas.bou.events;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class MainListener implements Listener {
    public MainListener() {
        Bukkit.getPluginManager().registerEvents(this, BukkitOfUtils.getInstance());

        MessageUtils.logInfo("&cBukkitOfUtils &dMain Listener &fregistered.");
    }
}
