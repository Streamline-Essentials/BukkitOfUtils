package host.plas.bou.helpful.data;

import gg.drak.thebase.objects.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class HelpfulInfo implements Identifiable {
    private String identifier;
    private String prettyName;
    private String stylizedName;

    public static HelpfulInfo of(String identifier, String prettyName, String stylizedName) {
        return new HelpfulInfo(identifier, prettyName, stylizedName);
    }
}
