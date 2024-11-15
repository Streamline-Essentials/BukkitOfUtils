package host.plas.bou.commands;

import host.plas.bou.BetterPlugin;
import host.plas.bou.instances.BaseManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
public class CommandContext {
    private Sender sender;
    private CommandSender commandSender;
    private Command command;
    private String label;
    private ConcurrentSkipListSet<CommandArgument> args;

    public CommandContext(CommandSender sender, Command command, String label, String... args) {
        this.sender = new Sender(sender);
        this.commandSender = sender;
        this.command = command;
        this.label = label;
        this.args = getArgsFrom(args);
    }

    public CommandArgument getArg(int index) {
        return args.stream().filter(arg -> arg.getIndex() == index).findFirst().orElse(new CommandArgument());
    }

    @Deprecated
    public String getArgString(int index) {
        return getStringArg(index);
    }

    public Optional<Player> getPlayer() {
        if (isPlayer()) return Optional.of((Player) commandSender);
        else return Optional.empty();
    }

    public Player getPlayerOrNull() {
        return getPlayer().orElse(null);
    }

    public Optional<CommandSender> getSenderArg(int argIndex) {
        String playerName = getStringArg(argIndex);
        if (playerName == null) return Optional.empty();

        if (playerName.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
            return Optional.of(Bukkit.getConsoleSender());
        }

        return Optional.ofNullable(Bukkit.getPlayer(playerName));
    }

    public String concatAfter(int index) {
        return args.stream().filter(arg -> arg.getIndex() > index).map(CommandArgument::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public String concatFrom(int index) {
        return args.stream().filter(arg -> arg.getIndex() >= index).map(CommandArgument::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public String concatBefore(int index) {
        return args.stream().filter(arg -> arg.getIndex() < index).map(CommandArgument::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public String concat(int start, int end) {
        return args.stream().filter(arg -> arg.getIndex() >= start && arg.getIndex() <= end).map(CommandArgument::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public String concatExcept(int... indexes) {
        return args.stream().filter(arg -> ! (indexes.length == 0 || indexes[0] == -1 || indexes[0] == 0)).map(CommandArgument::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public CommandArgument getLastArg() {
        return args.last();
    }

    public String getLastArgString() {
        return getLastArg().getContent();
    }

    public Optional<Player> getPlayerArg(int argIndex) {
        return getSenderArg(argIndex).filter(sender -> sender instanceof Player).map(sender -> (Player) sender);
    }

    public boolean isArgUsable(int index) {
        return args.stream().anyMatch(arg -> arg.getIndex() == index);
    }

    public boolean isConsole() {
        return sender.isConsole();
    }

    public boolean isPlayer() {
        return ! isConsole();
    }

    public boolean sendMessage(String message, boolean format) {
        return sender.sendMessage(message, format);
    }

    public boolean sendMessage(String message) {
        return sender.sendMessage(message);
    }

    public int getArgCount() {
        return args.size();
    }

    public String getStringArg(int index) {
        return args.stream().filter(arg -> arg.getIndex() == index).findFirst().orElse(new CommandArgument()).getContent();
    }

    public Optional<Integer> getIntArg(int index) {
        try {
            return Optional.of(Integer.parseInt(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Double> getDoubleArg(int index) {
        try {
            return Optional.of(Double.parseDouble(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Float> getFloatArg(int index) {
        try {
            return Optional.of(Float.parseFloat(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Long> getLongArg(int index) {
        try {
            return Optional.of(Long.parseLong(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Short> getShortArg(int index) {
        try {
            return Optional.of(Short.parseShort(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Byte> getByteArg(int index) {
        try {
            return Optional.of(Byte.parseByte(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Boolean> getBooleanArg(int index) {
        try {
            return Optional.of(Boolean.parseBoolean(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String... args) {
        ConcurrentSkipListSet<CommandArgument> arguments = new ConcurrentSkipListSet<>();
        for (int i = 0; i < args.length; i++) {
            arguments.add(new CommandArgument(i, args[i]));
        }

        return arguments;
    }

    public static ConcurrentSkipListSet<CommandArgument> getArgsFrom(String string) {
        String[] args = string.split(" ");

        return getArgsFrom(args);
    }
}
