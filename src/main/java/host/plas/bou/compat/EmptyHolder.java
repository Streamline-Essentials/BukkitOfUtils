package host.plas.bou.compat;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmptyHolder extends HeldHolder {
    public EmptyHolder(String identifier) {
        super(identifier, () -> null);

        setEnabled(false);
    }
}
