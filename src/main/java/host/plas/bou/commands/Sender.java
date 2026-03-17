package host.plas.bou.commands;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.utils.ColorUtils;
import host.plas.bou.utils.SenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import gg.drak.thebase.objects.AtomicString;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a command sender with additional utility methods for sending messages,
 * titles, and executing commands. Wraps a Bukkit CommandSender with UUID and name tracking.
 */
@Getter @Setter
public class Sender {
    /**
     * The unique identifier of this sender.
     *
     * @param uuid the UUID to set
     * @return the UUID of this sender
     */
    private String uuid;
    /**
     * The display name of this sender.
     *
     * @param name the name to set
     * @return the name of this sender
     */
    private String name;

    /**
     * The underlying Bukkit command sender.
     *
     * @param commandSender the command sender to set
     * @return the Bukkit command sender
     */
    private CommandSender commandSender;

    /**
     * Constructs a Sender with a specified UUID, name, and underlying CommandSender.
     *
     * @param uuid the UUID of the sender
     * @param name the name of the sender
     * @param sender the Bukkit CommandSender
     */
    public Sender(String uuid, String name, CommandSender sender) {
        this.uuid = uuid;
        this.name = name;
        this.commandSender = sender;
    }

    /**
     * Constructs a Sender from a name and CommandSender, formatting the UUID based on whether
     * the sender is the console.
     *
     * @param name the name of the sender
     * @param sender the Bukkit CommandSender
     * @param isConsole whether the sender is the console
     */
    public Sender(String name, CommandSender sender, boolean isConsole) {
        this(SenderUtils.formatUuid(name, isConsole), SenderUtils.formatName(name, isConsole), sender);
    }

    /**
     * Constructs a Sender from a Bukkit CommandSender, automatically detecting
     * whether the sender is the console.
     *
     * @param sender the Bukkit CommandSender
     */
    public Sender(CommandSender sender) {
        this(sender.getName(), sender, Bukkit.getConsoleSender().equals(sender));
    }

    /**
     * Checks whether this sender is the console.
     *
     * @return true if the sender is the console
     */
    public boolean isConsole() {
        return Bukkit.getConsoleSender().equals(commandSender) || uuid.equals(BaseManager.getBaseConfig().getConsoleUUID());
    }

    /**
     * Checks whether this sender is a player.
     *
     * @return true if the sender is a player
     */
    public boolean isPlayer() {
        return ! isConsole();
    }

    /**
     * Sends a message to this sender with optional color formatting.
     *
     * @param message the message to send
     * @param format whether to apply color formatting to the message
     * @return true if the message was sent successfully
     */
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

    /**
     * Sends a formatted message to this sender.
     *
     * @param message the message to send
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(String message) {
        return sendMessage(message, true);
    }

    /**
     * Sends a title and subtitle to this sender with specified timing.
     * Only works if the sender is a player.
     *
     * @param title the title text
     * @param subtitle the subtitle text
     * @param fadeIn the fade-in duration in ticks
     * @param stay the stay duration in ticks
     * @param fadeOut the fade-out duration in ticks
     * @return true if the title was sent successfully
     */
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

    /**
     * Sends a title to this sender by parsing a string that may contain optional
     * parameters (-fadeIn=, -stay=, -fadeOut=) and a newline separator (backslash-n)
     * between title and subtitle.
     *
     * @param string the title string to parse
     * @return true if the title was sent successfully
     */
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

    /**
     * Gets this sender as an OfflinePlayer.
     *
     * @return an Optional containing the OfflinePlayer, or empty if not found
     */
    public Optional<OfflinePlayer> getOfflinePlayer() {
        return SenderUtils.getOfflinePlayer(uuid);
    }

    /**
     * Gets this sender as an online Player.
     *
     * @return an Optional containing the Player, or empty if not online or not a player
     */
    public Optional<Player> getPlayer() {
        return getOfflinePlayer().map(OfflinePlayer::getPlayer);
    }

    /**
     * Gets the underlying CommandSender, resolving from console or player as appropriate.
     *
     * @return an Optional containing the CommandSender
     */
    public Optional<CommandSender> getCommandSender() {
        if (isConsole()) {
            return Optional.of(Bukkit.getConsoleSender());
        } else {
            return getPlayer().map(p -> p);
        }
    }

    /**
     * Dispatches a command as this sender.
     *
     * @param command the command string to execute (without the leading slash)
     */
    public void executeCommand(String command) {
        if (isConsole()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            getPlayer().ifPresent(player -> Bukkit.dispatchCommand(player, command));
        }
    }

    /**
     * Sends a chat message as this sender. If the sender is the console,
     * the message is dispatched as a command instead.
     *
     * @param message the message to send as chat
     */
    public void chatAs(String message) {
        if (isConsole()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
        } else {
            getPlayer().ifPresent(player -> player.chat(message));
        }
    }

    /**
     * Sends BaseComponent messages to this sender via Spigot's API, with a plain text fallback.
     *
     * @param alternate the fallback plain text message if component sending fails, or null for no fallback
     * @param components the base components to send
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(@Nullable String alternate, BaseComponent... components) {
        AtomicBoolean success = new AtomicBoolean(false);
        getCommandSender().ifPresent(sender -> {
            try {
                sender.spigot().sendMessage(components);
                success.set(true);
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logWarning("Failed to send Component message: " + e.getMessage(), e);

                if (alternate != null) {
                    sendMessage(alternate);
                    success.set(true);
                } else {
                    success.set(false);
                }
            }
        });

        return success.get();
    }

    /**
     * Sends BaseComponent messages to this sender via Spigot's API.
     *
     * @param components the base components to send
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(BaseComponent... components) {
        return sendMessage(null, components);
    }

    /**
     * Sends a message built from a ComponentBuilder with a plain text fallback.
     *
     * @param alternate the fallback plain text message if component sending fails
     * @param builder the component builder
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(String alternate, ComponentBuilder builder) {
        return sendMessage(alternate, builder.create());
    }

    /**
     * Sends a message built from a ComponentBuilder.
     *
     * @param builder the component builder
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(ComponentBuilder builder) {
        return sendMessage(builder.create());
    }
}
