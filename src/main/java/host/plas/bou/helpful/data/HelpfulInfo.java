package host.plas.bou.helpful.data;

import gg.drak.thebase.objects.Identifiable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HelpfulInfo implements Identifiable {
    private String identifier;
    private String prettyName;
    private String stylizedName;

    public static HelpfulInfo of(String identifier, String prettyName, String stylizedName) {
        return new HelpfulInfo(identifier, prettyName, stylizedName);
    }

    public HelpfulInfo(String identifier, String prettyName, String stylizedName) {
        this.identifier = identifier;
        this.prettyName = prettyName;
        this.stylizedName = stylizedName;
    }
}
