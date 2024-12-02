package host.plas.bou.utils.obj;

import host.plas.bou.commands.Sender;
import host.plas.bou.instances.BaseManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Getter @Setter
public class ContextedString<A extends StringArgument> implements Comparable<ContextedString<?>> {
    private Date createdAt;

    private Supplier<A> argumentCreator;
    private BiFunction<Integer, String[], A> argumentCreatorIndexed;
    private ConcurrentSkipListSet<A> args;

    public ContextedString(Supplier<A> argumentCreator, BiFunction<Integer, String[], A> argumentCreatorIndexed, String... args) {
        createdAt = new Date();

        this.argumentCreator = argumentCreator;
        this.argumentCreatorIndexed = argumentCreatorIndexed;
        this.args = getArgsFrom(argumentCreatorIndexed, args);
    }

    @Override
    public int compareTo(@NotNull ContextedString<?> o) {
        return createdAt.compareTo(o.getCreatedAt());
    }

    public A getArg(int index) {
        return args.stream().filter(arg -> arg.getIndex() == index).findFirst().orElse(getArgumentCreator().get());
    }

    @Deprecated
    public String getArgString(int index) {
        return getStringArg(index);
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
        return args.stream().filter(arg -> arg.getIndex() > index).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public String concatFrom(int index) {
        return args.stream().filter(arg -> arg.getIndex() >= index).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public String concatBefore(int index) {
        return args.stream().filter(arg -> arg.getIndex() < index).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public String concat(int start, int end) {
        return args.stream().filter(arg -> arg.getIndex() >= start && arg.getIndex() <= end).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public String concatExcept(int... indexes) {
        return args.stream().filter(arg -> ! (indexes.length == 0 || indexes[0] == -1 || indexes[0] == 0)).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    public A getLastArg() {
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

    public int getArgCount() {
        return args.size();
    }

    public String getStringArg(int index) {
        return args.stream().filter(arg -> arg.getIndex() == index).findFirst().orElse(getArgumentCreator().get()).getContent();
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

    public static <A extends StringArgument> ConcurrentSkipListSet<A> getArgsFrom(BiFunction<Integer, String[], A> argumentCreator, String... args) {
        ConcurrentSkipListSet<A> arguments = new ConcurrentSkipListSet<>();
        for (int i = 0; i < args.length; i++) {
            arguments.add(argumentCreator.apply(i, args));
        }

        return arguments;
    }

    public static <A extends StringArgument> ConcurrentSkipListSet<A> getArgsFrom(BiFunction<Integer, String[], A> argumentCreator, String string, String separator) {
        String[] args = string.split(separator);

        return getArgsFrom(argumentCreator, args);
    }
}
