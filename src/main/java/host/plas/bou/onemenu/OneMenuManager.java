package host.plas.bou.onemenu;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Loads and opens YAML GUIs from {@code plugins/BukkitOfUtils/guis/}.
 */
public final class OneMenuManager {
    public static final String DEFAULT_FILE = "onemenu.yml";

    private OneMenuManager() {
    }

    public static File getGuisFolder() {
        return new File(BukkitOfUtils.getInstance().getDataFolder(), "guis");
    }

    public static void ensureDefaults() {
        File folder = getGuisFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            BukkitOfUtils.getInstance().logWarning("Could not create guis folder at " + folder.getAbsolutePath());
        }
        File onemenu = new File(folder, DEFAULT_FILE);
        if (!onemenu.exists()) {
            try (InputStream in = BukkitOfUtils.getInstance().getResource("guis/" + DEFAULT_FILE)) {
                if (in == null) {
                    BukkitOfUtils.getInstance().logWarning("Missing resource guis/" + DEFAULT_FILE);
                    return;
                }
                try (OutputStream out = Files.newOutputStream(onemenu.toPath())) {
                    byte[] buf = new byte[8192];
                    int read;
                    while ((read = in.read(buf)) != -1) {
                        out.write(buf, 0, read);
                    }
                }
            } catch (Exception e) {
                BukkitOfUtils.getInstance().logWarning("Failed to write default onemenu.yml", e);
            }
        }
    }

    public static boolean isEnabled() {
        return BaseManager.getBaseConfig() != null && BaseManager.getBaseConfig().isOneMenuEnabled();
    }

    public static void openOneMenu(Player player) {
        openGui(player, DEFAULT_FILE);
    }

    public static void openGui(Player player, String fileName) {
        if (player == null) return;
        if (fileName == null || fileName.isBlank()) {
            fileName = DEFAULT_FILE;
        }
        if (!fileName.toLowerCase().endsWith(".yml")) {
            fileName = fileName + ".yml";
        }
        ensureDefaults();
        File file = new File(getGuisFolder(), fileName);
        if (!file.exists()) {
            MessageUtils.sendMessage(player, "&cGUI file not found: &e" + fileName);
            return;
        }
        try {
            YamlGuiDefinition definition = YamlGuiDefinition.load(file);
            YamlGuiMenu.open(player, definition);
        } catch (Exception e) {
            MessageUtils.sendMessage(player, "&cFailed to open GUI: &f" + e.getMessage());
            BukkitOfUtils.getInstance().logWarning("Failed to open GUI " + fileName, e);
        }
    }
}
