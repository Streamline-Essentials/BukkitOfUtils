package host.plas.bou.gui.menus;

import host.plas.bou.BetterPlugin;
import host.plas.bou.drakapi.VersionCheckResult;
import host.plas.bou.drakapi.VersionChecker;
import host.plas.bou.gui.BetterPluginMenuBuilder;
import host.plas.bou.gui.InventorySheet;
import host.plas.bou.gui.icons.BasicIcon;
import host.plas.bou.gui.screens.ScreenInstance;
import host.plas.bou.gui.slots.SlotType;
import host.plas.bou.gui.type.BouGuiTypes;
import host.plas.bou.items.ItemUtils;
import host.plas.bou.sql.ConnectorSet;
import host.plas.bou.sql.DBOperator;
import host.plas.bou.sql.DatabaseType;
import host.plas.bou.utils.DatabaseUtils;
import host.plas.bou.utils.MessageUtils;
import mc.obliviate.inventory.Icon;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Info GUI for a {@link BetterPlugin}, opened via {@code /boup menu <plugin>}.
 */
public class BouPluginInfoMenu extends ScreenInstance {
    private final BetterPlugin target;
    private volatile VersionCheckResult versionResult;

    public BouPluginInfoMenu(@NotNull Player player, @NotNull BetterPlugin target) {
        super(player, BouGuiTypes.BOU_PLUGIN_INFO, buildSheet(player, target, null), true);
        this.target = target;
        updateTitle(MessageUtils.codedString("&8BOU &7| &b" + target.getName()));
        refreshVersionAsync();
    }

    public static void open(@NotNull Player player, @NotNull BetterPlugin target) {
        new BouPluginInfoMenu(player, target).open();
    }

    private void refreshVersionAsync() {
        String id = VersionChecker.resolveModrinthId(target);
        if (id == null || id.isBlank()) {
            this.versionResult = VersionCheckResult.failure("No Modrinth id configured.");
            rebuild();
            return;
        }
        VersionCheckResult cached = VersionChecker.getCached(id);
        if (cached != null) {
            this.versionResult = cached;
            rebuild();
        }
        VersionChecker.checkThen(target, result -> {
            this.versionResult = result;
            rebuild();
        });
    }

    private void rebuild() {
        setInventorySheet(buildSheet(getPlayer(), target, versionResult));
        build(getInventorySheet());
    }

    private static InventorySheet buildSheet(Player player, BetterPlugin target, VersionCheckResult versionResult) {
        InventorySheet sheet = InventorySheet.empty(27);

        List<String> authors = target.getDescription().getAuthors();
        String authorLine = (authors == null || authors.isEmpty()) ? "Unknown" : String.join(", ", authors);

        ItemStack versionItem = ItemUtils.make(Material.BOOK,
                "&eVersion",
                "&7" + target.getDescription().getVersion(),
                "&7Status: " + (target.isEnabled() ? "&aEnabled" : "&cDisabled"));
        sheet.setIcon(10, versionItem, SlotType.BUTTON);

        ItemStack authorItem = ItemUtils.make(Material.PLAYER_HEAD,
                "&eAuthor(s)",
                "&7" + authorLine);
        sheet.setIcon(11, authorItem, SlotType.BUTTON);

        ItemStack dbItem = ItemUtils.make(Material.CHEST,
                "&eDatabase(s)",
                buildDatabaseLore(target).toArray(new String[0]));
        sheet.setIcon(12, dbItem, SlotType.BUTTON);

        ItemStack uptimeItem = ItemUtils.make(Material.CLOCK,
                "&eUptime",
                "&7" + target.getFormattedUptime());
        sheet.setIcon(13, uptimeItem, SlotType.BUTTON);

        Icon updateIcon = new BasicIcon(buildUpdateStack(target, versionResult)).onClick(e -> {
            Player clicker = (Player) e.getWhoClicked();
            sendDownloadLink(clicker, target, versionResult);
        });
        sheet.setIcon(14, updateIcon);

        BetterPluginMenuBuilder builder = target.getPluginMenuBuilder();
        if (builder != null) {
            Icon provided = new BasicIcon(ItemUtils.make(Material.EMERALD_BLOCK,
                    "&aPlugin Provided Menu",
                    "&7Click to open this plugin's",
                    "&7custom menu.")).onClick(e -> {
                Player clicker = (Player) e.getWhoClicked();
                try {
                    ScreenInstance menu = builder.build(clicker, target);
                    if (menu != null) {
                        menu.open();
                    } else {
                        MessageUtils.sendMessage(clicker, "&cPlugin menu builder returned null.");
                    }
                } catch (Throwable t) {
                    MessageUtils.sendMessage(clicker, "&cFailed to open plugin menu: &f" + t.getMessage());
                }
            });
            sheet.setIcon(16, provided);
        } else {
            sheet.setIcon(16, ItemUtils.make(Material.GRAY_DYE,
                    "&7Plugin Provided Menu",
                    "&cThis plugin did not provide a menu."), SlotType.STATIC);
        }

        return sheet;
    }

    private static ItemStack buildUpdateStack(BetterPlugin target, VersionCheckResult result) {
        List<String> lore = new ArrayList<>();
        String modrinthId = VersionChecker.resolveModrinthId(target);
        if (modrinthId == null || modrinthId.isBlank()) {
            lore.add("&cNo Modrinth id configured.");
            lore.add("&7Override getModrinthId() in the plugin.");
            return ItemUtils.make(Material.BARRIER, "&eUp to date?", lore);
        }
        lore.add("&7Project: &b" + modrinthId);
        if (result == null) {
            lore.add("&eChecking Modrinth...");
            lore.add("&7Click after check for download link.");
            return ItemUtils.make(Material.ENDER_PEARL, "&eUp to date?", lore);
        }
        if (!result.isSuccess()) {
            lore.add("&cCheck failed");
            if (result.getMessage() != null) lore.add("&7" + result.getMessage());
            return ItemUtils.make(Material.REDSTONE_BLOCK, "&eUp to date?", lore);
        }
        lore.add("&7Status: &e" + result.getStatus().name());
        lore.add("&7Installed: &b" + result.getCurrentVersion());
        if (result.getLatestVersion() != null) {
            lore.add("&7Latest: &a" + result.getLatestVersion());
        }
        if (result.isBehind()) {
            lore.add("&cOutdated — click for download link");
            return ItemUtils.make(Material.REDSTONE_TORCH, "&cUp to date? &7No", lore);
        }
        if (result.isUpToDate()) {
            lore.add("&aYou are up to date");
            lore.add("&7Click for Modrinth / download link");
            return ItemUtils.make(Material.LIME_DYE, "&aUp to date? &7Yes", lore);
        }
        if (result.isAhead()) {
            lore.add("&eInstalled version is ahead of Modrinth");
            lore.add("&7Click for link");
            return ItemUtils.make(Material.GLOWSTONE_DUST, "&eUp to date? &7Ahead", lore);
        }
        lore.add("&7Click for link");
        return ItemUtils.make(Material.COMPASS, "&eUp to date?", lore);
    }

    private static List<String> buildDatabaseLore(BetterPlugin target) {
        List<String> lore = new ArrayList<>();
        ConcurrentSkipListSet<DBOperator> ops = DatabaseUtils.get(target.getIdentifier());
        if (ops == null || ops.isEmpty()) {
            lore.add("&cNo databases registered.");
            return lore;
        }
        for (DBOperator op : ops) {
            ConnectorSet cs = op.getConnectorSet();
            lore.add("&7ID: &a" + op.getId() + " &7Type: &c" + (cs.getType() == null ? "?" : cs.getType().name()));
            if (cs.getType() == DatabaseType.MYSQL) {
                lore.add("  &7Host: &f" + nullSafe(cs.getHost()) + ":" + cs.getPort());
                lore.add("  &7DB: &f" + nullSafe(cs.getDatabase()) + " &7User: &f" + nullSafe(cs.getUsername()));
            } else if (cs.hasSqliteFile()) {
                lore.add("  &7File: &f" + cs.getSqliteFileName());
            }
            if (cs.getTablePrefix() != null && !cs.getTablePrefix().isBlank()) {
                lore.add("  &7Prefix: &f" + cs.getTablePrefix());
            }
        }
        return lore;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static void sendDownloadLink(Player player, BetterPlugin target, VersionCheckResult result) {
        String url = null;
        if (result != null && result.getDownloadUrl() != null && !result.getDownloadUrl().isBlank()) {
            url = result.getDownloadUrl();
        } else {
            String slug = result != null && result.getProjectSlug() != null
                    ? result.getProjectSlug()
                    : VersionChecker.resolveModrinthId(target);
            if (slug != null && !slug.isBlank()) {
                url = "https://modrinth.com/plugin/" + slug;
            }
        }
        if (url == null) {
            MessageUtils.sendMessage(player, "&cNo download / Modrinth link available for &e" + target.getName() + "&c.");
            return;
        }

        MessageUtils.sendMessage(player, "&7Download / Modrinth for &b" + target.getName() + "&7:");
        TextComponent component = new TextComponent(MessageUtils.codedString("&b&n" + url));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(MessageUtils.codedString("&aClick to open")).create()));
        player.spigot().sendMessage(component);
        player.closeInventory();
    }
}
