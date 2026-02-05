package host.plas.bou.helpful.data;

import gg.drak.thebase.objects.Identifiable;

public class HelpfulInfo implements Identifiable {
    private String identifier;
    private String prettyName;
    private String stylizedName;

    public static HelpfulInfo of(String identifier, String prettyName, String stylizedName) {
        return new HelpfulInfo(identifier, prettyName, stylizedName);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getPrettyName() {
        return this.prettyName;
    }

    public String getStylizedName() {
        return this.stylizedName;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public void setPrettyName(final String prettyName) {
        this.prettyName = prettyName;
    }

    public void setStylizedName(final String stylizedName) {
        this.stylizedName = stylizedName;
    }

    public HelpfulInfo(final String identifier, final String prettyName, final String stylizedName) {
        this.identifier = identifier;
        this.prettyName = prettyName;
        this.stylizedName = stylizedName;
    }
}
