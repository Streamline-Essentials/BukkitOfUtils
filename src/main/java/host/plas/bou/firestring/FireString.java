package host.plas.bou.firestring;

import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.Identifiable;
import gg.drak.thebase.objects.SingleSet;
import gg.drak.thebase.lib.re2j.Matcher;
import gg.drak.thebase.utils.MatcherUtils;

import java.util.List;

/**
 * Represents a named action that can be triggered by parsing a formatted string.
 * A FireString has an identifier and a consumer that processes the parsed input.
 */
@Getter @Setter
public class FireString implements Identifiable {
    /**
     * The unique identifier for this fire string.
     *
     * @param identifier the identifier to set
     * @return the current identifier
     */
    private String identifier;
    /**
     * The consumer that processes the parsed input when this fire string is fired.
     *
     * @param consumer the fire string consumer to set
     * @return the current fire string consumer
     */
    private FireStringConsumer consumer;

    /**
     * Constructs a new FireString with the given identifier and consumer.
     *
     * @param identifier the unique identifier for this fire string
     * @param consumer   the consumer that handles the string input
     * @param load       whether to immediately register this fire string with the manager
     */
    public FireString(String identifier, FireStringConsumer consumer, boolean load) {
        this.identifier = identifier;
        this.consumer = consumer;

        if (load) {
            load();
        }
    }

    /**
     * Registers this fire string with the {@link FireStringManager}.
     */
    public void load() {
        FireStringManager.register(this);
    }

    /**
     * Unregisters this fire string from the {@link FireStringManager}.
     */
    public void unload() {
        FireStringManager.unregister(getIdentifier());
    }

    /**
     * Fires this fire string's consumer with the given input string.
     *
     * @param string the input string to pass to the consumer
     */
    public void fire(String string) {
        consumer.accept(string);
    }

    /**
     * Parses the given string and fires this fire string's consumer if the parsed
     * identifier matches this fire string's identifier.
     *
     * @param string the formatted string to parse and potentially fire
     * @return {@code true} if the identifier matched and the consumer was fired, {@code false} otherwise
     */
    public boolean checkAndFire(String string) {
        SingleSet<String, String> set = parse(string);
        if (set.getKey().equals(getIdentifier())) {
            fire(set.getValue());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the regex pattern used to parse fire string formatted input.
     * The expected format is "(identifier) value".
     *
     * @return the regex pattern string
     */
    public static String getRegex() {
        // I want to match "[hello] world" and return "hello" and "world"
        return "[(](.*?)[)] (.*)";
    }

    /**
     * Parses a formatted string into an identifier-value pair.
     * The expected format is "(identifier) value".
     *
     * @param string the string to parse
     * @return a {@link SingleSet} containing the identifier as key and the value as value,
     *         or an empty identifier with the original string if parsing fails
     */
    public static SingleSet<String, String> parse(String string) {
        Matcher matcher = MatcherUtils.matcherBuilder(getRegex(), string);
        List<String[]> groups = MatcherUtils.getGroups(matcher, 2);

        if (groups.isEmpty()) {
            return new SingleSet<>("", string);
        } else {
            for (String[] group : groups) {
                return new SingleSet<>(group[0], group[1]);
            }

            return new SingleSet<>("", string);
        }
    }

    @Override
    public String toString() {
        return "FireString{" +
                "identifier='" + identifier + '\'' +
                '}';
    }
}
