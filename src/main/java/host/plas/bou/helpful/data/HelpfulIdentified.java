package host.plas.bou.helpful.data;

import gg.drak.thebase.objects.Identifiable;

public interface HelpfulIdentified extends Identifiable {
    HelpfulInfo getInfo();

    void setInfo(HelpfulInfo info);

    @Override
    default String getIdentifier() {
        return getInfo().getIdentifier();
    }

    @Override
    default void setIdentifier(String s) {
        getInfo().setIdentifier(s);
    }
}
