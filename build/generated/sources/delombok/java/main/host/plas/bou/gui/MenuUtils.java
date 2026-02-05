package host.plas.bou.gui;

import host.plas.bou.BukkitOfUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ConcurrentSkipListSet;

public class MenuUtils {
    public static ConcurrentSkipListSet<Integer> getOuter(int rows) {
        ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>();
        if (rows < 1) return set;
        if (rows > 6) rows = 6;

        // top and bottom
        for (int r = 1; r <= rows; r++) {
            for (int i = 1; i <= 9; i++) {
                int real = i * r - 1;

                if (real > 9 && real < 17) {
                    continue;
                }
                if (real > 18 && real < 26) {
                    continue;
                }
                if (real > 27 && real < 35) {
                    continue;
                }
                if (real > 36 && real < 44) {
                    continue;
                }
                if (real > 45 && real < 53) {
                    continue;
                }

                set.add(real);
            }
        }

        return set;
    }

    public static NamespacedKey getInventoryKey(String string) {
        return new NamespacedKey(BukkitOfUtils.getInstance(), "inventory-" + string);
    }

    public static NamespacedKey getStaticKey() {
        return getInventoryKey("static");
    }

    public static NamespacedKey getButtonKey() {
        return getInventoryKey("button");
    }

    public static void injectStatic(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(getStaticKey(), PersistentDataType.INTEGER, 1);
            stack.setItemMeta(meta);
        }
    }

    public static void injectButton(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(getButtonKey(), PersistentDataType.INTEGER, 1);
            stack.setItemMeta(meta);
        }
    }

    public static boolean isStatic(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(getStaticKey(), PersistentDataType.INTEGER);
    }

    public static boolean isButton(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(getButtonKey(), PersistentDataType.INTEGER);
    }
}
