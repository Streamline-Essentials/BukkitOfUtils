package host.plas.bou.utils.obj;

import host.plas.bou.instances.BaseManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Represents a contexted string composed of indexed string arguments,
 * providing typed argument access and concatenation utilities.
 *
 * @param <A> the type of StringArgument used by this contexted string
 */
@Getter @Setter
public class ContextedString<A extends StringArgument> implements Comparable<ContextedString<?>> {
    /**
     * The date and time when this contexted string was created.
     *
     * @param createdAt the creation date to set
     * @return the creation date
     */
    private Date createdAt;

    /**
     * A supplier that creates a default argument instance.
     *
     * @param argumentCreator the argument creator supplier to set
     * @return the argument creator supplier
     */
    private Supplier<A> argumentCreator;

    /**
     * A function that creates an argument from an index and the full args array.
     *
     * @param argumentCreatorIndexed the indexed argument creator function to set
     * @return the indexed argument creator function
     */
    private BiFunction<Integer, String[], A> argumentCreatorIndexed;

    /**
     * The set of parsed string arguments.
     *
     * @param args the argument set to set
     * @return the argument set
     */
    private ConcurrentSkipListSet<A> args;

    /**
     * Constructs a new ContextedString with the given argument creators and initial arguments.
     *
     * @param argumentCreator        a supplier that creates a default argument instance
     * @param argumentCreatorIndexed a function that creates an argument from an index and the full args array
     * @param args                   the initial string arguments
     */
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

    /**
     * Retrieves the argument at the specified index, or a default argument if not found.
     *
     * @param index the argument index
     * @return the argument at the index, or a default argument
     */
    public A getArg(int index) {
        return args.stream().filter(arg -> arg.getIndex() == index).findFirst().orElse(getArgumentCreator().get());
    }

    /**
     * Returns the string content of the argument at the specified index.
     *
     * @param index the argument index
     * @return the string content of the argument
     * @deprecated Use {@link #getStringArg(int)} instead
     */
    @Deprecated
    public String getArgString(int index) {
        return getStringArg(index);
    }

    /**
     * Resolves the argument at the given index to a CommandSender (player or console).
     *
     * @param argIndex the argument index containing the player name
     * @return an Optional containing the CommandSender if found
     */
    public Optional<CommandSender> getSenderArg(int argIndex) {
        String playerName = getStringArg(argIndex);
        if (playerName == null) return Optional.empty();

        if (playerName.equals(BaseManager.getBaseConfig().getConsoleUUID())) {
            return Optional.of(Bukkit.getConsoleSender());
        }

        return Optional.ofNullable(Bukkit.getPlayer(playerName));
    }

    /**
     * Concatenates all argument contents after the specified index, separated by spaces.
     *
     * @param index the index after which to start concatenating
     * @return the concatenated string
     */
    public String concatAfter(int index) {
        return args.stream().filter(arg -> arg.getIndex() > index).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    /**
     * Concatenates all argument contents from the specified index onward, separated by spaces.
     *
     * @param index the index from which to start concatenating (inclusive)
     * @return the concatenated string
     */
    public String concatFrom(int index) {
        return args.stream().filter(arg -> arg.getIndex() >= index).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    /**
     * Concatenates all argument contents before the specified index, separated by spaces.
     *
     * @param index the index before which to concatenate
     * @return the concatenated string
     */
    public String concatBefore(int index) {
        return args.stream().filter(arg -> arg.getIndex() < index).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    /**
     * Concatenates all argument contents within the specified index range (inclusive), separated by spaces.
     *
     * @param start the start index (inclusive)
     * @param end   the end index (inclusive)
     * @return the concatenated string
     */
    public String concat(int start, int end) {
        return args.stream().filter(arg -> arg.getIndex() >= start && arg.getIndex() <= end).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    /**
     * Concatenates all argument contents except those at the specified indices, separated by spaces.
     *
     * @param indexes the indices to exclude
     * @return the concatenated string
     */
    public String concatExcept(int... indexes) {
        return args.stream().filter(arg -> ! (indexes.length == 0 || indexes[0] == -1 || indexes[0] == 0)).map(A::getContent).reduce((a, b) -> a + " " + b).orElse("");
    }

    /**
     * Returns the last argument in the set.
     *
     * @return the last argument
     */
    public A getLastArg() {
        return args.last();
    }

    /**
     * Returns the string content of the last argument.
     *
     * @return the last argument's content
     */
    public String getLastArgString() {
        return getLastArg().getContent();
    }

    /**
     * Resolves the argument at the given index to a Player.
     *
     * @param argIndex the argument index containing the player name
     * @return an Optional containing the Player if found and online
     */
    public Optional<Player> getPlayerArg(int argIndex) {
        return getSenderArg(argIndex).filter(sender -> sender instanceof Player).map(sender -> (Player) sender);
    }

    /**
     * Checks whether an argument exists at the specified index.
     *
     * @param index the argument index to check
     * @return true if an argument exists at the index
     */
    public boolean isArgUsable(int index) {
        return args.stream().anyMatch(arg -> arg.getIndex() == index);
    }

    /**
     * Returns the total number of arguments.
     *
     * @return the argument count
     */
    public int getArgCount() {
        return args.size();
    }

    /**
     * Returns the string content of the argument at the specified index.
     *
     * @param index the argument index
     * @return the string content of the argument
     */
    public String getStringArg(int index) {
        return args.stream().filter(arg -> arg.getIndex() == index).findFirst().orElse(getArgumentCreator().get()).getContent();
    }

    /**
     * Parses the argument at the specified index as an integer.
     *
     * @param index the argument index
     * @return an Optional containing the parsed integer, or empty if parsing fails
     */
    public Optional<Integer> getIntArg(int index) {
        try {
            return Optional.of(Integer.parseInt(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses the argument at the specified index as a double.
     *
     * @param index the argument index
     * @return an Optional containing the parsed double, or empty if parsing fails
     */
    public Optional<Double> getDoubleArg(int index) {
        try {
            return Optional.of(Double.parseDouble(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses the argument at the specified index as a float.
     *
     * @param index the argument index
     * @return an Optional containing the parsed float, or empty if parsing fails
     */
    public Optional<Float> getFloatArg(int index) {
        try {
            return Optional.of(Float.parseFloat(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses the argument at the specified index as a long.
     *
     * @param index the argument index
     * @return an Optional containing the parsed long, or empty if parsing fails
     */
    public Optional<Long> getLongArg(int index) {
        try {
            return Optional.of(Long.parseLong(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses the argument at the specified index as a short.
     *
     * @param index the argument index
     * @return an Optional containing the parsed short, or empty if parsing fails
     */
    public Optional<Short> getShortArg(int index) {
        try {
            return Optional.of(Short.parseShort(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses the argument at the specified index as a byte.
     *
     * @param index the argument index
     * @return an Optional containing the parsed byte, or empty if parsing fails
     */
    public Optional<Byte> getByteArg(int index) {
        try {
            return Optional.of(Byte.parseByte(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses the argument at the specified index as a boolean.
     *
     * @param index the argument index
     * @return an Optional containing the parsed boolean, or empty if parsing fails
     */
    public Optional<Boolean> getBooleanArg(int index) {
        try {
            return Optional.of(Boolean.parseBoolean(getArgString(index)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Creates a sorted set of arguments from a varargs array of strings using the provided creator function.
     *
     * @param <A>              the argument type
     * @param argumentCreator  a function that creates an argument from an index and the full args array
     * @param args             the string arguments
     * @return a sorted set of created arguments
     */
    public static <A extends StringArgument> ConcurrentSkipListSet<A> getArgsFrom(BiFunction<Integer, String[], A> argumentCreator, String... args) {
        ConcurrentSkipListSet<A> arguments = new ConcurrentSkipListSet<>();
        for (int i = 0; i < args.length; i++) {
            arguments.add(argumentCreator.apply(i, args));
        }

        return arguments;
    }

    /**
     * Creates a sorted set of arguments by splitting a string with a separator and using the provided creator function.
     *
     * @param <A>              the argument type
     * @param argumentCreator  a function that creates an argument from an index and the full args array
     * @param string           the string to split
     * @param separator        the separator to split on
     * @return a sorted set of created arguments
     */
    public static <A extends StringArgument> ConcurrentSkipListSet<A> getArgsFrom(BiFunction<Integer, String[], A> argumentCreator, String string, String separator) {
        String[] args = string.split(separator);

        return getArgsFrom(argumentCreator, args);
    }
}
