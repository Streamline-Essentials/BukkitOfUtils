package host.plas.bou.firestring;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;
import tv.quaint.objects.SingleSet;
import tv.quaint.thebase.lib.re2j.Matcher;
import tv.quaint.utils.MatcherUtils;

import java.util.List;

@Getter @Setter
public class FireStringThing implements Identifiable {
    private String identifier;
    private FireStringConsumer consumer;

    public FireStringThing(String identifier, FireStringConsumer consumer, boolean load) {
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
        if (set.getKey().equals(string)) {
            fire(set.getValue());
            return true;
        } else {
            return false;
        }
    }

    public static String getRegex() {
        return "\\[(?<identifier>.+)] (?<string>.+)";
    }

    public static SingleSet<String, String> parse(String string) {
        Matcher matcher = MatcherUtils.matcherBuilder(getRegex(), string);
        List<String[]> groups = MatcherUtils.getGroups(matcher, 2);

        if (groups.isEmpty()) {
            return new SingleSet<>("", string);
        }

        return new SingleSet<>(groups.get(0)[0], groups.get(0)[1]);
    }
}
