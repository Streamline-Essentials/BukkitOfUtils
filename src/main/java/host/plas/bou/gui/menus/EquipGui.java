package host.plas.bou.gui.menus;

import host.plas.bou.commands.Sender;
import host.plas.justtags.data.ConfiguredTag;
import host.plas.justtags.data.TagPlayer;
import host.plas.justtags.gui.GuiType;
import host.plas.justtags.gui.TagGui;
import host.plas.justtags.gui.icons.TagIcon;
import host.plas.justtags.managers.TagManager;
import host.plas.justtags.utils.MenuUtils;
import host.plas.justtags.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@Getter @Setter
public class EquipGui extends TagGui {
    private int currentPage;

    public EquipGui(Player player, int currentPage) {
        super(player, GuiType.TAG_EQUIP, 3);

        this.currentPage = currentPage;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        buildPage();
    }

    public void buildPage() {
        buildInner(this);

        buildNextPanel(this);
        buildPreviousPanel(this);
    }

    public static void buildInner(EquipGui gui) {
        Player player = gui.player;
        Optional<TagPlayer> optional = TagManager.getOrGetPlayer(player.getUniqueId().toString());
        if (optional.isEmpty()) return;
        TagPlayer tagPlayer = optional.get();

        int delta = gui.getCurrentPage() * 15 - 15; // no page 0
        for (int i = 0; i < 15; i++) {
            if (delta >= tagPlayer.getAvailable().size()) {
                break;
            }

            TagIcon icon = new TagIcon(getDeltaTag(gui, delta), tagPlayer);

            icon.onClick(event -> {
                if (icon.isEquipped(tagPlayer, icon.getTagIdentifier())) {
                    tagPlayer.removeTag(icon.getTagIdentifier());

                    tagPlayer.ifPlayer(p -> {
                        Sender sender = new Sender(p);
                        sender.sendMessage("&eYou have &cunequipped &ethe tag &f" + icon.getTagIdentifier() + "&e.");
                    });

                    reopen(gui);
                } else {
                    tagPlayer.putTag(tagPlayer.getContainer().size(), icon.getTagIdentifier());

                    tagPlayer.ifPlayer(p -> {
                        Sender sender = new Sender(p);
                        sender.sendMessage("&eYou have &aequipped &ethe tag &f" + icon.getTagIdentifier() + "&e.");
                    });

                    reopen(gui);
                }
            });

            gui.addItem(getGuiSlot(i), icon);

            delta ++;
        }
    }

    public static void reopen(EquipGui gui) {
        new EquipGui(gui.player, gui.getCurrentPage()).open();
    }

    public static String getDeltaTag(EquipGui gui, int delta) {
        Player player = gui.player;
        Optional<TagPlayer> optional = TagManager.getOrGetPlayer(player.getUniqueId().toString());
        if (optional.isEmpty()) return "";
        TagPlayer tagPlayer = optional.get();

        String tag = null;
        Optional<ConfiguredTag> t = tagPlayer.getAvailableTag(delta);
        if (t.isPresent()) {
            tag = t.get().getIdentifier();
        }

        return tag;
    }

    public static int getGuiSlot(int deltaIndex) {
        if (deltaIndex < 5) {
            return deltaIndex + 2;
        }
        if (deltaIndex < 10) {
            return deltaIndex + 6;
        }
        return deltaIndex + 10;
    }

    public static void buildPreviousPanel(EquipGui gui) {
        gui.addItem(0, getOuter());
        gui.addItem(1, getOuter());
//        addItem(9, getAir()); // is the button
        gui.addItem(10, getOuter());
        gui.addItem(18, getOuter());
        gui.addItem(19, getOuter());

        gui.addItem(9, getPreviousButton(gui));
    }

    public static Icon getOuter() {
        ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);

        MenuUtils.insertKey(stack);

        return new Icon(stack);
    }

    public static Icon getPreviousButton(EquipGui gui) {
        ItemStack stack = new ItemStack(Material.ARROW, 1);
        MenuUtils.insertKey(stack);

        Icon icon = new Icon(stack);
        icon.setName(MessageUtils.colorize("&3&l<<< &bPrevious Page"));

        icon.onClick(event -> {
            paginate(gui, -1);
        });

        return icon;
    }

    public static void buildNextPanel(EquipGui gui) {
        gui.addItem(7, getOuter());
        gui.addItem(8, getOuter());
        gui.addItem(16, getOuter());
//        gui.addItem(17, getAir()); // is the button
        gui.addItem(25, getOuter());
        gui.addItem(26, getOuter());

        gui.addItem(17, getNextButton(gui));
    }

    public static Icon getNextButton(EquipGui gui) {
        ItemStack stack = new ItemStack(Material.ARROW, 1);
        MenuUtils.insertKey(stack);

        Icon icon = new Icon(stack);
        icon.setName(MessageUtils.colorize("&bNext Page &3&l>>>"));

        icon.onClick(event -> {
            paginate(gui, 1);
        });

        return icon;
    }

    public static void paginate(EquipGui gui, int amount) {
        int current = gui.getCurrentPage();
        int newPage = getNewPage(gui, amount);
        if (newPage == current) {
            return;
        }
        new EquipGui(gui.player, newPage).open();
    }

    public static int getNewPage(EquipGui gui, int amount) {
        int page = gui.getCurrentPage();
        page += amount;

        if (page > getMaxPages(gui.player)) {
            page = 1;
        }

        if (page < 1) {
            page = getMaxPages(gui.player);
        }

        return page;
    }

    public static int getMaxPages(Player player) {
        Optional<TagPlayer> optional = TagManager.getOrGetPlayer(player.getUniqueId().toString());
        if (optional.isEmpty()) return 1;
        TagPlayer tagPlayer = optional.get();

        int totalTags = tagPlayer.getAvailable().size();

        // if there are 15 or less tags, there is only 1 page
        // if there are 30 or less tags, there are 2 pages
        // if there are 33 tags, there are 3 pages
        int r = (int) Math.ceil((double) totalTags / 15);
        if (r <= 0) {
            r = 1;
        }
        return r;
    }
}
