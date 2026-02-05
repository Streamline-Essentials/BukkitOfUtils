package host.plas.bou.firestring;

import lombok.Getter;
import lombok.Setter;
import gg.drak.thebase.objects.Identifiable;
import gg.drak.thebase.objects.SingleSet;
import gg.drak.thebase.lib.re2j.Matcher;
import gg.drak.thebase.utils.MatcherUtils;

import java.util.List;

@Getter @Setter
public class FireString implements Identifiable {
    private String identifier;
    private FireStringConsumer consumer;

    public FireString(String identifier, FireStringConsumer consumer, boolean load) {
        this.identifier = identifier;
        this.consumer = consumer;

        if (load) {
            load();
        }
    }

    public void load() {
        FireStringManager.register(this);
    }

    public void unload() {
        FireStringManager.unregister(getIdentifier());
    }

    public void fire(String string) {
        consumer.accept(string);
    }

    public boolean checkAndFire(String string) {
        SingleSet<String, String> set = parse(string);
        if (set.getKey().equals(getIdentifier())) {
            fire(set.getValue());
            return true;
        } else {
            return false;
        }
    }

    public static String getRegex() {
        // I want to match "[hello] world" and return "hello" and "world"
        return "[(](.*?)[)] (.*)";
    }

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
