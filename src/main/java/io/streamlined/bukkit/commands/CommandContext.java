package io.streamlined.bukkit.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
public class CommandContext {
    private Sender sender;
    private CommandSender masterSender;
    private Command command;
    private String label;
    private ConcurrentSkipListSet<CommandArgument> args;

    public CommandContext(CommandSender sender, Command command, String label, String... args) {
        this.sender = new Sender(sender);
        this.masterSender = sender;
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
