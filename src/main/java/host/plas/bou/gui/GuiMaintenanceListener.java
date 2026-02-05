package host.plas.bou.gui;

import host.plas.bou.BukkitOfUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiMaintenanceListener implements Listener {
    public GuiMaintenanceListener() {
        Bukkit.getPluginManager().registerEvents(this, BukkitOfUtils.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (! (event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (MenuUtils.isButton(item) || MenuUtils.isStatic(item)) {
            event.setCancelled(true);
        }
    }
}
