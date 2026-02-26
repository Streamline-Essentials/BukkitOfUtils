package host.plas.bou.helpful.data;

import gg.drak.thebase.objects.Identifiable;

public interface HelpfulIdentified extends Identifiable {
    HelpfulInfo getInfo();

    void setInfo(HelpfulInfo var1);

    default String getIdentifier() {
        return this.getInfo().getIdentifier();
    }

    default void setIdentifier(String s) {
        this.getInfo().setIdentifier(s);
    }
}
