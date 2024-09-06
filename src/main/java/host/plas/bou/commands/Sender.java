package host.plas.bou.commands;

import host.plas.bou.utils.ColorUtils;
import host.plas.bou.utils.MessageUtils;
import host.plas.bou.BetterPlugin;
import host.plas.bou.utils.SenderUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tv.quaint.objects.AtomicString;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter @Setter
public class Sender {
    private String uuid;
    private String name;

    private CommandSender commandSender;

    public Sender(String uuid, String name, CommandSender sender) {
        this.uuid = uuid;
        this.name = name;
        this.commandSender = sender;
    }

    public Sender(String name, CommandSender sender, boolean isConsole) {
        this(SenderUtils.formatUuid(name, isConsole), SenderUtils.formatName(name, isConsole), sender);
    }

    public Sender(CommandSender sender) {
        this(sender.getName(), sender, Bukkit.getConsoleSender().equals(sender));
    }

    public boolean isConsole() {
        return Bukkit.getConsoleSender().equals(commandSender) || uuid.equals(BetterPlugin.getBaseConfig().getConsoleUUID());
    }

    public boolean isPlayer() {
        return ! isConsole();
    }

    public boolean sendMessage(String message, boolean format) {
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicString finalMessage = new AtomicString(message);

        if (format) {
            finalMessage.set(ColorUtils.colorizeHard(message));
        }

        getCommandSender().ifPresent(sender -> {
            sender.sendMessage(finalMessage.get());

            success.set(true);
        });

        return success.get();
    }

    public boolean sendMessage(String message) {
        return sendMessage(message, true);
    }

    public boolean sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        AtomicBoolean success = new AtomicBoolean(false);

        getCommandSender().ifPresent(sender -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                player.sendTitle(ColorUtils.colorizeHard(title), ColorUtils.colorizeHard(subtitle), fadeIn, stay, fadeOut);

                success.set(true);
            }
        });

        return success.get();
    }

    public boolean sendTitle(String string) {
        int fadeIn = 10;
        int stay = 70;
        int fadeOut = 20;

        ConcurrentSkipListSet<Integer> remove = new ConcurrentSkipListSet<>();
        String[] split = string.split(" ");
        int i = 0;
        for (String s : split) {
            if (s.startsWith("-fadeIn=")) {
                try {
                    fadeIn = Integer.parseInt(s.replace("-fadeIn=", ""));
                    remove.add(i);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else if (s.startsWith("-stay=")) {
                try {
                    stay = Integer.parseInt(s.replace("-stay=", ""));
                    remove.add(i);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else if (s.startsWith("-fadeOut=")) {
                try {
                    fadeOut = Integer.parseInt(s.replace("-fadeOut=", ""));
                    remove.add(i);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            i++;
        }

        StringBuilder title = new StringBuilder();
        int n = 0;
        for (String s : split) {
            if (! remove.contains(n)) {
                title.append(s).append(" ");
            }

            n++;
        }
        String t = title.toString().trim();

        String[] splitTitle = t.split("\\\\n", 2);
        String titleString = splitTitle[0];
        String subtitleString = splitTitle.length > 1 ? splitTitle[1] : "";

        return sendTitle(titleString, subtitleString, fadeIn, stay, fadeOut);
    }

    public Optional<OfflinePlayer> getOfflinePlayer() {
        return SenderUtils.getOfflinePlayer(uuid);
    }

    public Optional<Player> getPlayer() {
        return getOfflinePlayer().map(OfflinePlayer::getPlayer);
    }

    public Optional<CommandSender> getCommandSender() {
        if (isConsole()) {
            return Optional.of(Bukkit.getConsoleSender());
        } else {
            return getPlayer().map(p -> p);
        }
    }
}
